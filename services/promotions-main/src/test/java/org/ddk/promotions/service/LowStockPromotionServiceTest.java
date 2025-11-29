package org.ddk.promotions.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.ddk.promotions.model.Promotion;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for LowStockPromotionService.
 */
class LowStockPromotionServiceTest {

    @Test
    void createLowStockPromotion_shouldCreatePromotionWithCorrectAttributes() {
        // Arrange
        var promotionService = mock(PromotionService.class);
        var productPromotionService = mock(ProductPromotionService.class);
        var service = new LowStockPromotionService(
            promotionService,
            productPromotionService
        );

        UUID productId = UUID.randomUUID();
        UUID promotionId = UUID.randomUUID();
        int stock = 5;
        int threshold = 10;

        // Create mock promotion that will be returned
        Promotion savedPromotion = new Promotion();
        savedPromotion.setId(promotionId);
        savedPromotion.setName("Flash Sale - Low Stock - 5 items left");
        savedPromotion.setStartTime(Instant.now());
        savedPromotion.setEndTime(Instant.now().plus(Duration.ofHours(48)));
        savedPromotion.setDiscountRate(0.2);

        when(promotionService.createPromotion(any(Promotion.class))).thenReturn(savedPromotion);

        // Act
        Promotion result = service.createLowStockPromotion(productId, stock, threshold);

        // Assert
        assertNotNull(result);
        assertEquals(promotionId, result.getId());
        assertTrue(result.getName().startsWith("Flash Sale - Low Stock"));
        assertEquals(0.2, result.getDiscountRate(), 0.001);

        // Verify promotion creation was called
        verify(promotionService).createPromotion(any(Promotion.class));

        // Verify product was attached to promotion
        verify(productPromotionService).attachProductToPromotion(
            eq(promotionId),
            eq(productId),
            eq(0.2)
        );
    }

    @Test
    void createLowStockPromotion_shouldCreatePromotionWith48HourDuration() {
        // Arrange
        var promotionService = mock(PromotionService.class);
        var productPromotionService = mock(ProductPromotionService.class);
        var service = new LowStockPromotionService(
            promotionService,
            productPromotionService
        );

        UUID productId = UUID.randomUUID();
        UUID promotionId = UUID.randomUUID();

        Promotion capturedPromotion = new Promotion();
        capturedPromotion.setId(promotionId);

        when(promotionService.createPromotion(any(Promotion.class))).thenAnswer(invocation -> {
            Promotion promo = invocation.getArgument(0);
            capturedPromotion.setName(promo.getName());
            capturedPromotion.setStartTime(promo.getStartTime());
            capturedPromotion.setEndTime(promo.getEndTime());
            capturedPromotion.setDiscountRate(promo.getDiscountRate());
            return capturedPromotion;
        });

        // Act
        Promotion result = service.createLowStockPromotion(productId, 5, 10);

        // Assert
        assertNotNull(result.getStartTime());
        assertNotNull(result.getEndTime());

        Duration duration = Duration.between(result.getStartTime(), result.getEndTime());
        assertEquals(48, duration.toHours(), "Promotion duration should be 48 hours");
    }

    @Test
    void createLowStockPromotion_shouldCreate20PercentDiscount() {
        // Arrange
        var promotionService = mock(PromotionService.class);
        var productPromotionService = mock(ProductPromotionService.class);
        var service = new LowStockPromotionService(
            promotionService,
            productPromotionService
        );

        UUID productId = UUID.randomUUID();
        UUID promotionId = UUID.randomUUID();

        Promotion savedPromotion = new Promotion();
        savedPromotion.setId(promotionId);
        savedPromotion.setDiscountRate(0.2);

        when(promotionService.createPromotion(any(Promotion.class))).thenReturn(savedPromotion);

        // Act
        Promotion result = service.createLowStockPromotion(productId, 5, 10);

        // Assert
        assertEquals(0.2, result.getDiscountRate(), 0.001,
            "Discount rate should be 20% (0.2)");
    }

    @Test
    void createLowStockPromotion_shouldIncludeStockLevelInName() {
        // Arrange
        var promotionService = mock(PromotionService.class);
        var productPromotionService = mock(ProductPromotionService.class);
        var service = new LowStockPromotionService(
            promotionService,
            productPromotionService
        );

        UUID productId = UUID.randomUUID();
        UUID promotionId = UUID.randomUUID();

        Promotion capturedPromotion = new Promotion();
        capturedPromotion.setId(promotionId);

        when(promotionService.createPromotion(any(Promotion.class))).thenAnswer(invocation -> {
            Promotion promo = invocation.getArgument(0);
            capturedPromotion.setName(promo.getName());
            capturedPromotion.setStartTime(promo.getStartTime());
            capturedPromotion.setEndTime(promo.getEndTime());
            capturedPromotion.setDiscountRate(promo.getDiscountRate());
            return capturedPromotion;
        });

        // Act
        service.createLowStockPromotion(productId, 3, 10);

        // Assert
        assertTrue(capturedPromotion.getName().contains("3 items left"),
            "Promotion name should include stock level");
    }
}
