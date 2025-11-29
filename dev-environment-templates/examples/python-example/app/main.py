"""
Example Python Microservice using FastAPI and LocalStack
Demonstrates AWS S3, DynamoDB, and SQS integration with LocalStack
"""

import os
from datetime import datetime
from typing import Dict, List, Optional

import boto3
from botocore.exceptions import ClientError
from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

# Load environment variables
load_dotenv()

# Initialize FastAPI
app = FastAPI(
    title="Python Example Microservice",
    description="Example microservice demonstrating LocalStack integration",
    version="1.0.0"
)

# AWS Configuration
AWS_ENDPOINT = os.getenv("AWS_ENDPOINT_URL", "http://localstack:4566")
AWS_REGION = os.getenv("AWS_DEFAULT_REGION", "us-east-1")
AWS_ACCESS_KEY = os.getenv("AWS_ACCESS_KEY_ID", "test")
AWS_SECRET_KEY = os.getenv("AWS_SECRET_ACCESS_KEY", "test")

# Initialize AWS clients
s3_client = boto3.client(
    "s3",
    endpoint_url=AWS_ENDPOINT,
    aws_access_key_id=AWS_ACCESS_KEY,
    aws_secret_access_key=AWS_SECRET_KEY,
    region_name=AWS_REGION
)

dynamodb = boto3.resource(
    "dynamodb",
    endpoint_url=AWS_ENDPOINT,
    aws_access_key_id=AWS_ACCESS_KEY,
    aws_secret_access_key=AWS_SECRET_KEY,
    region_name=AWS_REGION
)

sqs_client = boto3.client(
    "sqs",
    endpoint_url=AWS_ENDPOINT,
    aws_access_key_id=AWS_ACCESS_KEY,
    aws_secret_access_key=AWS_SECRET_KEY,
    region_name=AWS_REGION
)


# Pydantic models
class BucketCreate(BaseModel):
    bucket_name: str


class FileUpload(BaseModel):
    bucket_name: str
    file_name: str
    content: str


class QueueMessage(BaseModel):
    queue_name: str
    message: str


class UserCreate(BaseModel):
    user_id: str
    name: str
    email: str


# Health check endpoint
@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {
        "status": "healthy",
        "timestamp": datetime.utcnow().isoformat(),
        "service": "python-example-microservice",
        "aws_endpoint": AWS_ENDPOINT
    }


# Root endpoint
@app.get("/")
async def root():
    """Root endpoint with API information"""
    return {
        "message": "Python Example Microservice",
        "endpoints": {
            "health": "GET /health",
            "s3": {
                "list_buckets": "GET /api/s3/buckets",
                "create_bucket": "POST /api/s3/buckets",
                "upload_file": "POST /api/s3/upload"
            },
            "dynamodb": {
                "create_table": "POST /api/dynamodb/table",
                "create_user": "POST /api/dynamodb/users",
                "get_user": "GET /api/dynamodb/users/{user_id}"
            },
            "sqs": {
                "list_queues": "GET /api/sqs/queues",
                "send_message": "POST /api/sqs/message"
            }
        }
    }


# ==================== S3 Endpoints ====================

@app.get("/api/s3/buckets")
async def list_s3_buckets():
    """List all S3 buckets"""
    try:
        response = s3_client.list_buckets()
        buckets = [bucket["Name"] for bucket in response.get("Buckets", [])]
        return {
            "success": True,
            "count": len(buckets),
            "buckets": buckets
        }
    except ClientError as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/s3/buckets")
async def create_s3_bucket(bucket: BucketCreate):
    """Create a new S3 bucket"""
    try:
        s3_client.create_bucket(Bucket=bucket.bucket_name)
        return {
            "success": True,
            "message": f"Bucket '{bucket.bucket_name}' created successfully"
        }
    except ClientError as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/s3/upload")
async def upload_to_s3(file: FileUpload):
    """Upload a file to S3"""
    try:
        s3_client.put_object(
            Bucket=file.bucket_name,
            Key=file.file_name,
            Body=file.content.encode('utf-8')
        )
        return {
            "success": True,
            "message": f"File '{file.file_name}' uploaded to bucket '{file.bucket_name}'"
        }
    except ClientError as e:
        raise HTTPException(status_code=500, detail=str(e))


# ==================== DynamoDB Endpoints ====================

@app.post("/api/dynamodb/table")
async def create_users_table():
    """Create a DynamoDB users table"""
    try:
        table = dynamodb.create_table(
            TableName="users",
            KeySchema=[
                {"AttributeName": "user_id", "KeyType": "HASH"}
            ],
            AttributeDefinitions=[
                {"AttributeName": "user_id", "AttributeType": "S"},
                {"AttributeName": "email", "AttributeType": "S"}
            ],
            GlobalSecondaryIndexes=[
                {
                    "IndexName": "EmailIndex",
                    "KeySchema": [
                        {"AttributeName": "email", "KeyType": "HASH"}
                    ],
                    "Projection": {"ProjectionType": "ALL"},
                    "ProvisionedThroughput": {
                        "ReadCapacityUnits": 5,
                        "WriteCapacityUnits": 5
                    }
                }
            ],
            ProvisionedThroughput={
                "ReadCapacityUnits": 5,
                "WriteCapacityUnits": 5
            }
        )
        return {
            "success": True,
            "message": "Users table created successfully",
            "table_name": table.table_name
        }
    except ClientError as e:
        if e.response['Error']['Code'] == 'ResourceInUseException':
            return {
                "success": True,
                "message": "Users table already exists"
            }
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/dynamodb/users")
async def create_user(user: UserCreate):
    """Create a new user in DynamoDB"""
    try:
        table = dynamodb.Table("users")
        table.put_item(
            Item={
                "user_id": user.user_id,
                "name": user.name,
                "email": user.email,
                "created_at": datetime.utcnow().isoformat()
            }
        )
        return {
            "success": True,
            "message": f"User '{user.user_id}' created successfully"
        }
    except ClientError as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/dynamodb/users/{user_id}")
async def get_user(user_id: str):
    """Get a user by ID from DynamoDB"""
    try:
        table = dynamodb.Table("users")
        response = table.get_item(Key={"user_id": user_id})

        if "Item" not in response:
            raise HTTPException(status_code=404, detail="User not found")

        return {
            "success": True,
            "user": response["Item"]
        }
    except ClientError as e:
        raise HTTPException(status_code=500, detail=str(e))


# ==================== SQS Endpoints ====================

@app.get("/api/sqs/queues")
async def list_sqs_queues():
    """List all SQS queues"""
    try:
        response = sqs_client.list_queues()
        queues = response.get("QueueUrls", [])
        return {
            "success": True,
            "count": len(queues),
            "queues": queues
        }
    except ClientError as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/sqs/message")
async def send_sqs_message(message: QueueMessage):
    """Send a message to an SQS queue"""
    try:
        # Create queue if it doesn't exist
        response = sqs_client.create_queue(QueueName=message.queue_name)
        queue_url = response["QueueUrl"]

        # Send message
        sqs_client.send_message(
            QueueUrl=queue_url,
            MessageBody=message.message
        )

        return {
            "success": True,
            "message": f"Message sent to queue '{message.queue_name}'",
            "queue_url": queue_url
        }
    except ClientError as e:
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("APP_PORT", 8000))
    uvicorn.run(app, host="0.0.0.0", port=port)
