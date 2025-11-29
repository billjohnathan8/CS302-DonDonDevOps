import json
import os
import logging
import boto3
from datetime import datetime, timezone

SNS_TOPIC_ARN = os.environ['SNS_TOPIC_ARN']
sns = boto3.client('sns')

logger = logging.getLogger()
logger.setLevel(logging.INFO)

def lambda_handler(event, context):
    logger.info("Raw event: %s", event)

    processed_count = 0

    rmq_queues = event.get('rmqMessagesByQueue', {})
    for queue_key, messages in rmq_queues.items():
        for record in messages:
            try:
                # Decode the Base64-encoded message
                import base64
                body_bytes = base64.b64decode(record['data'])
                message = json.loads(body_bytes)
                
                logger.info("Processing message from %s: %s", queue_key, json.dumps(message, indent=2))
                
                handle_notification_event(message.get("eventType"), message)
                processed_count += 1

            except Exception as e:
                logger.error("Failed to process message: %s", e, exc_info=True)

    logger.info("Processed %d message(s) successfully.", processed_count)
    return {"statusCode": 200, "processed": processed_count}

def timestamp_to_str(ts):
    """Convert UNIX timestamp (seconds) to human-readable string."""
    try:
        return datetime.fromtimestamp(ts, tz=timezone.utc).strftime('%Y-%m-%d %H:%M:%S UTC')
    except Exception as e:
        logger.warning("Failed to convert timestamp: %s", e)
        return str(ts)


def handle_notification_event(event_type, message):
    """
    Process notification events from the queue and send via SNS with nicer formatting.
    """

    # Prepare email content
    if event_type == "promotion.started":
        # Convert UNIX timestamp (seconds) to human-readable string
        start_ts = message.get('startDate')
        start_str = timestamp_to_str(start_ts) if start_ts else None
        end_ts = message.get('endDate')
        end_str = timestamp_to_str(end_ts) if end_ts else None
        subject = "Promotion Started!"
        body = (
            f"Hello,\n\n"
            f"A new promotion has started!\n\n"
            f"Promotion Name: {message.get('name')}\n"
            f"Promotion ID: {message.get('promotionId')}\n"
            f"Start Date: {start_str or message.get('startDate')}\n"
            f"End Date: {end_str or message.get('endDate')}\n"
        )

    elif event_type == "promotion.ended":
        # Convert UNIX timestamp (seconds) to human-readable string
        occurred_at_ts = message.get('occurredAt')
        occurred_at_str = timestamp_to_str(occurred_at_ts) if occurred_at_ts else None
        subject = "Promotion Ended"
        body = (
            f"Hello,\n\n"
            f"The following promotion has ended:\n\n"
            f"Promotion ID: {message.get('promotionId')}\n"
            f"Occurred At: {occurred_at_str or message.get('occurredAt')}\n"
        )

    elif event_type == "promotion.product_updated":
        # Convert UNIX timestamp (seconds) to human-readable string
        occurred_at_ts = message.get('occurredAt')
        occurred_at_str = timestamp_to_str(occurred_at_ts) if occurred_at_ts else None
        subject = "Promotion Updated"
        body = (
            f"Hello,\n\n"
            f"The following promotion has been updated:\n\n"
            f"Promotion ID: {message.get('promotionId')}\n"
            f"Product ID: {message.get('productId')}\n"
            f"Discount Rate: {message.get('discountRate')}\n"
            f"Occurred At: {occurred_at_str or message.get('occurredAt')}\n"
        )

    elif event_type == "inventory.low_stock":
        # Convert UNIX timestamp (seconds) to human-readable string
        occurred_at_ts = message.get('occurredAt')
        occurred_at_str = timestamp_to_str(occurred_at_ts) if occurred_at_ts else None
        subject = "Low Stock Alert"
        body = (
            f"Hello,\n\n"
            f"There is a product with low stock:\n\n"
            f"Product ID: {message.get('productId')}\n"
            f"Current Stock: {message.get('stock')}\n"
            f"Alert Threshold: {message.get('threshold')}\n"
            f"Occurred At: {occurred_at_str or message.get('occurredAt')}\n"
        )

    else:
        logger.warning("Unknown notification event type: %s", event_type)
        return

    # Send the formatted email
    try:
        sns.publish(
            TopicArn=SNS_TOPIC_ARN,
            Subject=subject,
            Message=body
        )
        logger.info("Notification sent via SNS: %s", subject)
    except Exception as e:
        logger.error("Failed to send SNS notification: %s", e)
