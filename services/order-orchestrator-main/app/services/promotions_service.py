from typing import Dict, List
import httpx
from fastapi import HTTPException
from decimal import Decimal
from datetime import datetime

from app.models.order import OrderItem
from app.config import get_settings

settings = get_settings()
PROMOTIONS_SERVICE_URL = settings.promotions_service_url.rstrip("/")


async def apply_promotion(items: List[OrderItem]) -> Dict:
    """
    Query the promotions service for per-item discounts and return an aggregated
    discount amount. The promotions service exposes a POST /apply endpoint which
    returns per-item discountAmount and finalUnitPrice. This function keeps the
    existing return shape {"valid": bool, "discount": Decimal} so callers
    in the orchestrator don't need to change.

    Note: the promotions microservice applies discounts based on active
    promotions and product data; promo codes are not part of the `/apply`
    contract.
    """
    # Build items payload expected by promotions service
    items_data = [
        {
            "productId": str(item.product_id),
            "quantity": int(item.quantity),
            # promotions expects unitPrice as a double (not string)
            "unitPrice": float(item.unit_price)
        }
        for item in items
    ]

    payload = {
        # controller uses current time when `now` is null; include ISO time if desired
        "now": None,
        "items": items_data,
    }

    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(f"{PROMOTIONS_SERVICE_URL}/promotions/apply", json=payload)
            response.raise_for_status()
            result = response.json()

            # result is ApplyResponse: { items: [ { productId, discountRate, discountAmount, finalUnitPrice } ] }
            total_discount = Decimal("0")
            for it in result.get("items", []):
                # discountAmount is a number (double)
                da = it.get("discountAmount", 0)
                total_discount += Decimal(str(da))

            return {"valid": True, "discount": total_discount}

    except httpx.HTTPError as e:
        raise HTTPException(status_code=503, detail=f"Promotion service error: {str(e)}")
    except (ValueError, TypeError) as e:
        raise HTTPException(status_code=400, detail=f"Invalid promotion response: {str(e)}")
