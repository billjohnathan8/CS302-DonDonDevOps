# Python Example Microservice

This is a complete example of a Python microservice built using the development environment template. It demonstrates integration with LocalStack for AWS services including S3, DynamoDB, and SQS.

## Features

- **FastAPI** web framework with automatic OpenAPI documentation
- **LocalStack** integration for AWS services:
  - S3 (object storage)
  - DynamoDB (NoSQL database)
  - SQS (message queuing)
- **Docker** development environment
- **VS Code Dev Containers** support
- **GitLab CI/CD** pipeline ready

## Quick Start

### Using VS Code Dev Containers (Recommended)

1. Open this folder in VS Code
2. Click "Reopen in Container" when prompted
3. Wait for the container to build
4. The API will start automatically at http://localhost:8000

### Using Docker Compose

```bash
# Initialize environment
make init

# Copy and configure .env
cp .env.example .env

# Start services
make up

# View logs
make logs

# Access API at http://localhost:8000
```

## API Documentation

Once the service is running, visit:

- **API Docs (Swagger)**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc
- **OpenAPI JSON**: http://localhost:8000/openapi.json

## API Endpoints

### Health Check
```bash
curl http://localhost:8000/health
```

### S3 Operations

**List buckets:**
```bash
curl http://localhost:8000/api/s3/buckets
```

**Create bucket:**
```bash
curl -X POST http://localhost:8000/api/s3/buckets \
  -H "Content-Type: application/json" \
  -d '{"bucket_name": "my-test-bucket"}'
```

**Upload file:**
```bash
curl -X POST http://localhost:8000/api/s3/upload \
  -H "Content-Type: application/json" \
  -d '{
    "bucket_name": "my-test-bucket",
    "file_name": "hello.txt",
    "content": "Hello from LocalStack!"
  }'
```

### DynamoDB Operations

**Create users table:**
```bash
curl -X POST http://localhost:8000/api/dynamodb/table
```

**Create user:**
```bash
curl -X POST http://localhost:8000/api/dynamodb/users \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": "123",
    "name": "John Doe",
    "email": "john@example.com"
  }'
```

**Get user:**
```bash
curl http://localhost:8000/api/dynamodb/users/123
```

### SQS Operations

**List queues:**
```bash
curl http://localhost:8000/api/sqs/queues
```

**Send message:**
```bash
curl -X POST http://localhost:8000/api/sqs/message \
  -H "Content-Type: application/json" \
  -d '{
    "queue_name": "my-queue",
    "message": "Hello from SQS!"
  }'
```

## Development

### Running Tests

```bash
make test
```

### Code Quality

```bash
# Format code
make format

# Lint code
make lint

# Type checking
make type-check
```

### Accessing Container Shell

```bash
make shell
```

### Viewing Logs

```bash
# All services
make logs

# Specific service
docker-compose logs -f dev
docker-compose logs -f localstack
```

## Working with LocalStack

### Using awslocal CLI

From within the container:

```bash
make shell

# S3 operations
awslocal s3 ls
awslocal s3 ls s3://my-test-bucket

# DynamoDB operations
awslocal dynamodb list-tables
awslocal dynamodb scan --table-name users

# SQS operations
awslocal sqs list-queues
awslocal sqs receive-message --queue-url http://localhost:4566/000000000000/my-queue
```

### Check LocalStack Health

```bash
curl http://localhost:4566/_localstack/health
```

## Project Structure

```
python-example/
├── app/
│   ├── __init__.py
│   └── main.py              # FastAPI application
├── tests/                   # Tests directory
├── .devcontainer/
│   └── devcontainer.json    # VS Code Dev Container config
├── Dockerfile               # Development environment
├── docker-compose.yml       # Service orchestration
├── .env.example             # Environment template
├── requirements.txt         # Python dependencies
├── Makefile                 # Common commands
├── .gitlab-ci.yml           # CI/CD pipeline
└── README.md               # This file
```

## Customization

This example uses the Python template from `templates/python/`. You can customize:

1. **Add dependencies**: Edit `requirements.txt`
2. **Add endpoints**: Edit `app/main.py`
3. **Configure services**: Edit `docker-compose.yml`
4. **Environment variables**: Edit `.env`

## Testing Your Changes

```bash
# Stop services
make down

# Rebuild after changes
make rebuild

# Clean start
make clean
make up
```

## Deployment

This example includes a GitLab CI/CD pipeline (`.gitlab-ci.yml`) that:

1. Runs tests and linting
2. Builds Docker image
3. Pushes to container registry
4. Deploys to AWS (manual trigger)

Configure your GitLab CI/CD variables for AWS deployment:
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_DEFAULT_REGION`

## Troubleshooting

### Port already in use

```bash
# Change APP_PORT in .env
APP_PORT=8001
```

### Dependencies not installing

```bash
make rebuild
```

### LocalStack not responding

```bash
docker-compose logs localstack
docker-compose restart localstack
```

### Reset everything

```bash
make clean
make up
```

## Next Steps

- Add authentication and authorization
- Implement database migrations
- Add more AWS service integrations (SNS, Lambda, etc.)
- Write comprehensive tests
- Set up monitoring and logging

## Resources

- [FastAPI Documentation](https://fastapi.tiangolo.com/)
- [LocalStack Documentation](https://docs.localstack.cloud/)
- [Boto3 Documentation](https://boto3.amazonaws.com/v1/documentation/api/latest/index.html)
- [Template Documentation](../../docs/)
