# Python Development Environment Template

This template provides a complete Docker-based development environment for Python microservices with LocalStack integration.

## What's Included

- **Python 3.11** with pip, setuptools, and wheel
- **Package managers**: poetry, pipenv
- **Code quality tools**: black, flake8, pylint, mypy
- **Testing tools**: pytest, pytest-cov
- **AWS CLI v2** and awslocal wrapper
- **LocalStack** for AWS service emulation
- **Database clients**: PostgreSQL, MySQL, Redis
- **VS Code Dev Container** configuration with recommended extensions

## Quick Start

### Option 1: Using VS Code Dev Containers (Recommended)

1. Copy this template to your microservice repository
2. Copy `.env.example` to `.env` and customize
3. Create `requirements.txt` with your dependencies
4. Open the folder in VS Code
5. Click "Reopen in Container" when prompted
6. Wait for the container to build and dependencies to install

### Option 2: Using Docker Compose

1. Copy this template to your microservice repository
2. Copy `.env.example` to `.env` and customize
3. Create `requirements.txt` with your dependencies
4. Run:
   ```bash
   docker-compose up -d
   docker-compose exec dev bash
   ```

## Customization

### Adding Dependencies

Add packages to `requirements.txt` or use poetry/pipenv, then run:
```bash
pip install -r requirements.txt
# or
poetry install
```

### Modifying Ports

Update the following in `.env`:
- `APP_PORT` - Your application's main port (default: 8000)
- `DEBUG_PORT` - Python debugger port (default: 5678)

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
      "name": "Python: Remote Attach",
      "type": "python",
      "request": "attach",
      "connect": {
        "host": "localhost",
        "port": 5678
      },
      "pathMappings": [
        {
          "localRoot": "${workspaceFolder}",
          "remoteRoot": "/workspace"
        }
      ]
    }
  ]
}
```

Install debugpy in your requirements and add to your code:
```python
import debugpy
debugpy.listen(("0.0.0.0", 5678))
debugpy.wait_for_client()
```

### Viewing Logs

```bash
docker-compose logs -f dev
```

## Testing with LocalStack

LocalStack is pre-configured and will start automatically. Use boto3 with custom endpoint:

```python
import boto3

s3 = boto3.client(
    's3',
    endpoint_url='http://localstack:4566',
    aws_access_key_id='test',
    aws_secret_access_key='test'
)
```

Or use awslocal CLI:
```bash
awslocal s3 ls
```

## File Structure

```
your-microservice/
├── .devcontainer/
│   └── devcontainer.json
├── app/
│   ├── __init__.py
│   └── main.py
├── tests/
│   └── test_main.py
├── Dockerfile
├── docker-compose.yml
├── .env
├── requirements.txt
├── setup.py
└── README.md
```

## Troubleshooting

### Container won't start
- Check Docker Desktop is running
- Verify ports are not already in use
- Check `.env` file is properly configured

### Dependencies not installing
- Verify `requirements.txt` syntax
- Check for package compatibility issues
- Rebuild: `docker-compose up --build`

### LocalStack not responding
- Check LocalStack health: `curl http://localhost:4566/_localstack/health`
- View logs: `docker-compose logs localstack`

## Next Steps

- [Integration Environment Guide](../../docs/integration-guide.md)
- [LocalStack Configuration Guide](../../docs/localstack-guide.md)
- [Best Practices](../../docs/best-practices.md)
