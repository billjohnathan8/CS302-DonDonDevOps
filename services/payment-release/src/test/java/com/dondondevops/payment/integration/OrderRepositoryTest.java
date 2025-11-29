package com.dondondevops.payment.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dondondevops.payment.entities.Item;
import com.dondondevops.payment.entities.Order;
import com.dondondevops.payment.repositories.OrderRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

@SpringBootTest(
    properties = "spring.main.allow-bean-definition-overriding=true"
)
@EnabledIfSystemProperty(named = "spring.profiles.active", matches = "it")
@Import(DynamoDbConfiguration.class)
@EnabledIfDockerAvailable
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void testSaveAndFindOrder() {
        Item item1 = new Item.Builder()
            .withId(UUID.fromString("2f5c0534-3657-48fd-92b4-e37d7d366050"))
            .withName("item1")
            .build();

        Item item2 = new Item.Builder()
            .withId(UUID.fromString("8fa74ede-9287-4488-bb13-057be76d0b2a"))
            .withName("item2")
            .build();

        var order = new Order.Builder()
            .addItem(item1, 2)
            .addItem(item2, 1)
            .withTotalPrice(40.0)
            .build();

        order = orderRepository.save(order);
        Order result = orderRepository.findById(order.getId());
        assertEquals(order.getId(), result.getId());
        assertEquals(order.getTotalPrice(), result.getTotalPrice());
        assertEquals(order.getItems(), result.getItems());
    }
}
