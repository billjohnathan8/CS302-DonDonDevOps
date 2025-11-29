package com.dondondevops.inventory.event.dto;

import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(startApplication = false)
class LowStockEventSerializationTest {

    @Inject
    ObjectMapper objectMapper;

    @Test
    @DisplayName("Should serialize LowStockEvent with Instant to JSON")
    void testSerializeLowStockEvent() throws IOException {
        // Arrange
        UUID productId = UUID.randomUUID();
        LowStockEvent event = new LowStockEvent(productId, 5, 10);

        // Act
        String json = objectMapper.writeValueAsString(event);

        // Assert
        assertNotNull(json);
        assertTrue(json.contains("inventory.low_stock"));
        assertTrue(json.contains(productId.toString()));
        assertTrue(json.contains("\"stock\":5"));
        assertTrue(json.contains("\"threshold\":10"));
        assertTrue(json.contains("occurredAt"));

        System.out.println("Serialized JSON: " + json);
    }

    @Test
    @DisplayName("Should deserialize JSON to LowStockEvent with Instant")
    void testDeserializeLowStockEvent() throws IOException {
        // Arrange
        String json = "{\"eventType\":\"inventory.low_stock\",\"productId\":\"d9daebef-6c0b-44c9-b6bc-19847dde432d\",\"stock\":5,\"threshold\":10,\"occurredAt\":\"2025-11-08T12:14:28.171Z\"}";

        // Act
        LowStockEvent event = objectMapper.readValue(json, LowStockEvent.class);

        // Assert
        assertNotNull(event);
        assertEquals("inventory.low_stock", event.getEventType());
        assertEquals(UUID.fromString("d9daebef-6c0b-44c9-b6bc-19847dde432d"), event.getProductId());
        assertEquals(5, event.getStock());
        assertEquals(10, event.getThreshold());
        assertNotNull(event.getOccurredAt());

        System.out.println("Deserialized event: " + event);
    }
}
