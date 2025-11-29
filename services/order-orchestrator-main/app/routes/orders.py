from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from app.services.inventory_service import update_inventory, pull_stock
from app.services.promotions_service import apply_promotion
from app.services.payment_service import process_payment
from app.models.order import Order, PaymentInfo, CartItem
from decimal import Decimal

router = APIRouter()

class OrderRequest(BaseModel):
    items: list[CartItem]
    payment_info: PaymentInfo

@router.post("", summary="Create an order (orchestrated)")
async def create_order(req: OrderRequest):
    
    # Merge duplicate items
    item_map = {}
    for item in req.items:
        if item.product_id in item_map:
            item_map[item.product_id].quantity += item.quantity
        else:
            item_map[item.product_id] = item
    req.items = list(item_map.values())
    
    # Check stock
    stock = await pull_stock(req.items)
    items_with_stock = stock.get("items", [])
    if not stock.get("success", False):
        raise HTTPException(
            status_code=400,
            detail=f"Not enough stock for products: {', '.join(
                str(item.product_id)
                for item, has_stock in items_with_stock
                if not has_stock)}"
        )

    a, b = items_with_stock[0]
    items = tuple(item for item, has_stock in items_with_stock if has_stock)
    
    # Apply promotions if any
    # Query promotions service for any applicable discounts for these items
    discount = 0
    promo = await apply_promotion(items)
    if not promo.get("valid", False):
        raise HTTPException(status_code=400, detail="Promotion service returned invalid response")
    discount = Decimal(str(promo.get("discount", 0)))
    # Create order object for payment processing (apply discount if one was returned)
    total_amount = sum(item.quantity * item.unit_price for item in items)
    final_amount = total_amount - discount

    order = Order(
        items=items,
        payment_info=req.payment_info,
        total_amount=total_amount,
        discount_amount=discount,
        final_amount=final_amount
    )
    
    # Process payment for all orders (with or without promo)
    payment = await process_payment(order)
    order_details = payment.get("order")

    if not payment.get("success", False) or order_details is None:
        raise HTTPException(status_code=402, detail="Payment failed")

    order_details["discount"] = discount

    # Update inventory by reducing quantities
    await update_inventory(str(order_details.get("id")), items)

    # Success
    return {
        "order": order_details,
        "clientSecret": payment.get("client_secret")
    }
