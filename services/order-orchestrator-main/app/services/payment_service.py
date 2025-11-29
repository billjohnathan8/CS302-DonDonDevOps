from typing import Dict, Optional
import httpx
from fastapi import HTTPException
from uuid import UUID
from decimal import Decimal

from app.models.order import Order, PaymentInfo
from app.config import get_settings

settings = get_settings()
PAYMENT_SERVICE_URL = settings.payment_service_url.rstrip("/")

async def process_payment(order: Order) -> Dict:
    """
    Process payment for an order using Stripe
    
    Args:
        order: Order object with payment information
        
    Returns:
        Dict containing payment status and Stripe details
        
    Raises:
        HTTPException: If payment service is unavailable or payment fails
    """
    try:
        # Convert order items to cart format expected by payment service
        cart = {
            str(item.product_id): item.quantity
            for item in order.items
        }

        # Include final amount (in cents) and currency so payment service knows how much to charge
        payment_request = {
            "cart": cart,
            "paymentMethodId": order.payment_info.payment_method_id,
            "amount": float(order.final_amount),
            "currency": order.payment_info.currency
        }
        
        async with httpx.AsyncClient() as client:
            response = await client.post(f"{PAYMENT_SERVICE_URL}/payments", json=payment_request)
            response.raise_for_status()
            result = response.json()
            
            # Extract response details
            order_data = result.get("order", {})
            client_secret = result.get("clientSecret")
            
            return {
                "success": True,
                "client_secret": client_secret,
                "order": order_data
            }
            
    except httpx.HTTPError as e:
        raise HTTPException(
            status_code=402,
            detail=f"Payment processing failed: {str(e)}"
        ) from e
