# Template Usage Guide

This guide explains how to customize and use the development environment templates for your microservices.

## Template Structure

Each template follows this structure:

```
template-name/
├── .devcontainer/
│   └── devcontainer.json      # VS Code Dev Container configuration
├── Dockerfile                 # Development environment image
├── docker-compose.yml         # Service orchestration
├── .env.example               # Environment variables template
├── .gitlab-ci.yml             # CI/CD pipeline template
├── Makefile                   # Common commands
└── README.md                  # Template-specific documentation
```

## Using a Template for a New Microservice

### Method 1: Copy Entire Template

```bash
# Copy template to your new microservice directory
cp -r dev-environment-templates/templates/nodejs my-new-service
cd my-new-service

# Initialize
cp .env.example .env
```

### Method 2: Copy into Existing Project

```bash
cd /path/to/existing/project

# Copy template files
cp /path/to/dev-environment-templates/templates/nodejs/Dockerfile .
cp /path/to/dev-environment-templates/templates/nodejs/docker-compose.yml .
cp /path/to/dev-environment-templates/templates/nodejs/.env.example .env
cp -r /path/to/dev-environment-templates/templates/nodejs/.devcontainer .
cp /path/to/dev-environment-templates/templates/nodejs/Makefile .
cp /path/to/dev-environment-templates/templates/nodejs/.gitlab-ci.yml .
```

### Method 3: Git Submodule (Advanced)

```bash
# Add templates as submodule
git submodule add <templates-repo-url> .devtools

# Symlink template files
ln -s .devtools/templates/nodejs/Dockerfile .
ln -s .devtools/templates/nodejs/docker-compose.yml .
cp .devtools/templates/nodejs/.env.example .env
```

## Customizing Templates

### 1. Environment Variables (.env)

The `.env` file controls runtime configuration:

```bash
# Service Configuration
SERVICE_NAME=my-auth-service    # Change this to your service name
APP_PORT=3000                   # Change if port conflicts
DEBUG_PORT=9229                 # Debugger port

# AWS Configuration
AWS_DEFAULT_REGION=us-east-1    # Your preferred AWS region
LOCALSTACK_SERVICES=s3,dynamodb # Only services you need

# Database (if enabled)
POSTGRES_DB=auth_db             # Your database name
DATABASE_URL=postgresql://...   # Update connection string
```

**Best Practices:**
- Never commit `.env` files (add to `.gitignore`)
- Document required variables in README
- Use descriptive service names
- Only enable services you actually use

### 2. Dockerfile Customization

#### Adding System Packages

```dockerfile
# Add to the apt-get install section
RUN apt-get update && apt-get install -y \
    git \
    curl \
    # ... existing packages ...
    your-new-package \
    && rm -rf /var/lib/apt/lists/*
```

#### Adding Language-Specific Tools

**Node.js:**
```dockerfile
RUN npm install -g \
    typescript \
    # ... existing tools ...
    your-global-package
```

**Python:**
```dockerfile
RUN pip install \
    pytest \
    # ... existing packages ...
    your-python-package
```

**Java:**
```dockerfile
# Install additional tools
RUN wget https://example.com/tool.tar.gz \
    && tar -xzf tool.tar.gz -C /opt \
    && rm tool.tar.gz
```

#### Changing User UID/GID (for Linux)

```dockerfile
ARG USER_UID=1000
ARG USER_GID=1000

# The template will use these values
# Override during build:
# docker-compose build --build-arg USER_UID=$(id -u) --build-arg USER_GID=$(id -g)
```

### 3. Docker Compose Customization

#### Adding Ports

```yaml
services:
  dev:
    ports:
      - "${APP_PORT:-3000}:3000"
      - "${DEBUG_PORT:-9229}:9229"
      - "8080:8080"  # Add new port mapping
```

#### Enabling Optional Services

Uncomment pre-configured services:

```yaml
# Uncomment PostgreSQL
postgres:
  image: postgres:15-alpine
  # ... rest of config
```

#### Adding New Services

```yaml
services:
  # Your existing dev service
  dev:
    # ... config ...

  # Add a new service
  mongodb:
    image: mongo:6
    container_name: ${SERVICE_NAME}-mongo
    ports:
      - "27017:27017"
    volumes:
      - mongo_data:/data/db
    networks:
      - microservices

volumes:
  mongo_data:  # Don't forget to add volume
```

#### Environment-Specific Overrides

Create `docker-compose.override.yml` for local customizations:

```yaml
# docker-compose.override.yml (not committed)
version: '3.9'

services:
  dev:
    volumes:
      # Mount additional local directories
      - ~/my-shared-libs:/libs:ro
    environment:
      # Override environment variables
      - DEBUG=true
```

### 4. VS Code Dev Container Customization

Edit `.devcontainer/devcontainer.json`:

#### Adding Extensions

```json
{
  "customizations": {
    "vscode": {
      "extensions": [
        "dbaeumer.vscode-eslint",
        // Add your extension
        "your-publisher.your-extension"
      ]
    }
  }
}
```

#### Changing Settings

```json
{
  "customizations": {
    "vscode": {
      "settings": {
        "editor.formatOnSave": true,
        "your.custom.setting": "value"
      }
    }
  }
}
```

#### Adding Post-Create Commands

```json
{
  "postCreateCommand": "npm install && npm run setup"
}
```

#### Mounting Additional Volumes

```json
{
  "mounts": [
    "source=${localEnv:HOME}/.aws,target=/home/node/.aws,type=bind,consistency=cached"
  ]
}
```

### 5. Makefile Customization

Add project-specific commands:

```makefile
# Add at the end of Makefile

migrate: ## Run database migrations
	@docker-compose exec dev npm run migrate

seed: ## Seed database
	@docker-compose exec dev npm run seed

generate: ## Generate API documentation
	@docker-compose exec dev npm run generate-docs
```

### 6. GitLab CI/CD Customization

Edit `.gitlab-ci.yml`:

#### Adding Stages

```yaml
stages:
  - test
  - build
  - security-scan  # New stage
  - deploy
```

#### Adding Jobs

```yaml
security:scan:
  stage: security-scan
  image: aquasec/trivy:latest
  script:
    - trivy image $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
  only:
    - main
    - merge_requests
```

#### Customizing Deployment

```yaml
deploy:dev:
  script:
    # Replace with your specific AWS commands
    - aws ecs update-service --cluster my-cluster --service my-service --force-new-deployment
    # Or for Lambda:
    - aws lambda update-function-code --function-name my-function --image-uri $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
```

## Template Maintenance

### Keeping Templates Updated

```bash
# If using Git submodule
git submodule update --remote

# If copied directly, manually sync changes
# Compare your files with template updates
diff Dockerfile /path/to/templates/nodejs/Dockerfile
```

### Version Control

Add to `.gitignore`:
```
.env
.env.local
.env.*.local
node_modules/
__pycache__/
*.pyc
.gradle/
build/
target/
.idea/
.vscode/
*.iml
.DS_Store
```

Add to `.gitattributes`:
```
*.sh text eol=lf
Makefile text eol=lf
```

Commit to Git:
```
Dockerfile
docker-compose.yml
.devcontainer/
Makefile
.gitlab-ci.yml
.env.example
.gitignore
```

## Common Customization Patterns

### Pattern 1: Adding Database Migrations

**Node.js (TypeORM example):**

1. Update `docker-compose.yml`:
```yaml
command: bash -c "npm install && npm run migrate && npm run dev"
```

2. Add to Makefile:
```makefile
migrate: ## Run migrations
	@docker-compose exec dev npm run typeorm migration:run
```

**Python (Alembic example):**

1. Update `docker-compose.yml`:
```yaml
command: bash -c "pip install -r requirements.txt && alembic upgrade head && python app/main.py"
```

### Pattern 2: Multi-Stage Builds

For production-ready images:

```dockerfile
# Development stage (existing)
FROM node:20-bullseye AS development
# ... your development setup ...

# Production stage (add this)
FROM node:20-alpine AS production
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY --from=development /workspace/dist ./dist
CMD ["node", "dist/main.js"]
```

### Pattern 3: Shared Configuration

For multiple services sharing config:

```yaml
# Create a base service
x-base-service: &base-service
  networks:
    - microservices
  environment: &base-environment
    AWS_ENDPOINT_URL: http://localstack:4566
    AWS_DEFAULT_REGION: us-east-1

services:
  service1:
    <<: *base-service
    # ... service-specific config ...

  service2:
    <<: *base-service
    # ... service-specific config ...
```

## Troubleshooting Customizations

### Changes Not Reflecting

```bash
# Rebuild containers
make rebuild
# or
docker-compose up -d --build

# For major changes, clean and rebuild
make clean
make up
```

### Volume Permission Issues

```bash
# Linux: Match container UID to host UID
export USER_UID=$(id -u)
export USER_GID=$(id -g)
docker-compose build --build-arg USER_UID=$USER_UID --build-arg USER_GID=$USER_GID
```

### Environment Variables Not Loading

```bash
# Ensure .env is in the same directory as docker-compose.yml
# Restart after changing .env
docker-compose down
docker-compose up -d
```

## Next Steps

- [LocalStack Configuration Guide](localstack-guide.md)
- [Integration Environment Guide](integration-guide.md)
- [Best Practices & Troubleshooting](best-practices.md)
