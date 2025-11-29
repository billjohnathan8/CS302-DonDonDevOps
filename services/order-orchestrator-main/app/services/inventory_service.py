from sys import stderr
from typing import List, Dict
import httpx
from fastapi import HTTPException
from decimal import Decimal

from app.models.order import OrderItem, CartItem
from app.config import get_settings

settings = get_settings()
INVENTORY_URL = settings.inventory_service_url.rstrip("/")

async def update_inventory(order_id: str, items: list[OrderItem]) -> dict:
    """
    Update inventory after an order is placed by reducing each item's quantity
    
    Args:
        order_id: Unique order identifier
        items: List of OrderItems to reduce inventory for
        
    Returns:
        Dict with update status
        
    Raises:
        HTTPException: If inventory service is unavailable or returns an error
    """
    
    # For each item, call reduce endpoint with quantity
    async with httpx.AsyncClient() as client:
        try:
            for item in items:
                resp = await client.post(
                    f"{INVENTORY_URL}/api/inventory/reduce-stock/{item.product_id}",
                    json={"quantity": item.quantity}
                )
                resp.raise_for_status()
            return {"success": True}
        except httpx.HTTPError as e:
            raise HTTPException(
                status_code=503,
                detail=f"Inventory service error: {str(e)}"
            ) from e

async def pull_stock(items: list[CartItem]) -> dict:
    """
    Check the inventory to ensure that stock is available for items
    
    Args:
        items: List of OrderItems to check stock for
        
    Returns:
        Dict with check status
    
    Raises:
        HTTPException: If inventory service is unavailable or returns an error
    """
    order_items = [] 
    # For each item, call check endpoint
    async with httpx.AsyncClient() as client:
        URL = f"{INVENTORY_URL}/api/product"
        all_items_stocked = True
        try:
            for item in items:
                resp = await client.get(URL + f"/{item.product_id}")
                resp.raise_for_status() 
                json = resp.json()
                
                order_item = OrderItem(
                    product_id=item.product_id,
                    quantity=item.quantity,
                    unit_price=Decimal(json.get("priceInSGD")),
                    name=json.get("name"),
                    brand=json.get("brand")
                )

                has_stock = json.get("stock") >= item.quantity
                if not has_stock:
                    all_items_stocked = False
                order_items.append((order_item, has_stock))

            if not all_items_stocked:
                return {"success": all_items_stocked, "items": order_items}

            return {"success": all_items_stocked, "items": order_items}

        except httpx.HTTPError as e:
            print(
                "Failed to contact inventory service at", URL + "/{id}",
                file=stderr
            )
            raise HTTPException(
                status_code=503,
                detail=f"Inventory service error: {str(e)}"
            ) from e
