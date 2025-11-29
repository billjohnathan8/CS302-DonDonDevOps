from enum import Enum
from typing import Optional, List, Dict
from datetime import datetime
from pydantic import BaseModel, Field, field_validator, ConfigDict
from uuid import UUID
from decimal import Decimal
from typing import Union, Any

class OrderStatus(str, Enum):
    PENDING = "PENDING"
    STOCK_CONFIRMED = "STOCK_CONFIRMED"
    PAYMENT_CONFIRMED = "PAYMENT_CONFIRMED"
    COMPLETED = "COMPLETED"
    CANCELLED = "CANCELLED"
    FAILED = "FAILED"

class PaymentInfo(BaseModel):
    payment_method_id: str = Field(..., description="Stripe Payment Method ID")
    currency: str = Field(default="sgd", pattern="^[A-Za-z]{3}$", description="Three-letter currency code")

    model_config = ConfigDict(
        json_encoders={
            Decimal: str
        })

class CartItem(BaseModel):
    product_id: UUID = Field(..., description="Product ID (UUID)")
    quantity: int = Field(..., gt=0, description="Quantity of items")

class OrderItem(BaseModel):
    product_id: UUID = Field(..., description="Product ID (UUID)")
    quantity: int = Field(..., gt=0, description="Quantity of items")
    unit_price: Decimal = Field(..., ge=0, description="Price per unit in SGD")
    name: Optional[str] = Field(None, description="Product name (for display)")
    brand: Optional[str] = Field(None, description="Product brand (for display)")

    @field_validator('quantity')
    @classmethod
    def validate_quantity(cls, v):
        if v <= 0:
            raise ValueError('Quantity must be greater than 0')
        return v

    model_config = ConfigDict(
        json_encoders={
            UUID: str,
            Decimal: str
        })

from typing import Union, Any

class Order(BaseModel):
    status: OrderStatus = Field(default=OrderStatus.PENDING, description="Current order status")
    items: List[OrderItem] = Field(..., min_length=1, description="List of items in the order")
    payment_info: PaymentInfo
    total_amount: Decimal = Field(..., ge=0, description="Total order amount before discount")
    discount_amount: Decimal = Field(default=Decimal('0'), ge=0, description="Discount amount applied")
    final_amount: Decimal = Field(..., ge=0, description="Final amount after discount")
    stripe_payment_intent_id: Optional[str] = Field(None, description="Stripe Payment Intent ID")
    stripe_client_secret: Optional[str] = Field(None, description="Stripe Client Secret for payment confirmation")
    created_at: datetime = Field(default_factory=datetime.utcnow, description="Order creation timestamp")
    updated_at: datetime = Field(default_factory=datetime.utcnow, description="Last update timestamp")
    
    @field_validator('final_amount')
    @classmethod
    def validate_final_amount(cls, v, info):
        data = info.data
        if 'total_amount' in data and 'discount_amount' in data:
            expected = data['total_amount'] - data['discount_amount']
            if v != expected:
                raise ValueError('Final amount must equal total_amount minus discount_amount')
        return v

    @field_validator('discount_amount')
    @classmethod
    def validate_discount_amount(cls, v, info):
        data = info.data
        if 'total_amount' in data and v > data['total_amount']:
            raise ValueError('Discount amount cannot be greater than total amount')
        return v

    def to_cart(self) -> Dict[str, int]:
        """Convert order items to cart format for payment service"""
        return {
            str(item.product_id): item.quantity
            for item in self.items
        }

    def to_restock_request(self) -> Dict:
        """Convert order items to restock request format"""
        return {
            "items": [
                {
                    "productId": str(item.product_id),
                    "quantity": -item.quantity  # Negative quantity for sales
                }
                for item in self.items
            ]
        }

    def calculate_total(self) -> Decimal:
        """Calculate the total amount from order items"""
        return sum(item.unit_price * item.quantity for item in self.items)

    model_config = ConfigDict(
        json_encoders={
            datetime: lambda v: v.isoformat(),
            Decimal: str,
            UUID: str
        })
        
class OrderResponse(BaseModel):
    order_id: UUID
    status: OrderStatus
    total_amount: Decimal
    discount_amount: Decimal
    final_amount: Decimal
    stripe_client_secret: Optional[str] = None
    message: Optional[str] = None

    model_config = ConfigDict(
        json_encoders={
            UUID: str,
            Decimal: str
        })
