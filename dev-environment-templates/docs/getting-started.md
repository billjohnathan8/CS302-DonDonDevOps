# Getting Started Guide

This guide will help you set up your local development environment using the Docker development templates.

## Prerequisites

Before you begin, ensure you have the following installed:

### Required Software

1. **Docker Desktop** (version 20.10 or higher)
   - [Download for Windows](https://docs.docker.com/desktop/install/windows-install/)
   - [Download for macOS](https://docs.docker.com/desktop/install/mac-install/)
   - [Download for Linux](https://docs.docker.com/desktop/install/linux-install/)

2. **Docker Compose** (version 2.0 or higher)
   - Included with Docker Desktop
   - For Linux: Install separately following [official docs](https://docs.docker.com/compose/install/)

3. **Git**
   - [Download Git](https://git-scm.com/downloads)

### Optional (but Recommended)

4. **Visual Studio Code**
   - [Download VS Code](https://code.visualstudio.com/)
   - Install the **Dev Containers** extension

5. **Make** (for using Makefile commands)
   - macOS/Linux: Pre-installed
   - Windows: Install via [Chocolatey](https://chocolatey.org/) or use Git Bash

## Verification

Verify your installations:

```bash
# Check Docker
docker --version
# Expected: Docker version 20.10.x or higher

# Check Docker Compose
docker-compose --version
# Expected: Docker Compose version 2.x.x or higher

# Check Git
git --version
# Expected: git version 2.x.x or higher

# Check VS Code (optional)
code --version
```

## Quick Start: Creating Your First Development Environment

### Step 1: Clone the Templates Repository

```bash
git clone <your-templates-repo-url>
cd dev-environment-templates
```

### Step 2: Choose Your Tech Stack Template

Navigate to the appropriate template directory:

- **Node.js/TypeScript**: `templates/nodejs/`
- **Python**: `templates/python/`
- **Java/Kotlin**: `templates/java/`

```bash
# Example for Node.js
cd templates/nodejs
```

### Step 3: Copy Template to Your Microservice

```bash
# Option A: Copy template to new microservice directory
cp -r templates/nodejs ../my-new-microservice
cd ../my-new-microservice

# Option B: Copy template files to existing microservice
cp templates/nodejs/{Dockerfile,docker-compose.yml,.env.example,.devcontainer} /path/to/your/microservice/
```

### Step 4: Configure Environment

```bash
# Copy environment template
cp .env.example .env

# Edit .env with your preferred editor
nano .env  # or code .env
```

Update the following variables in `.env`:
```bash
SERVICE_NAME=my-microservice  # Your service name
APP_PORT=3000                 # Your application port
# ... other configuration
```

### Step 5: Start Development Environment

#### Option A: Using VS Code Dev Containers (Recommended)

1. Open your microservice folder in VS Code
2. You'll see a prompt: **"Reopen in Container"** - Click it
3. Wait for the container to build (this may take a few minutes the first time)
4. Once ready, you'll have a full development environment inside the container

---

1. Access "Open in Remote Window"
2. Click "Reopen in Container"
   ![alt text](image.png)

#### Option B: Using Docker Compose

```bash
# Initialize environment
make init  # or: bash ../../shared/scripts/dev-init.sh

# Start services
make up    # or: docker-compose up -d

# View logs
make logs  # or: docker-compose logs -f

# Access container shell
make shell # or: docker-compose exec dev bash
```

### Step 6: Verify Everything Works

#### Check Running Containers

```bash
make ps
# or
docker-compose ps
```

You should see containers running for:
- Your dev container
- LocalStack
- (Optional) PostgreSQL, Redis, etc.

#### Check Service Health

```bash
make health
# or manually:
curl http://localhost:4566/_localstack/health  # LocalStack
```

#### Test Your Application

```bash
# From your host machine
curl http://localhost:3000  # or your APP_PORT

# Or from within the container
make shell
curl http://localhost:3000
```

## Common Workflows

### Installing Dependencies

**Node.js:**
```bash
make shell
npm install package-name
```

**Python:**
```bash
make shell
pip install package-name
# Don't forget to update requirements.txt
```

**Java:**
```bash
make shell
# Add dependency to build.gradle, then:
./gradlew dependencies
```

### Running Tests

```bash
# Using Makefile
make test

# Or manually
make shell
npm test  # Node.js
pytest    # Python
./gradlew test  # Java
```

### Viewing Logs

```bash
# All services
make logs

# Specific service
docker-compose logs -f localstack
```

### Accessing LocalStack (AWS Emulation)

```bash
# From within container
make shell
awslocal s3 ls

# From host (if you have aws cli installed)
aws --endpoint-url=http://localhost:4566 s3 ls
```

## Troubleshooting

### Windows "Error response from daemon: can't access specified distro mount service"
- https://stackoverflow.com/a/78559587

### Container Won't Start

```bash
# Check Docker is running
docker ps

# Check for port conflicts
lsof -i :3000  # macOS/Linux
netstat -ano | findstr :3000  # Windows

# View detailed logs
docker-compose logs dev
```

### Permission Issues (Linux)

```bash
# Fix file ownership
sudo chown -R $USER:$USER .

# Or run with current user
export USER_UID=$(id -u)
export USER_GID=$(id -g)
docker-compose up
```

### "Port Already in Use" Error

Edit `.env` and change the conflicting port:
```bash
APP_PORT=3001  # or any available port
```

### Container Builds Slowly

```bash
# Use build cache
docker-compose build --parallel

# Or enable BuildKit
export DOCKER_BUILDKIT=1
docker-compose build
```

## Next Steps

- [Template Usage Guide](template-usage.md) - Learn how to customize templates
- [LocalStack Configuration Guide](localstack-guide.md) - Set up AWS services locally
- [Integration Environment Guide](integration-guide.md) - Run multiple services together
- [Best Practices & Troubleshooting](best-practices.md) - Tips and solutions

## Getting Help

If you encounter issues:

1. Check the [Best Practices & Troubleshooting Guide](best-practices.md)
2. Review template-specific README files
3. Check Docker logs: `docker-compose logs`
4. Verify Docker Desktop is running and has enough resources
5. Contact your team lead or DevOps team

## Useful Commands Reference

```bash
# Environment management
make init          # Initialize environment
make up            # Start services
make down          # Stop services
make restart       # Restart services
make clean         # Remove all containers and volumes

# Development
make shell         # Access container shell
make logs          # View logs
make test          # Run tests
make ps            # Show running containers

# Docker Compose (if not using Make)
docker-compose up -d
docker-compose down
docker-compose logs -f
docker-compose exec dev bash
docker-compose ps
docker-compose restart
```
