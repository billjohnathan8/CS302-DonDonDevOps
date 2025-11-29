# Integration Environment

This environment allows you to run multiple microservices together for integration testing and development.

## What's Included

- **LocalStack** - AWS services emulation (shared across all services)
- **PostgreSQL** - Shared database with support for multiple databases
- **Redis** - Shared cache
- **Network** - Common Docker network for inter-service communication

## Quick Start

1. Copy `.env.example` to `.env` and customize
2. Uncomment and configure your microservices in `docker-compose.yml`
3. Ensure your microservice repositories are in the correct relative paths
4. Run:
   ```bash
   docker-compose up -d
   ```

## Adding Your Microservices

### Step 1: Uncomment Service Definition

In `docker-compose.yml`, uncomment and customize one of the example services:

```yaml
service1:
  build:
    context: ../../your-microservices/service1  # Path to your service
    dockerfile: Dockerfile
  container_name: integration-service1
  ports:
    - "3001:3000"  # External:Internal
  environment:
    - NODE_ENV=development
    - AWS_ENDPOINT_URL=http://localstack:4566
    - DATABASE_URL=postgresql://postgres:postgres@postgres:5432/service1_db
    - REDIS_URL=redis://redis:6379
    - SERVICE2_URL=http://service2:8000  # Service discovery
  networks:
    - microservices
  depends_on:
    localstack:
      condition: service_healthy
    postgres:
      condition: service_healthy
```

### Step 2: Configure Database

If your service needs its own database, add it to PostgreSQL environment:

```yaml
postgres:
  environment:
    - POSTGRES_MULTIPLE_DATABASES=service1_db,service2_db,your_new_db
```

### Step 3: Inter-Service Communication

Services can communicate using container names as hostnames:
- `http://service1:3000`
- `http://service2:8000`
- `http://postgres:5432`
- `http://redis:6379`
- `http://localstack:4566`

## Common Commands

```bash
# Start all services
docker-compose up -d

# View logs for all services
docker-compose logs -f

# View logs for specific service
docker-compose logs -f service1

# Restart a service
docker-compose restart service1

# Stop all services
docker-compose down

# Stop and remove volumes (fresh start)
docker-compose down -v

# Rebuild services
docker-compose up -d --build
```

## Testing Integration

### Check Service Health

```bash
# Check all containers
docker-compose ps

# Check LocalStack health
curl http://localhost:4566/_localstack/health

# Check PostgreSQL
docker-compose exec postgres psql -U postgres -c '\l'

# Check Redis
docker-compose exec redis redis-cli ping
```

### Service-to-Service Communication

```bash
# From within a service container
docker-compose exec service1 bash
curl http://service2:8000/api/health
```

## Debugging

### View Service Logs

```bash
docker-compose logs -f service1
```

### Access Service Shell

```bash
docker-compose exec service1 bash
```

### Check Network Connectivity

```bash
docker-compose exec service1 ping service2
docker-compose exec service1 curl http://localstack:4566/_localstack/health
```

## Environment Variables

Each service should configure:
- AWS endpoint pointing to LocalStack
- Database URLs pointing to shared PostgreSQL
- Redis URLs pointing to shared Redis
- URLs for other services they need to communicate with

## File Structure

```
integration/
├── docker-compose.yml
├── .env
└── README.md
```

## Best Practices

1. **Service Independence**: Each service should work independently when possible
2. **Health Checks**: Implement health check endpoints in your services
3. **Environment Variables**: Use env vars for all configuration
4. **Database Migrations**: Run migrations on container startup
5. **Graceful Shutdown**: Handle SIGTERM signals properly
6. **Logging**: Log to stdout/stderr for docker-compose logs

## Troubleshooting

### Services can't communicate
- Check they're on the same network: `docker network inspect microservices-network`
- Verify container names are correct
- Check firewall/port configurations

### Database connection issues
- Ensure PostgreSQL is healthy: `docker-compose ps`
- Check database exists: `docker-compose exec postgres psql -U postgres -l`
- Verify connection string format

### LocalStack not working
- Check health endpoint: `curl http://localhost:4566/_localstack/health`
- View logs: `docker-compose logs localstack`
- Ensure Docker socket is mounted correctly

## Next Steps

- [LocalStack Configuration Guide](../docs/localstack-guide.md)
- [Best Practices](../docs/best-practices.md)
