import pytest
from fastapi.testclient import TestClient
from app.main import app
from uuid import UUID

@pytest.fixture
def client():
    return TestClient(app)