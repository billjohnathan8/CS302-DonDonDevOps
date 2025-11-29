import sys, os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

# Set fake env vars before importing the Lambda code
os.environ['SNS_TOPIC_ARN'] = 'arn:aws:sns:ap-southeast-1:123456789012:test-topic'
os.environ["AWS_DEFAULT_REGION"] = "ap-southeast-1"

import json
import base64
import pytest
from unittest.mock import patch, MagicMock
import lambda_function


@pytest.fixture(autouse=True)
def mock_env(monkeypatch):
    """Automatically set SNS_TOPIC_ARN for all tests."""
    monkeypatch.setenv("SNS_TOPIC_ARN", "arn:aws:sns:ap-southeast-1:123456789012:test-topic")


@pytest.fixture
def mock_sns_publish():
    """Mock the boto3 SNS publish call."""
    with patch.object(lambda_function, "sns") as mock_sns:
        mock_sns.publish = MagicMock(return_value={"MessageId": "abc123"})
        yield mock_sns.publish


def make_rmq_event(event_type, message):
    """Helper to create fake RabbitMQ payload."""
    encoded_data = base64.b64encode(json.dumps(message).encode()).decode()
    return {
        "rmqMessagesByQueue": {
            "testQueue": [{"data": encoded_data}]
        }
    }


# ---------- TEST CASES ----------

def test_promotion_started_triggers_sns(mock_sns_publish):
    message = {
        "eventType": "promotion.started",
        "promotionId": "123",
        "name": "Holiday Sale",
        "startDate": "2025-12-01T00:00:00Z",
        "endDate": "2025-12-31T23:59:59Z"
    }
    event = make_rmq_event("promotion.started", message)
    result = lambda_function.lambda_handler(event, None)

    mock_sns_publish.assert_called_once()
    call_args = mock_sns_publish.call_args[1]
    assert "Promotion Started" in call_args["Subject"]
    assert "Holiday Sale" in call_args["Message"]
    assert result["processed"] == 1


def test_promotion_ended_converts_timestamp(mock_sns_publish):
    message = {
        "eventType": "promotion.ended",
        "promotionId": "xyz",
        "occurredAt": 1736611200  # UNIX timestamp
    }
    event = make_rmq_event("promotion.ended", message)
    result = lambda_function.lambda_handler(event, None)

    mock_sns_publish.assert_called_once()
    msg_body = mock_sns_publish.call_args[1]["Message"]
    assert "Promotion ID: xyz" in msg_body
    assert "UTC" in msg_body
    assert result["processed"] == 1


def test_product_updated(mock_sns_publish):
    message = {
        "eventType": "promotion.product_updated",
        "promotionId": "p1",
        "productId": "prod123",
        "discountRate": "15%",
        "occurredAt": 1736611200
    }
    event = make_rmq_event("promotion.product_updated", message)
    lambda_function.lambda_handler(event, None)

    mock_sns_publish.assert_called_once()
    body = mock_sns_publish.call_args[1]["Message"]
    assert "prod123" in body
    assert "15%" in body


def test_unknown_event_type_does_not_publish(mock_sns_publish):
    message = {
        "eventType": "unknown.event",
        "promotionId": "p2"
    }
    event = make_rmq_event("unknown.event", message)
    lambda_function.lambda_handler(event, None)

    mock_sns_publish.assert_not_called()


def test_invalid_base64_data_handled_gracefully(mock_sns_publish, caplog):
    event = {
        "rmqMessagesByQueue": {
            "testQueue": [{"data": "invalid_base64"}]
        }
    }
    result = lambda_function.lambda_handler(event, None)

    assert result["processed"] == 0
    mock_sns_publish.assert_not_called()
    assert "Failed to process message" in caplog.text


def test_timestamp_to_str_valid(monkeypatch):
    from datetime import timezone
    ts = 1736611200  # 2025-01-12 00:00:00 UTC
    result = lambda_function.timestamp_to_str(ts)
    assert "UTC" in result
