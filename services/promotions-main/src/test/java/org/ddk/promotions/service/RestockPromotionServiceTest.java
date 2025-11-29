package org.ddk.promotions.service;

import org.ddk.promotions.model.Promotion;
import org.ddk.promotions.model.ProductPromotion;
import org.ddk.promotions.store.PromotionStore;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RestockPromotionService.
 */
class RestockPromotionServiceTest {

    @Test
    void cancelLowStockPromotions_shouldCancelActiveLowStockPromotion() {
        // Arrange
        var store = mock(PromotionStore.class);
        var promotionService = mock(PromotionService.class);
        var service = new RestockPromotionService(
            store,
            promotionService
        );

        UUID productId = UUID.randomUUID();
        UUID promotionId = UUID.randomUUID();

        // Create low-stock promotion
        Promotion lowStockPromotion = new Promotion();
        lowStockPromotion.setId(promotionId);
        lowStockPromotion.setName("Flash Sale - Low Stock - 5 items left");
        lowStockPromotion.setStartTime(Instant.now().minusSeconds(3600));
        lowStockPromotion.setEndTime(Instant.now().plusSeconds(3600));
        lowStockPromotion.setDiscountRate(0.2);

        // Create product-promotion link
        ProductPromotion pp = new ProductPromotion();
        pp.setPromotionId(promotionId);
        pp.setProductId(productId);

        when(store.findProductPromotions(productId)).thenReturn(List.of(pp));
        when(store.findPromotion(promotionId)).thenReturn(Optional.of(lowStockPromotion));
        when(promotionService.deletePromotion(promotionId)).thenReturn(true);

        // Act
        int result = service.cancelLowStockPromotions(productId, 55);

        // Assert
        assertEquals(1, result, "Should cancel 1 promotion");
        verify(promotionService).deletePromotion(promotionId);
    }

    @Test
    void cancelLowStockPromotions_shouldNotCancelRegularPromotions() {
        // Arrange
        var store = mock(PromotionStore.class);
        var promotionService = mock(PromotionService.class);
        var service = new RestockPromotionService(
            store,
            promotionService
        );

        UUID productId = UUID.randomUUID();
        UUID promotionId = UUID.randomUUID();

        // Create regular promotion (not low-stock)
        Promotion regularPromotion = new Promotion();
        regularPromotion.setId(promotionId);
        regularPromotion.setName("Summer Sale - Regular Promotion");
        regularPromotion.setStartTime(Instant.now().minusSeconds(3600));
        regularPromotion.setEndTime(Instant.now().plusSeconds(3600));
        regularPromotion.setDiscountRate(0.3);

        // Create product-promotion link
        ProductPromotion pp = new ProductPromotion();
        pp.setPromotionId(promotionId);
        pp.setProductId(productId);

        when(store.findProductPromotions(productId)).thenReturn(List.of(pp));
        when(store.findPromotion(promotionId)).thenReturn(Optional.of(regularPromotion));

        // Act
        int result = service.cancelLowStockPromotions(productId, 55);

        // Assert
        assertEquals(0, result, "Should not cancel regular promotions");
        verify(promotionService, never()).deletePromotion(any());
    }

    @Test
    void cancelLowStockPromotions_shouldNotCancelExpiredLowStockPromotions() {
        // Arrange
        var store = mock(PromotionStore.class);
        var promotionService = mock(PromotionService.class);
        var service = new RestockPromotionService(
            store,
            promotionService
        );

        UUID productId = UUID.randomUUID();
        UUID promotionId = UUID.randomUUID();

        // Create expired low-stock promotion
        Promotion expiredPromotion = new Promotion();
        expiredPromotion.setId(promotionId);
        expiredPromotion.setName("Flash Sale - Low Stock - 5 items left");
        expiredPromotion.setStartTime(Instant.now().minusSeconds(7200)); // 2 hours ago
        expiredPromotion.setEndTime(Instant.now().minusSeconds(3600)); // Ended 1 hour ago
        expiredPromotion.setDiscountRate(0.2);

        // Create product-promotion link
        ProductPromotion pp = new ProductPromotion();
        pp.setPromotionId(promotionId);
        pp.setProductId(productId);

        when(store.findProductPromotions(productId)).thenReturn(List.of(pp));
        when(store.findPromotion(promotionId)).thenReturn(Optional.of(expiredPromotion));

        // Act
        int result = service.cancelLowStockPromotions(productId, 55);

        // Assert
        assertEquals(0, result, "Should not cancel already expired promotions");
        verify(promotionService, never()).deletePromotion(any());
    }

    @Test
    void cancelLowStockPromotions_shouldCancelMultipleLowStockPromotions() {
        // Arrange
        var store = mock(PromotionStore.class);
        var promotionService = mock(PromotionService.class);
        var service = new RestockPromotionService(
            store,
            promotionService
        );

        UUID productId = UUID.randomUUID();
        UUID promotionId1 = UUID.randomUUID();
        UUID promotionId2 = UUID.randomUUID();

        // Create two low-stock promotions
        Promotion lowStockPromotion1 = new Promotion();
        lowStockPromotion1.setId(promotionId1);
        lowStockPromotion1.setName("Flash Sale - Low Stock - 5 items left");
        lowStockPromotion1.setStartTime(Instant.now().minusSeconds(3600));
        lowStockPromotion1.setEndTime(Instant.now().plusSeconds(3600));
        lowStockPromotion1.setDiscountRate(0.2);

        Promotion lowStockPromotion2 = new Promotion();
        lowStockPromotion2.setId(promotionId2);
        lowStockPromotion2.setName("Flash Sale - Low Stock - 3 items left");
        lowStockPromotion2.setStartTime(Instant.now().minusSeconds(1800));
        lowStockPromotion2.setEndTime(Instant.now().plusSeconds(3600));
        lowStockPromotion2.setDiscountRate(0.2);

        // Create product-promotion links
        ProductPromotion pp1 = new ProductPromotion();
        pp1.setPromotionId(promotionId1);
        pp1.setProductId(productId);

        ProductPromotion pp2 = new ProductPromotion();
        pp2.setPromotionId(promotionId2);
        pp2.setProductId(productId);

        when(store.findProductPromotions(productId))
            .thenReturn(List.of(pp1, pp2));
        when(store.findPromotion(promotionId1))
            .thenReturn(Optional.of(lowStockPromotion1));
        when(store.findPromotion(promotionId2))
            .thenReturn(Optional.of(lowStockPromotion2));
        when(promotionService.deletePromotion(any())).thenReturn(true);

        // Act
        int result = service.cancelLowStockPromotions(productId, 55);

        // Assert
        assertEquals(2, result, "Should cancel both low-stock promotions");
        verify(promotionService).deletePromotion(promotionId1);
        verify(promotionService).deletePromotion(promotionId2);
    }

    @Test
    void cancelLowStockPromotions_shouldReturnZeroWhenNoPromotionsFound() {
        // Arrange
        var store = mock(PromotionStore.class);
        var promotionService = mock(PromotionService.class);
        var service = new RestockPromotionService(
            store,
            promotionService
        );

        UUID productId = UUID.randomUUID();

        when(store.findProductPromotions(productId)).thenReturn(List.of());

        // Act
        int result = service.cancelLowStockPromotions(productId, 55);

        // Assert
        assertEquals(0, result, "Should return 0 when no promotions found");
        verify(promotionService, never()).deletePromotion(any());
    }

    @Test
    void cancelLowStockPromotions_shouldHandleNullPromotion() {
        // Arrange
        var store = mock(PromotionStore.class);
        var promotionService = mock(PromotionService.class);
        var service = new RestockPromotionService(
            store,
            promotionService
        );

        UUID productId = UUID.randomUUID();
        UUID promotionId = UUID.randomUUID();

        ProductPromotion pp = new ProductPromotion();
        pp.setPromotionId(promotionId);
        pp.setProductId(productId);

        when(store.findProductPromotions(productId)).thenReturn(List.of(pp));
        when(store.findPromotion(promotionId)).thenReturn(Optional.empty());

        // Act
        int result = service.cancelLowStockPromotions(productId, 55);

        // Assert
        assertEquals(0, result, "Should handle null promotion gracefully");
        verify(promotionService, never()).deletePromotion(any());
    }
}
