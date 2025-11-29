# Example Microservices

This directory contains complete, working examples of microservices built using the development environment templates.

## Available Examples

### Python Example
**Location**: `python-example/`

A FastAPI-based microservice demonstrating:
- RESTful API with automatic OpenAPI documentation
- AWS S3 integration (object storage)
- DynamoDB integration (NoSQL database)
- SQS integration (message queuing)
- Complete test suite with pytest
- LocalStack integration for local AWS development

**Quick Start:**
```bash
cd python-example
make init
make up
# Visit http://localhost:8000/docs for API documentation
```

[View Python Example Documentation](python-example/README.md)

## Using These Examples

### As Learning Resources

These examples demonstrate:
1. How to use the development environment templates
2. Best practices for structuring microservices
3. LocalStack integration for AWS services
4. Testing strategies
5. CI/CD pipeline configuration

### As Starting Points

You can copy an example to start your own microservice:

```bash
# Copy example to your new service
cp -r examples/python-example ../my-new-service
cd ../my-new-service

# Customize for your needs
# 1. Update .env with your configuration
# 2. Modify app/main.py with your business logic
# 3. Update tests
# 4. Push to your Git repository
```

### As References

Browse the examples to see:
- How to configure Docker and docker-compose
- How to integrate with LocalStack
- How to structure tests
- How to set up CI/CD pipelines

## What's Included in Each Example

All examples include:

- ✅ **Complete application code** - Fully functional microservice
- ✅ **Development environment** - Dockerfile and docker-compose.yml
- ✅ **VS Code Dev Containers** - .devcontainer configuration
- ✅ **LocalStack integration** - AWS services emulation
- ✅ **Tests** - Unit and integration tests
- ✅ **CI/CD Pipeline** - GitLab CI/CD configuration
- ✅ **Makefile** - Common development commands
- ✅ **Documentation** - Comprehensive README

## Running the Examples

### Prerequisites

- Docker Desktop (20.10+)
- Docker Compose (2.0+)
- VS Code with Dev Containers extension (optional)

### Using VS Code Dev Containers

1. Open example folder in VS Code
2. Click "Reopen in Container"
3. Wait for setup to complete
4. Service starts automatically

### Using Docker Compose

```bash
cd python-example  # or your chosen example
make init          # Initialize environment
make up            # Start services
make logs          # View logs
make test          # Run tests
make down          # Stop services
```

## Testing Locally

Each example includes:

1. **Health Check Endpoint**
   ```bash
   curl http://localhost:8000/health  # Python
   curl http://localhost:3000/health  # Node.js
   ```

2. **API Documentation**
   - Python: http://localhost:8000/docs
   - Node.js: Usually in README

3. **LocalStack Dashboard**
   - http://localhost:4566/_localstack/health

## Customization

To customize an example for your needs:

1. **Environment Variables**: Edit `.env` file
2. **Dependencies**: Update requirements.txt (Python) or package.json (Node.js)
3. **Business Logic**: Modify application code
4. **AWS Services**: Edit LOCALSTACK_SERVICES in .env
5. **Ports**: Change APP_PORT in .env
6. **Database**: Uncomment PostgreSQL/Redis in docker-compose.yml

## Learning Path

Recommended order for exploring examples:

1. **Start with Python Example** - Most comprehensive, best documented
2. **Try LocalStack Integration** - Learn AWS service emulation
3. **Run Tests** - Understand testing strategies
4. **Customize** - Modify for your use case
5. **Deploy** - Use CI/CD pipeline

## Common Tasks

### Adding a New Endpoint

**Python:**
```python
# app/main.py
@app.get("/api/my-endpoint")
async def my_endpoint():
    return {"message": "Hello World"}
```

### Adding Dependencies

**Python:**
```bash
# Add to requirements.txt
new-package==1.0.0

# Rebuild
make rebuild
```

### Running Tests

```bash
make test
```

### Debugging

```bash
# View logs
make logs

# Access container
make shell

# Check LocalStack
curl http://localhost:4566/_localstack/health
```

## Troubleshooting

### Port Conflicts

Change port in `.env`:
```bash
APP_PORT=8001  # or any available port
```

### Container Won't Start

```bash
# Check logs
docker-compose logs

# Rebuild
make rebuild

# Clean restart
make clean
make up
```

### LocalStack Issues

```bash
# Check health
curl http://localhost:4566/_localstack/health

# Restart
docker-compose restart localstack
```

## Next Steps

After exploring the examples:

1. Read the [Getting Started Guide](../docs/getting-started.md)
2. Learn about [Template Customization](../docs/template-usage.md)
3. Explore [LocalStack Configuration](../docs/localstack-guide.md)
4. Set up [Integration Environment](../docs/integration-guide.md)
5. Review [Best Practices](../docs/best-practices.md)

## Contributing

To add a new example:

1. Create new directory in `examples/`
2. Copy appropriate template from `templates/`
3. Implement complete, working application
4. Add comprehensive tests
5. Write detailed README
6. Update this file

## Resources

- [Main Documentation](../docs/)
- [Templates](../templates/)
- [Integration Environment](../integration/)
- [Shared Scripts](../shared/)
