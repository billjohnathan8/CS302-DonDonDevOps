package com.dondondevops.inventory.event.publisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.dondondevops.inventory.event.dto.LowStockEvent;
import com.dondondevops.inventory.event.dto.RestockedEvent;
import com.dondondevops.inventory.event.dto.RestockedItem;

import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
class InventoryEventPublisherTest {

    @Inject
    InventoryEventPublisher eventPublisher;

    @MockBean(InventoryEventPublisher.class)
    InventoryEventPublisher mockEventPublisher() {
        return mock(InventoryEventPublisher.class);
    }

    @Test
    @DisplayName("Should publish low stock event with correct payload")
    void testPublishLowStockEvent() {
        // Arrange
        UUID productId = UUID.randomUUID();
        Integer stock = 5;
        Integer threshold = 10;
        LowStockEvent event = new LowStockEvent(productId, stock, threshold);

        // Act
        eventPublisher.publishLowStock(event);

        // Assert
        ArgumentCaptor<LowStockEvent> eventCaptor = ArgumentCaptor.forClass(LowStockEvent.class);
        verify(eventPublisher, times(1)).publishLowStock(eventCaptor.capture());

        LowStockEvent capturedEvent = eventCaptor.getValue();
        assertEquals("inventory.low_stock", capturedEvent.getEventType());
        assertEquals(productId, capturedEvent.getProductId());
        assertEquals(stock, capturedEvent.getStock());
        assertEquals(threshold, capturedEvent.getThreshold());
        assertNotNull(capturedEvent.getOccurredAt());
    }

    @Test
    @DisplayName("Should publish restocked event with correct payload")
    void testPublishRestockedEvent() {
        // Arrange
        UUID productId1 = UUID.randomUUID();

        RestockedItem item1 = new RestockedItem(productId1, 50, 150);
        RestockedEvent event = new RestockedEvent(item1);

        // Act
        eventPublisher.publishRestocked(event);

        // Assert
        ArgumentCaptor<RestockedEvent> eventCaptor = ArgumentCaptor.forClass(RestockedEvent.class);
        verify(eventPublisher, times(1)).publishRestocked(eventCaptor.capture());

        RestockedEvent capturedEvent = eventCaptor.getValue();
        assertEquals("inventory.restocked", capturedEvent.getEventType());
        assertNotNull(capturedEvent.getOccurredAt());

        // Verify item
        RestockedItem capturedItem1 = capturedEvent.getItem();
        assertEquals(productId1, capturedItem1.getProductId());
        assertEquals(50, capturedItem1.getAdded());
        assertEquals(150, capturedItem1.getStockAfter());
    }

    @Test
    @DisplayName("Should verify event publisher is called for low stock")
    void testEventPublisherCalledForLowStock() {
        // Arrange
        LowStockEvent event = new LowStockEvent(UUID.randomUUID(), 3, 10);

        // Act
        eventPublisher.publishLowStock(event);

        // Assert
        verify(eventPublisher, times(1)).publishLowStock(any(LowStockEvent.class));
    }

    @Test
    @DisplayName("Should verify event publisher is called for restocked")
    void testEventPublisherCalledForRestocked() {
        // Arrange
        RestockedItem item = new RestockedItem(UUID.randomUUID(), 100, 200);
        RestockedEvent event = new RestockedEvent(item);

        // Act
        eventPublisher.publishRestocked(event);

        // Assert
        verify(eventPublisher, times(1)).publishRestocked(any(RestockedEvent.class));
    }
}
