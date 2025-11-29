package org.ddk.promotions.event.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.ddk.promotions.dto.InventoryLowStockEvent;
import org.ddk.promotions.dto.InventoryRestockedEvent;
import org.ddk.promotions.service.LowStockPromotionService;
import org.ddk.promotions.service.RestockPromotionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for InventoryEventListener.
 * Tests the handling of inventory.low_stock and inventory.restocked events.
 */
@ExtendWith(MockitoExtension.class)
class InventoryEventListenerTest {

    @Mock
    private LowStockPromotionService lowStockPromotionService;

    @Mock
    private RestockPromotionService restockPromotionService;

    private InventoryEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new InventoryEventListener(
            lowStockPromotionService,
            restockPromotionService
        );
    }

    // ============= handleLowStock() Tests =============

    @Test
    void handleLowStock_shouldCallLowStockPromotionService() {
        // Given
        UUID productId = UUID.randomUUID();
        int stock = 5;
        int threshold = 10;

        InventoryLowStockEvent event = new InventoryLowStockEvent(
            "inventory.low_stock",
            productId,
            stock,
            threshold,
            Instant.now()
        );

        // When
        listener.handleLowStock(event);

        // Then
        verify(lowStockPromotionService).createLowStockPromotion(
            eq(productId),
            eq(stock),
            eq(threshold)
        );
    }

    @Test
    void handleLowStock_whenServiceThrowsException_shouldPropagateException() {
        // Given
        UUID productId = UUID.randomUUID();
        InventoryLowStockEvent event = new InventoryLowStockEvent(
            "inventory.low_stock",
            productId,
            3,
            10,
            Instant.now()
        );

        doThrow(new RuntimeException("Database connection failed"))
            .when(lowStockPromotionService).createLowStockPromotion(any(), anyInt(), anyInt());

        // When / Then
        assertThrows(RuntimeException.class, () -> listener.handleLowStock(event));
    }

    @Test
    void handleLowStock_shouldHandleMultipleEventsSequentially() {
        // Given
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();

        InventoryLowStockEvent event1 = new InventoryLowStockEvent(
            "inventory.low_stock", productId1, 2, 10, Instant.now()
        );
        InventoryLowStockEvent event2 = new InventoryLowStockEvent(
            "inventory.low_stock", productId2, 1, 5, Instant.now()
        );

        // When
        listener.handleLowStock(event1);
        listener.handleLowStock(event2);

        // Then
        verify(lowStockPromotionService).createLowStockPromotion(eq(productId1), eq(2), eq(10));
        verify(lowStockPromotionService).createLowStockPromotion(eq(productId2), eq(1), eq(5));
    }

    @Test
    void handleLowStock_shouldLogErrorButPropagateException() {
        // Given
        UUID productId = UUID.randomUUID();
        InventoryLowStockEvent event = new InventoryLowStockEvent(
            "inventory.low_stock",
            productId,
            5,
            10,
            Instant.now()
        );

        RuntimeException expectedException = new RuntimeException("Service failure");
        doThrow(expectedException)
            .when(lowStockPromotionService).createLowStockPromotion(any(), anyInt(), anyInt());

        // When / Then
        RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> listener.handleLowStock(event));

        assertEquals(expectedException, thrown);
        verify(lowStockPromotionService).createLowStockPromotion(productId, 5, 10);
    }

    // ============= handleRestocked() Tests =============

    @Test
    void handleRestocked_shouldCallRestockPromotionService() {
        // Given
        UUID productId1 = UUID.randomUUID();

        InventoryRestockedEvent.RestockItem item1 =
            new InventoryRestockedEvent.RestockItem(productId1, 50, 100);

        InventoryRestockedEvent event = new InventoryRestockedEvent(
            "inventory.restocked",
            item1,
            Instant.now()
        );

        when(restockPromotionService.cancelLowStockPromotions(productId1, 100)).thenReturn(1);

        // When
        listener.handleRestocked(event);

        // Then
        verify(restockPromotionService).cancelLowStockPromotions(eq(productId1), eq(100));
    }

    @Test
    void handleRestocked_whenServiceThrowsException_shouldPropagateException() {
        // Given
        UUID productId = UUID.randomUUID();
        InventoryRestockedEvent.RestockItem item =
            new InventoryRestockedEvent.RestockItem(productId, 100, 150);

        InventoryRestockedEvent event = new InventoryRestockedEvent(
            "inventory.restocked",
            item,
            Instant.now()
        );

        doThrow(new RuntimeException("Database error"))
            .when(restockPromotionService).cancelLowStockPromotions(any(), anyInt());

        // When / Then
        assertThrows(RuntimeException.class, () -> listener.handleRestocked(event));
        verify(restockPromotionService).cancelLowStockPromotions(productId, 150);
    }
}
