# LocalStack Configuration Guide

LocalStack provides a fully functional local AWS cloud stack for development and testing. This guide covers configuration, usage, and best practices.

## Available Services

LocalStack supports many AWS services including:

| Service | Description |
|---------|-------------|
| S3 | Object storage |
| DynamoDB | NoSQL database |
| SQS | Message queuing |
| SNS | Pub/sub messaging |
| Lambda | Serverless functions |
| API Gateway | API management |
| Secrets Manager | Secrets storage |
| SSM | Parameter store |
| CloudWatch | Monitoring and logs |
| IAM | Identity and access (limited) |
| EC2 | Virtual servers (limited) |
| ECS | Container orchestration |

## Configuration

### Basic Setup

LocalStack is pre-configured in the templates. Configuration is in `docker-compose.yml`:

```yaml
localstack:
  image: localstack/localstack:latest
  ports:
    - "4566:4566"  # Main gateway
  environment:
    - SERVICES=s3,dynamodb,sqs,sns  # Services to enable
    - DEBUG=1                        # Enable debug logging
    - PERSISTENCE=1                  # Persist data between restarts
```

### Customizing Services

Edit `.env` to enable only needed services:

```bash
# Enable specific services
LOCALSTACK_SERVICES=s3,dynamodb,sqs,sns,lambda

# Or enable all services
LOCALSTACK_SERVICES=

# Common combinations:
# Storage: s3
# Database: dynamodb,rds
# Messaging: sqs,sns
# Compute: lambda,ecs
# Full stack: s3,dynamodb,sqs,sns,lambda,apigateway,secretsmanager,ssm
```

### Data Persistence

LocalStack data is persisted in a Docker volume:

```yaml
volumes:
  localstack_data:/var/lib/localstack
```

To reset LocalStack:
```bash
# Remove volume
docker-compose down -v

# Restart
docker-compose up -d
```

## Accessing LocalStack

### Endpoint URL

All AWS SDK calls should point to LocalStack:

```
http://localhost:4566
```

From within Docker containers:
```
http://localstack:4566
```

### Authentication

LocalStack doesn't require real credentials:

```bash
AWS_ACCESS_KEY_ID=test
AWS_SECRET_ACCESS_KEY=test
AWS_DEFAULT_REGION=us-east-1
```

## Usage Examples

### Using AWS CLI

#### Install awslocal (recommended)

The `awslocal` wrapper automatically points to LocalStack:

```bash
# Already installed in dev containers
awslocal s3 ls
awslocal dynamodb list-tables
awslocal sqs list-queues
```

#### Using Standard AWS CLI

```bash
aws --endpoint-url=http://localhost:4566 s3 ls
```

### S3 Examples

```bash
# Create bucket
awslocal s3 mb s3://my-bucket

# List buckets
awslocal s3 ls

# Upload file
awslocal s3 cp file.txt s3://my-bucket/

# Download file
awslocal s3 cp s3://my-bucket/file.txt ./downloaded.txt

# List bucket contents
awslocal s3 ls s3://my-bucket/

# Delete bucket
awslocal s3 rb s3://my-bucket --force
```

### DynamoDB Examples

```bash
# Create table
awslocal dynamodb create-table \
  --table-name users \
  --attribute-definitions \
    AttributeName=userId,AttributeType=S \
  --key-schema \
    AttributeName=userId,KeyType=HASH \
  --provisioned-throughput \
    ReadCapacityUnits=5,WriteCapacityUnits=5

# List tables
awslocal dynamodb list-tables

# Put item
awslocal dynamodb put-item \
  --table-name users \
  --item '{"userId": {"S": "123"}, "name": {"S": "John"}}'

# Get item
awslocal dynamodb get-item \
  --table-name users \
  --key '{"userId": {"S": "123"}}'

# Scan table
awslocal dynamodb scan --table-name users
```

### SQS Examples

```bash
# Create queue
awslocal sqs create-queue --queue-name my-queue

# List queues
awslocal sqs list-queues

# Send message
awslocal sqs send-message \
  --queue-url http://localhost:4566/000000000000/my-queue \
  --message-body "Hello from LocalStack"

# Receive messages
awslocal sqs receive-message \
  --queue-url http://localhost:4566/000000000000/my-queue
```

### SNS Examples

```bash
# Create topic
awslocal sns create-topic --name my-topic

# List topics
awslocal sns list-topics

# Subscribe to topic
awslocal sns subscribe \
  --topic-arn arn:aws:sns:us-east-1:000000000000:my-topic \
  --protocol email \
  --notification-endpoint test@example.com

# Publish message
awslocal sns publish \
  --topic-arn arn:aws:sns:us-east-1:000000000000:my-topic \
  --message "Hello from SNS"
```

### Secrets Manager Examples

```bash
# Create secret
awslocal secretsmanager create-secret \
  --name my-secret \
  --secret-string '{"username":"admin","password":"secret123"}'

# Get secret value
awslocal secretsmanager get-secret-value --secret-id my-secret

# Update secret
awslocal secretsmanager update-secret \
  --secret-id my-secret \
  --secret-string '{"username":"admin","password":"newpassword"}'
```

## Using LocalStack in Code

### Node.js (AWS SDK v3)

```javascript
import { S3Client, ListBucketsCommand } from "@aws-sdk/client-s3";

const s3Client = new S3Client({
  endpoint: "http://localstack:4566",
  region: "us-east-1",
  credentials: {
    accessKeyId: "test",
    secretAccessKey: "test"
  },
  forcePathStyle: true  // Important for S3
});

// List buckets
const command = new ListBucketsCommand({});
const response = await s3Client.send(command);
console.log(response.Buckets);
```

### Node.js (AWS SDK v2)

```javascript
const AWS = require('aws-sdk');

const s3 = new AWS.S3({
  endpoint: 'http://localstack:4566',
  s3ForcePathStyle: true,
  accessKeyId: 'test',
  secretAccessKey: 'test',
  region: 'us-east-1'
});

s3.listBuckets((err, data) => {
  if (err) console.log(err);
  else console.log(data.Buckets);
});
```

### Python (Boto3)

```python
import boto3

# Create S3 client
s3 = boto3.client(
    's3',
    endpoint_url='http://localstack:4566',
    aws_access_key_id='test',
    aws_secret_access_key='test',
    region_name='us-east-1'
)

# List buckets
response = s3.list_buckets()
print(response['Buckets'])

# DynamoDB example
dynamodb = boto3.resource(
    'dynamodb',
    endpoint_url='http://localstack:4566',
    aws_access_key_id='test',
    aws_secret_access_key='test',
    region_name='us-east-1'
)

table = dynamodb.Table('users')
response = table.get_item(Key={'userId': '123'})
```

### Java (AWS SDK v2)

```java
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import java.net.URI;

S3Client s3 = S3Client.builder()
    .endpointOverride(URI.create("http://localstack:4566"))
    .region(Region.US_EAST_1)
    .credentialsProvider(StaticCredentialsProvider.create(
        AwsBasicCredentials.create("test", "test")))
    .build();

ListBucketsResponse response = s3.listBuckets();
response.buckets().forEach(bucket ->
    System.out.println(bucket.name()));
```

## Health Checks

### Check LocalStack Status

```bash
# Health endpoint
curl http://localhost:4566/_localstack/health

# Pretty print with jq
curl -s http://localhost:4566/_localstack/health | jq '.'
```

Expected output:
```json
{
  "services": {
    "s3": "running",
    "dynamodb": "running",
    "sqs": "running"
  },
  "version": "2.x.x"
}
```

### From Makefile

```bash
make health
```

## Initialization Scripts

You can automatically create resources on LocalStack startup.

### Using Init Scripts

1. Create initialization script:
```bash
# shared/localstack/init-aws.sh
#!/bin/bash
awslocal s3 mb s3://my-app-bucket
awslocal dynamodb create-table --table-name users ...
```

2. Mount in docker-compose.yml:
```yaml
localstack:
  volumes:
    - ./shared/localstack/init-aws.sh:/etc/localstack/init/ready.d/init-aws.sh
```

3. Make executable:
```bash
chmod +x shared/localstack/init-aws.sh
```

## Common Patterns

### Environment-Based Configuration

```javascript
// config.js
const awsConfig = {
  region: process.env.AWS_DEFAULT_REGION || 'us-east-1',
  credentials: {
    accessKeyId: process.env.AWS_ACCESS_KEY_ID,
    secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY
  }
};

// Add endpoint for local development
if (process.env.AWS_ENDPOINT_URL) {
  awsConfig.endpoint = process.env.AWS_ENDPOINT_URL;
}

export default awsConfig;
```

### Testing with LocalStack

```javascript
// test-setup.js
beforeAll(async () => {
  // Create test resources
  await s3.createBucket({ Bucket: 'test-bucket' }).promise();
  await dynamodb.createTable({ /* ... */ }).promise();
});

afterAll(async () => {
  // Cleanup
  await s3.deleteBucket({ Bucket: 'test-bucket' }).promise();
  await dynamodb.deleteTable({ TableName: 'test-table' }).promise();
});
```

## Troubleshooting

### LocalStack Not Responding

```bash
# Check container is running
docker-compose ps localstack

# View logs
docker-compose logs localstack

# Restart LocalStack
docker-compose restart localstack
```

### Services Not Available

```bash
# Check which services are enabled
curl -s http://localhost:4566/_localstack/health | jq '.services'

# Update .env to enable service
echo "LOCALSTACK_SERVICES=s3,dynamodb,sqs" >> .env

# Restart
docker-compose down && docker-compose up -d
```

### Connection Refused

Make sure you're using the correct endpoint:
- From host: `http://localhost:4566`
- From container: `http://localstack:4566`

### Persistence Not Working

```bash
# Check volume exists
docker volume ls | grep localstack

# Check PERSISTENCE is enabled
docker-compose config | grep PERSISTENCE
```

## Limitations

LocalStack has some limitations compared to real AWS:

- IAM policies are not fully enforced
- Some advanced features may not be available
- Performance differs from production AWS
- Not all services perfectly emulate AWS behavior

Always test against real AWS before deployment.

## Next Steps

- [Integration Environment Guide](integration-guide.md)
- [Best Practices & Troubleshooting](best-practices.md)
- [LocalStack Documentation](https://docs.localstack.cloud/)
