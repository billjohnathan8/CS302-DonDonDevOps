package com.dondondevops.payment.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import com.dondondevops.payment.controllers.PaymentController.CreatePaymentRequest;
import com.dondondevops.payment.controllers.PaymentController.CreatePaymentResponse;
import com.dondondevops.payment.entities.Item;
import com.dondondevops.payment.repositories.ItemRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.stripe.exception.StripeException;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.main.allow-bean-definition-overriding=true"
)
@EnabledIfSystemProperty(named = "spring.profiles.active", matches = "smoke")
@Import(DynamoDbConfiguration.class)
public class PaymentFlowTest {
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    private static final String PAYMENT_METHOD = "pm_card_mastercard";
    private static final String CURRENCY = "sgd";
    
    @Test
    public void testBuySingleItem() throws JsonProcessingException, StripeException {
        // 1. Create items
        var item = new Item.Builder()
            .withId(UUID.fromString("8fa74ede-9287-4488-bb13-057be76d0b2a"))
            .withName("item1")
            .build();
        itemRepository.save(item);

        // 2. Create an order with these items
        var request = new CreatePaymentRequest(
                Map.of(UUID.fromString("8fa74ede-9287-4488-bb13-057be76d0b2a"), 1),
                PAYMENT_METHOD,
                BigDecimal.TWO,
                CURRENCY
        );

        // 3. Call the payment service to create and confirm a payment intent
        var response = restTemplate.postForEntity(
            "/payments",
            request,
            CreatePaymentResponse.class
        );
        
        // 4. Verify the order is marked as paid and receipt is stored
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        var body = response.getBody();
        assertNotNull(body);

        var itemIds = body.getOrder().getItems().keySet();
        assertIterableEquals(Collections.singleton(item.getId()), itemIds);
    }
}
