package com.dondondevops.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import com.dondondevops.payment.entities.Item;
import com.dondondevops.payment.entities.Order;
import com.dondondevops.payment.repositories.ItemRepository;
import com.dondondevops.payment.repositories.OrderRepository;
import com.dondondevops.payment.services.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

public class PaymentServiceTests {
    
    @Test
    void createOrder() {
        Item item1 = new Item.Builder()
            .withId(UUID.fromString("2f5c0534-3657-48fd-92b4-e37d7d366050"))
            .withName("item1")
            .build();

        Item item2 = new Item.Builder()
            .withId(UUID.fromString("8fa74ede-9287-4488-bb13-057be76d0b2a"))
            .withName("item2")
            .build();

        OrderRepository orderRepository = mock(OrderRepository.class);
        ItemRepository itemRepository = mock(ItemRepository.class);
        PaymentService paymentService = new PaymentService(orderRepository, itemRepository, mock(SecretManager.class));

        when(itemRepository.findByIds(any())).thenReturn(List.of(item1, item2));
        paymentService.makeOrder(Map.of(
            item1.getId(), 2,
            item2.getId(), 1
        ), BigDecimal.valueOf(40));

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createPaymentIntent() throws StripeException {
        UUID orderId = UUID.randomUUID();
        Order order = new Order.Builder()
            .withId(orderId)
            .withTotalPrice(new BigDecimal(50))
            .build();

        var mockPaymentIntent = mock(PaymentIntent.class);
        mockPaymentIntent.setId("pi_123");
        when(mockPaymentIntent.confirm()).thenReturn(mockPaymentIntent);

        try (MockedStatic<Stripe> stripeMock = mockStatic(Stripe.class);
            var paymentIntentMock = mockStatic(PaymentIntent.class)) {

            ArgumentCaptor<PaymentIntentCreateParams> paramsCaptor = ArgumentCaptor.forClass(PaymentIntentCreateParams.class);
            paymentIntentMock.when(() -> PaymentIntent.create(paramsCaptor.capture())).thenReturn(mockPaymentIntent);
            
            PaymentService paymentService = new PaymentService(mock(OrderRepository.class), mock(ItemRepository.class), mock(SecretManager.class));
            PaymentIntent result = paymentService.createAndConfirmPaymentIntent(order, "pm_card_mastercard");

            assertNotNull(result);
            assertEquals(mockPaymentIntent.getId(), result.getId());
            assertEquals(orderId.toString(), paramsCaptor.getValue().getMetadata().get("order_id"));
            assertEquals(5000L, paramsCaptor.getValue().getAmount());
            assertEquals("sgd", paramsCaptor.getValue().getCurrency());
        }
    }
}
