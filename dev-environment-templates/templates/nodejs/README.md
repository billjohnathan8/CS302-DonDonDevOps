# Node.js/TypeScript Development Environment Template

This template provides a complete Docker-based development environment for Node.js and TypeScript microservices with LocalStack integration.

## What's Included

- **Node.js 20** (LTS version)
- **TypeScript** compiler and ts-node
- **Development tools**: nodemon, eslint, prettier
- **AWS CLI v2** and awslocal wrapper
- **LocalStack** for AWS service emulation
- **Database clients**: PostgreSQL, MySQL, Redis
- **VS Code Dev Container** configuration with recommended extensions

## Quick Start

### Option 1: Using VS Code Dev Containers (Recommended)

1. Copy this template to your microservice repository
2. Copy `.env.example` to `.env` and customize
3. Open the folder in VS Code
4. Click "Reopen in Container" when prompted
5. Wait for the container to build and dependencies to install

### Option 2: Using Docker Compose

1. Copy this template to your microservice repository
2. Copy `.env.example` to `.env` and customize
3. Run:
   ```bash
   docker-compose up -d
   docker-compose exec dev bash
   ```

## Customization

### Adding Dependencies

Edit `package.json` in your microservice and run:
```bash
npm install
```

The `node_modules` volume will be updated automatically.

### Modifying Ports

Update the following in `.env`:
- `APP_PORT` - Your application's main port
- `DEBUG_PORT` - Node.js debugger port

### Enabling Optional Services

Uncomment the desired services in `docker-compose.yml`:
- PostgreSQL database
- Redis cache

### LocalStack Services

Customize `LOCALSTACK_SERVICES` in `.env` to include only the AWS services you need:
```
LOCALSTACK_SERVICES=s3,dynamodb,sqs,sns,lambda
```

## Debugging

### VS Code Debugging

Create `.vscode/launch.json`:
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "node",
      "request": "attach",
      "name": "Docker: Attach to Node",
      "port": 9229,
      "restart": true,
      "skipFiles": ["<node_internals>/**"]
    }
  ]
}
```

### Viewing Logs

```bash
docker-compose logs -f dev
```

## Testing with LocalStack

LocalStack is pre-configured and will start automatically. Use the AWS CLI with the `--endpoint-url` flag:

```bash
# Using AWS CLI
aws --endpoint-url=http://localstack:4566 s3 ls

# Using awslocal wrapper (recommended)
awslocal s3 ls
```

## File Structure

```
your-microservice/
├── .devcontainer/
│   └── devcontainer.json
├── src/
│   └── index.ts
├── tests/
├── Dockerfile
├── docker-compose.yml
├── .env
├── package.json
├── tsconfig.json
└── README.md
```

## Troubleshooting

### Container won't start
- Check Docker Desktop is running
- Verify ports are not already in use
- Check `.env` file is properly configured

### Dependencies not installing
- Delete `node_modules` volume: `docker-compose down -v`
- Rebuild: `docker-compose up --build`

### LocalStack not responding
- Check LocalStack health: `curl http://localhost:4566/_localstack/health`
- View logs: `docker-compose logs localstack`

## Next Steps

- [Integration Environment Guide](../../docs/integration-guide.md)
- [LocalStack Configuration Guide](../../docs/localstack-guide.md)
- [Best Practices](../../docs/best-practices.md)
