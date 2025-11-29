# promotions
A Spring Boot (v3.5) Atomic Microservice that exposes RESTful CRUD and "Apply Promotion" HTTP API Endpoints for downstream commerce services in the CS302 G1T8 DonDonDevOps Microservices Web Application. 

It is fitted with extensive testing (with over 80% code and branch coverage) and documentation. There is a strong focus on Test-Driven Development (TDD) to write tests first via FIRST Principles (tested locally using JUnit and Sonarcube on GitLab CI Pipeline) then trusting the CI Pipeline & failing fast to receive fast & frequent feedback, to then write code to fix errors received from fast feedback. This is all done to enable & align with DevOps practices and create strong testing culture.

The service persists data in DynamoDB (AWS SDK v2 enhanced client) and ships a container image via GitLab's Container Registry for Amazon ECS. 

## Tech stack
- Java 21, Spring Boot (web, AMQP, Actuator)
- DynamoDB via `software.amazon.awssdk:dynamodb-enhanced` + AWS SDK default credential chain
- Docker Compose for local orchestration (app + DynamoDB Local + RabbitMQ)

## Dependencies
- Springboot Starter AMQP
- Spring Web
- Springboot Start Rest Client
- Springboot Start Web MVC
- Springboot DevTools
- Springboot Starter Test
- Lombok
- JPA
- JUnit
- testcontainers
- AWS Spring Cloud 
- AWS SDK
- AWS SDK DynamoDB Enhanced

## Prerequisites
- Docker Desktop (or Docker Engine) with Compose v2
- Java 21 + Gradle 8 if you want to run or test outside of Docker
- Open ports: `8080` (HTTP), `8000` (DynamoDB Local), `5672/15672` (RabbitMQ)

## Quickstart (Docker Compose)
The easiest way to set up and run everything locally is via Docker Compose from the repo root:

```bash
docker compose up --build
```

What this does:
- Builds the `promotions` image using the multi-stage `Dockerfile`
- Starts DynamoDB Local (in-memory, shared DB) at `http://localhost:8000`
- Starts RabbitMQ + management UI at `http://localhost:15672`
- Runs the Spring Boot service in the `prod` profile with credentials + endpoint overrides that point to the local containers

Once the logs show `Started PromotionsApplication`, hit `http://localhost:8080/actuator/health` (or any promotion API) to verify the service is healthy. Use `CTRL+C` to stop and `docker compose down -v` to remove containers/volumes.

### Customizing the compose setup
- Edit `compose.yaml` to tweak env vars (e.g., `AWS_REGION`, `SPRING_PROFILES_ACTIVE`)
- Add `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` to expose more actuator endpoints
- If you need persistent DynamoDB data, remove the `-inMemory` flag and add a volume

## Configuration
| Variable | Description | Default |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod` (default) or `dev` if you want the dev-specific overrides. | `prod` |
| `AWS_REGION` | Region used to build the DynamoDB client. | `ap-southeast-1` |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | Credentials for DynamoDB. Use an IAM role in ECS. | unset |
| `DYNAMODB_ENDPOINT` | Optional endpoint override (e.g. DynamoDB Local). | unset |
| `SERVER_PORT` | HTTP port exposed by the container. | `8080` |

## Testing
```bash
./gradlew test
```
Unit tests remain pure JVM tests. Integration tests spin up DynamoDB Local via Testcontainers (profile `it`) so no real AWS access is needed.

## Deployment
- Image is built via the multi-stage `Dockerfile` and pushed by GitLab CI.
- ECS task definitions should inject the AWS region/credentials (typically via task roles).
- Note that due to springboot being used, we are listening on Port:8080 instead of Port:5000.