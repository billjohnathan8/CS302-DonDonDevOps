import pytest
from decimal import Decimal
from uuid import UUID
from app.models.order import Order, OrderItem, PaymentInfo

def test_order_item_validation():
    product_id = UUID("123e4567-e89b-42d3-a456-426614174000")
    
    # Test valid order item
    item = OrderItem(
        product_id=product_id,
        quantity=2,
        unit_price=Decimal("29.99"),
        name="Test Product",
        brand="Test Brand"
    )
    assert str(item.product_id) == "123e4567-e89b-42d3-a456-426614174000"
    assert item.quantity == 2
    assert item.unit_price == Decimal("29.99")
    assert item.name == "Test Product"
    assert item.brand == "Test Brand"

    # Test invalid quantity
    with pytest.raises(ValueError):
        OrderItem(
            product_id=product_id,
            quantity=0,
            unit_price=Decimal("29.99")
        )

    # Test with invalid UUID
    with pytest.raises(ValueError):
        OrderItem(
            product_id="invalid-uuid",
            quantity=1,
            unit_price=Decimal("29.99")
        )

def test_payment_info_validation():
    # Test valid payment info
    payment = PaymentInfo(
        payment_method_id="pm_test_123",
        currency="SGD"
    )
    assert payment.payment_method_id == "pm_test_123"
    assert payment.currency == "SGD"

    # Test invalid currency
    with pytest.raises(ValueError):
        PaymentInfo(
            payment_method_id="pm_test_123",
            currency="INVALID"
        )

def test_order_total_calculation():
    product_id1 = UUID("123e4567-e89b-42d3-a456-426614174000")
    product_id2 = UUID("223e4567-e89b-42d3-a456-426614174000")
    
    order = Order(
        order_id=UUID("323e4567-e89b-42d3-a456-426614174000"),
        items=[
            OrderItem(
                product_id=product_id1,
                quantity=2,
                unit_price=Decimal("10.00"),
                name="Test Product 1",
                brand="Test Brand 1"
            ),
            OrderItem(
                product_id=product_id2,
                quantity=1,
                unit_price=Decimal("15.00"),
                name="Test Product 2",
                brand="Test Brand 2"
            )
        ],
        payment_info=PaymentInfo(
            payment_method_id="pm_test_123",
            currency="SGD"
        ),
        total_amount=Decimal("35.00"),
        final_amount=Decimal("35.00")
    )

    assert order.calculate_total() == Decimal("35.00")

def test_order_discount_validation():
    product_id = UUID("123e4567-e89b-42d3-a456-426614174000")
    
    # Test valid discount
    order = Order(
        order_id=UUID("223e4567-e89b-42d3-a456-426614174000"),
        items=[
            OrderItem(
                product_id=product_id,
                quantity=2,
                unit_price=Decimal("10.00"),
                name="Test Product",
                brand="Test Brand"
            )
        ],
        payment_info=PaymentInfo(
            payment_method_id="pm_test_123",
            currency="SGD"
        ),
        total_amount=Decimal("20.00"),
        discount_amount=Decimal("5.00"),
        final_amount=Decimal("15.00")
    )
    assert order.final_amount == Decimal("15.00")

    # Test invalid discount (greater than total)
    with pytest.raises(ValueError):
        Order(
            order_id=UUID("323e4567-e89b-42d3-a456-426614174000"),
            items=[
                OrderItem(product_id=product_id, quantity=2, unit_price=Decimal("10.00"))
            ],
            payment_info=PaymentInfo(
                payment_method_id="pm_test_123",
                currency="SGD"
            ),
            total_amount=Decimal("20.00"),
            discount_amount=Decimal("25.00"),
            final_amount=Decimal("-5.00")
        )

