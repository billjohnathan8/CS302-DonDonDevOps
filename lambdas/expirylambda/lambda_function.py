import boto3
import os
from datetime import datetime, timedelta, timezone

dynamodb = boto3.resource('dynamodb')
sns = boto3.client('sns')

TABLE_NAME = os.environ['TABLE_NAME']
SNS_TOPIC_ARN = os.environ['SNS_TOPIC_ARN']

def lambda_handler(event, context):
    table = dynamodb.Table(TABLE_NAME)

    today = datetime.now(timezone.utc)
    one_month_later = today + timedelta(days=30)

    response = table.scan()
    items = response['Items']
    print(f"Scanned {len(items)} items from {TABLE_NAME}")

    expiring_items = []

    for item in items:
        expiry_str = item.get('expiryDate')
        if not expiry_str:
            continue

        try:
            expiry_date = datetime.fromisoformat(expiry_str)
        except ValueError:
            try:
                expiry_date = datetime.strptime(expiry_str, "%Y-%m-%dT%H:%M")
            except Exception as e:
                print(f"Skipping invalid date {expiry_str}: {e}")
                continue

        # Ensure timezone awareness for comparison
        if expiry_date.tzinfo is None:
            expiry_date = expiry_date.replace(tzinfo=timezone.utc)

        if today <= expiry_date <= one_month_later:
            expiring_items.append(item)

    if expiring_items:
        message_lines = ["Products expiring within the next month:\n"]
        for item in expiring_items:
            message_lines.append(
                f"- {item.get('name')} ({item.get('brand')}) expires on {item.get('expiryDate')}"
            )
        message = "\n".join(message_lines)

        sns.publish(
            TopicArn=SNS_TOPIC_ARN,
            Subject="DynamoDB Inventory: Upcoming Expiries",
            Message=message
        )
        print("Sent alert:", message)
    else:
        print("No expiring items found.")
