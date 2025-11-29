"""
Tests for the Python example microservice
"""

import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_health_check():
    """Test the health check endpoint"""
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "healthy"
    assert "timestamp" in data
    assert data["service"] == "python-example-microservice"


def test_root_endpoint():
    """Test the root endpoint"""
    response = client.get("/")
    assert response.status_code == 200
    data = response.json()
    assert "message" in data
    assert "endpoints" in data
    assert data["message"] == "Python Example Microservice"


def test_list_s3_buckets():
    """Test listing S3 buckets"""
    response = client.get("/api/s3/buckets")
    assert response.status_code == 200
    data = response.json()
    assert "success" in data
    assert "buckets" in data
    assert isinstance(data["buckets"], list)


def test_create_s3_bucket():
    """Test creating an S3 bucket"""
    bucket_data = {"bucket_name": "test-bucket"}
    response = client.post("/api/s3/buckets", json=bucket_data)
    assert response.status_code == 200
    data = response.json()
    assert data["success"] is True
    assert "test-bucket" in data["message"]


def test_upload_to_s3():
    """Test uploading a file to S3"""
    # First create a bucket
    client.post("/api/s3/buckets", json={"bucket_name": "upload-test-bucket"})

    # Upload a file
    upload_data = {
        "bucket_name": "upload-test-bucket",
        "file_name": "test.txt",
        "content": "Test content"
    }
    response = client.post("/api/s3/upload", json=upload_data)
    assert response.status_code == 200
    data = response.json()
    assert data["success"] is True


def test_create_users_table():
    """Test creating DynamoDB users table"""
    response = client.post("/api/dynamodb/table")
    assert response.status_code == 200
    data = response.json()
    assert data["success"] is True


def test_create_and_get_user():
    """Test creating and retrieving a user from DynamoDB"""
    # Ensure table exists
    client.post("/api/dynamodb/table")

    # Create user
    user_data = {
        "user_id": "test-123",
        "name": "Test User",
        "email": "test@example.com"
    }
    response = client.post("/api/dynamodb/users", json=user_data)
    assert response.status_code == 200
    data = response.json()
    assert data["success"] is True

    # Get user
    response = client.get("/api/dynamodb/users/test-123")
    assert response.status_code == 200
    data = response.json()
    assert data["success"] is True
    assert data["user"]["user_id"] == "test-123"
    assert data["user"]["name"] == "Test User"


def test_get_nonexistent_user():
    """Test getting a user that doesn't exist"""
    response = client.get("/api/dynamodb/users/nonexistent")
    assert response.status_code == 404


def test_list_sqs_queues():
    """Test listing SQS queues"""
    response = client.get("/api/sqs/queues")
    assert response.status_code == 200
    data = response.json()
    assert "success" in data
    assert "queues" in data


def test_send_sqs_message():
    """Test sending a message to SQS"""
    message_data = {
        "queue_name": "test-queue",
        "message": "Test message"
    }
    response = client.post("/api/sqs/message", json=message_data)
    assert response.status_code == 200
    data = response.json()
    assert data["success"] is True
    assert "test-queue" in data["message"]
