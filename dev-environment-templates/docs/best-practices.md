# Best Practices & Troubleshooting Guide

This guide covers recommended practices for using Docker development environments and solutions to common issues.

## Best Practices

### 1. Environment Configuration

#### Use Environment Variables

✅ **DO:**
```yaml
# docker-compose.yml
environment:
  - DATABASE_URL=${DATABASE_URL}
  - API_KEY=${API_KEY}
```

❌ **DON'T:**
```yaml
environment:
  - DATABASE_URL=postgresql://postgres:password@postgres:5432/db
```

#### Separate Concerns

- `.env.example` - Template with documentation
- `.env` - Local configuration (gitignored)
- `.env.local` - Developer overrides (gitignored)

```bash
# .env.example
# Database connection string
DATABASE_URL=postgresql://postgres:postgres@postgres:5432/mydb

# API port (default: 3000)
APP_PORT=3000
```

### 2. Volume Management

#### Use Named Volumes for Dependencies

✅ **DO:**
```yaml
volumes:
  - .:/workspace:cached
  - node_modules:/workspace/node_modules
```

**Why?** Improves performance, especially on macOS/Windows

#### Use Bind Mounts for Source Code

```yaml
volumes:
  - .:/workspace:cached  # cached improves performance
```

#### Clean Up Regularly

```bash
# Remove unused volumes
docker volume prune

# Clean everything
docker system prune -a --volumes
```

### 3. Resource Optimization

#### Limit LocalStack Services

Only enable services you need:
```bash
# .env
LOCALSTACK_SERVICES=s3,dynamodb  # Not all services
```

#### Configure Docker Desktop Resources

Recommended minimums:
- **CPUs**: 4
- **Memory**: 8 GB
- **Disk**: 60 GB

### 4. Development Workflow

#### Daily Workflow

```bash
# Morning
make up            # Start environment

# During development
make logs          # Monitor logs
make test          # Run tests frequently

# Evening
make down          # Stop services (preserves data)
# OR
make clean         # Full cleanup (removes data)
```

#### Before Committing

```bash
make test          # Run all tests
make lint          # Check code quality
make clean         # Clean environment
make up            # Fresh start
make test          # Verify clean build
```

### 5. Docker Image Best Practices

#### Multi-Stage Builds for Production

```dockerfile
# Development
FROM node:20-bullseye AS development
# ... dev dependencies ...

# Production
FROM node:20-alpine AS production
COPY --from=development /workspace/dist ./dist
# ... only production dependencies ...
```

#### Minimize Layer Size

✅ **DO:**
```dockerfile
RUN apt-get update && apt-get install -y \
    package1 \
    package2 \
    && rm -rf /var/lib/apt/lists/*
```

❌ **DON'T:**
```dockerfile
RUN apt-get update
RUN apt-get install package1
RUN apt-get install package2
```

#### Use .dockerignore

```
# .dockerignore
node_modules
npm-debug.log
.env
.git
.gitignore
README.md
.vscode
.idea
*.md
```

### 6. Security Practices

#### Run as Non-Root User

Templates already use non-root users:
```dockerfile
USER node  # or python, devuser, etc.
```

#### Don't Commit Secrets

```bash
# .gitignore
.env
.env.local
.env.*.local
secrets/
*.pem
*.key
```

#### Scan Images Regularly

```bash
# Using Trivy
docker run aquasec/trivy image your-image:tag
```

### 7. Performance Optimization

#### Enable BuildKit

```bash
# ~/.profile or ~/.zshrc
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1
```

#### Use Build Cache

```bash
# Build with cache
docker-compose build

# Force rebuild
docker-compose build --no-cache
```

#### Optimize on macOS/Windows

Use `cached` or `delegated` for volumes:
```yaml
volumes:
  - .:/workspace:cached
```

### 8. Git Workflow

#### Version Control Structure

```
my-microservice/
├── src/                    # Your source code
├── tests/                  # Your tests
├── .devcontainer/          # ✓ Commit
├── Dockerfile              # ✓ Commit
├── docker-compose.yml      # ✓ Commit
├── .env.example            # ✓ Commit
├── .env                    # ✗ DON'T commit
├── Makefile                # ✓ Commit
├── .gitlab-ci.yml          # ✓ Commit
└── README.md               # ✓ Commit
```

#### .gitattributes for Line Endings

```
# .gitattributes
*.sh text eol=lf
Makefile text eol=lf
```

## Troubleshooting

### Container Issues

#### Container Won't Start

**Symptom:** `docker-compose up` fails

**Solutions:**

1. Check logs:
```bash
docker-compose logs dev
```

2. Check for port conflicts:
```bash
# macOS/Linux
lsof -i :3000

# Windows
netstat -ano | findstr :3000
```

3. Rebuild containers:
```bash
docker-compose down
docker-compose up --build
```

4. Check Docker Desktop is running:
```bash
docker ps
```

#### Container Exits Immediately

**Check command in docker-compose.yml:**
```yaml
command: bash -c "npm install && npm run dev"
```

**Debug:**
```bash
# Override command to keep container running
docker-compose run dev bash
```

### Volume and Permission Issues

#### Permission Denied (Linux)

**Symptom:** Can't write files, permission errors

**Solution 1:** Match UIDs
```bash
export USER_UID=$(id -u)
export USER_GID=$(id -g)
docker-compose build --build-arg USER_UID=$USER_UID --build-arg USER_GID=$USER_GID
```

**Solution 2:** Fix ownership
```bash
sudo chown -R $USER:$USER .
```

#### Changes Not Reflecting

**Solution:** Check volume mounts
```bash
# Verify mounts
docker-compose config | grep volumes -A 5

# Restart with fresh mount
docker-compose down
docker-compose up -d
```

#### node_modules Issues (Node.js)

**Symptom:** Module not found errors

**Solution:**
```bash
# Enter container
docker-compose exec dev bash

# Delete and reinstall
rm -rf node_modules package-lock.json
npm install

# Or use volume
docker-compose down -v  # Remove volumes
docker-compose up -d    # Recreate
```

### Network Issues

#### Can't Connect to LocalStack

**From container:**
```bash
docker-compose exec dev bash
curl http://localstack:4566/_localstack/health
```

**From host:**
```bash
curl http://localhost:4566/_localstack/health
```

**Solution:** Check networks
```bash
docker network inspect microservices-network
```

#### Services Can't Communicate

**Verify all on same network:**
```bash
docker-compose ps
docker network inspect microservices-network
```

**Test connectivity:**
```bash
docker-compose exec service1 ping service2
docker-compose exec service1 curl http://service2:8000
```

### Performance Issues

#### Slow on macOS/Windows

**Solutions:**

1. Use `:cached` or `:delegated`:
```yaml
volumes:
  - .:/workspace:cached
```

2. Exclude unnecessary directories:
```yaml
volumes:
  - .:/workspace:cached
  - /workspace/node_modules  # Don't sync
```

3. Increase Docker resources (Settings > Resources)

4. Use volumes for dependencies:
```yaml
volumes:
  - node_modules:/workspace/node_modules
```

#### Slow Build Times

**Solutions:**

1. Enable BuildKit:
```bash
export DOCKER_BUILDKIT=1
```

2. Use build cache:
```dockerfile
# Copy package files first
COPY package*.json ./
RUN npm install

# Then copy source
COPY . .
```

3. Use .dockerignore
4. Build in parallel:
```bash
docker-compose build --parallel
```

### Database Issues

#### Connection Refused

**Check database is ready:**
```bash
docker-compose ps postgres
docker-compose logs postgres
```

**Wait for health check:**
```yaml
depends_on:
  postgres:
    condition: service_healthy
```

#### Data Persisting When It Shouldn't

**Remove volumes:**
```bash
docker-compose down -v
docker-compose up -d
```

#### Migrations Failing

**Run manually:**
```bash
docker-compose exec dev bash
npm run migrate  # or your migration command
```

### LocalStack Issues

#### Services Not Available

**Check enabled services:**
```bash
curl http://localhost:4566/_localstack/health | jq '.services'
```

**Enable in .env:**
```bash
LOCALSTACK_SERVICES=s3,dynamodb,sqs,sns
```

**Restart:**
```bash
docker-compose restart localstack
```

#### Resources Not Persisting

**Enable persistence:**
```yaml
environment:
  - PERSISTENCE=1
volumes:
  - localstack_data:/var/lib/localstack
```

### VS Code Dev Container Issues

#### Container Won't Reopen

**Solutions:**

1. Rebuild container:
   - Command Palette (`Ctrl/Cmd + Shift + P`)
   - "Dev Containers: Rebuild Container"

2. Check .devcontainer/devcontainer.json syntax:
```bash
cat .devcontainer/devcontainer.json | jq '.'
```

3. View logs:
   - Command Palette
   - "Dev Containers: Show Container Log"

#### Extensions Not Installing

**Check configuration:**
```json
{
  "customizations": {
    "vscode": {
      "extensions": [
        "valid-extension-id"
      ]
    }
  }
}
```

**Manually install:**
- Open Extensions panel
- Install required extensions

### CI/CD Issues

#### Pipeline Fails But Works Locally

**Common causes:**

1. **Missing environment variables**
   - Add to GitLab CI/CD settings

2. **Different Node/Python/Java version**
   - Match versions in Dockerfile and .gitlab-ci.yml

3. **Missing dependencies**
   - Check package files are committed

4. **Timing issues**
   - Add delays or health checks

**Debug:**
```yaml
test:
  script:
    - env  # Print all environment variables
    - ls -la  # Check files
    - # your test command
```

## Common Error Messages

### "port is already allocated"

**Solution:**
```bash
# Change port in .env
APP_PORT=3001

# Or stop conflicting service
docker ps  # Find container using port
docker stop <container-id>
```

### "no space left on device"

**Solution:**
```bash
# Clean Docker
docker system prune -a --volumes

# Check disk space
docker system df
```

### "network not found"

**Solution:**
```bash
# Recreate network
docker-compose down
docker network rm microservices-network
docker-compose up -d
```

### "exec format error"

**Cause:** Usually line ending issues in scripts

**Solution:**
```bash
# Fix line endings
dos2unix script.sh
# Or
sed -i 's/\r$//' script.sh
```

## Debugging Techniques

### Interactive Debugging

```bash
# Override entrypoint to debug
docker-compose run --entrypoint bash dev

# Run specific command
docker-compose run dev npm test -- --verbose

# Check environment
docker-compose exec dev env
```

### Log Analysis

```bash
# Follow logs
docker-compose logs -f

# Last 100 lines
docker-compose logs --tail=100 dev

# Since timestamp
docker-compose logs --since 2024-01-01T00:00:00

# Save to file
docker-compose logs > debug.log
```

### Resource Monitoring

```bash
# Container stats
docker stats

# Specific service
docker stats $(docker-compose ps -q dev)
```

## Getting Help

### Information to Gather

When asking for help, provide:

1. **Environment:**
```bash
docker --version
docker-compose --version
OS and version
```

2. **Logs:**
```bash
docker-compose logs > issue-logs.txt
```

3. **Configuration:**
```bash
docker-compose config > config.yml
```

4. **Error messages** (full output)

5. **What you've tried**

### Useful Commands

```bash
# Full diagnostic
docker info
docker-compose ps
docker-compose config
docker network ls
docker volume ls
docker images

# Clean slate
docker-compose down -v
docker system prune -a
docker-compose up -d --build
```

## Next Steps

- [Getting Started Guide](getting-started.md)
- [Template Usage Guide](template-usage.md)
- [LocalStack Configuration Guide](localstack-guide.md)
- [Integration Environment Guide](integration-guide.md)
