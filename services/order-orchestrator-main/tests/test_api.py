import pytest
import respx
from httpx import Response
from fastapi import status
from uuid import UUID
from decimal import Decimal

@pytest.fixture
def test_order_data():
    return {
        "order_id": "123e4567-e89b-42d3-a456-426614174000",
        "items": [
            {
                "product_id": "223e4567-e89b-42d3-a456-426614174000",
                "quantity": 2,
                "unit_price": "29.99",
                "name": "Test Product",
                "brand": "Test Brand"
            }
        ],
        "payment_info": {
            "payment_method_id": "pm_test_123",
            "currency": "USD"
        },
        "total_amount": "59.98",
        "final_amount": "59.98",
        }

@respx.mock
def test_create_order_success(client, test_order_data):
    # Mock promotions service /apply (returns per-item discount data)
    respx.post("http://promotions-service:8080/apply").mock(
        return_value=Response(200, json={
            "items": [
                {
                    "productId": "223e4567-e89b-42d3-a456-426614174000",
                    "discountRate": 0.0833333,
                    "discountAmount": 5.00,
                    "finalUnitPrice": 24.99
                }
            ]
        })
    )
    
    # Mock payment service
    respx.post("http://payment-service:8080/payments").mock(
        return_value=Response(200, json={
            "success": True,
            "clientSecret": "test_secret",
            "orderId": "123e4567-e89b-42d3-a456-426614174000",
            "amount": 5998,  # Amount in cents
            "currency": "USD",
            "status": "pending"
        })
    )

    # Mock inventory service reduce
    respx.post("http://inventory-service:8000/api/inventory/reduce/223e4567-e89b-42d3-a456-426614174000").mock(
        return_value=Response(200, json={
            "success": True,
            "updated": True,
            "quantity": 2
        })
    )
    
    response = client.post("/orders/", json=test_order_data)
    assert response.status_code == status.HTTP_200_OK
    data = response.json()
    assert "order_id" in data
    assert data["status"] == "order_placed"

# Note: promotions are applied based on current promotions in the promotions service
# (no promo codes). The invalid-promo test was removed because promo codes are
# no longer part of the orchestrator contract.

