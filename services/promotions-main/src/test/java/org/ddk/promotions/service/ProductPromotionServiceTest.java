package org.ddk.promotions.service;

import org.ddk.promotions.dto.PromotionProductUpdatedEvent;
import org.ddk.promotions.event.publisher.PromotionEventPublisher;
import org.ddk.promotions.model.ProductPromotion;
import org.ddk.promotions.repository.ProductPromotionRepository;
import org.ddk.promotions.store.PromotionStore;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductPromotionService.
 * Tests product-to-promotion attachment, detachment, validation, and event publishing.
 */
class ProductPromotionServiceTest {

    @Test
    void attachProductToPromotion_shouldSuccessfullyAttachProduct() {
        // Arrange
        var repository = mock(ProductPromotionRepository.class);
        var store = mock(PromotionStore.class);
        var eventPublisher = mock(PromotionEventPublisher.class);
        var service = new ProductPromotionService(repository, store, eventPublisher);

        UUID promotionId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        double discountRate = 0.25;

        ProductPromotion savedProductPromotion = new ProductPromotion();
        savedProductPromotion.setId(UUID.randomUUID());
        savedProductPromotion.setPromotionId(promotionId);
        savedProductPromotion.setProductId(productId);

        when(store.promotionExists(promotionId)).thenReturn(true);
        when(repository.save(any(ProductPromotion.class))).thenReturn(savedProductPromotion);

        // Act
        ProductPromotion result = service.attachProductToPromotion(promotionId, productId, discountRate);

        // Assert
        assertNotNull(result);
        assertEquals(promotionId, result.getPromotionId());
        assertEquals(productId, result.getProductId());

        // Verify repository save was called
        verify(repository).save(any(ProductPromotion.class));

        // Verify event was published
        ArgumentCaptor<PromotionProductUpdatedEvent> eventCaptor =
            ArgumentCaptor.forClass(PromotionProductUpdatedEvent.class);
        verify(eventPublisher).publishPromotionProductUpdated(eventCaptor.capture());

        PromotionProductUpdatedEvent publishedEvent = eventCaptor.getValue();
        assertEquals(promotionId, publishedEvent.promotionId());
        assertEquals(productId, publishedEvent.productId());
        assertEquals(discountRate, publishedEvent.discountRate(), 0.001);
        assertEquals("promotion.product_updated", publishedEvent.eventType());
        assertNotNull(publishedEvent.occurredAt());
    }

    @Test
    void attachProductToPromotion_shouldThrowExceptionWhenPromotionIdIsNull() {
        // Arrange
        var repository = mock(ProductPromotionRepository.class);
        var store = mock(PromotionStore.class);
        var eventPublisher = mock(PromotionEventPublisher.class);
        var service = new ProductPromotionService(repository, store, eventPublisher);

        UUID productId = UUID.randomUUID();
        double discountRate = 0.25;

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            service.attachProductToPromotion(null, productId, discountRate);
        });

        // Verify no interactions with repository or event publisher
        verifyNoInteractions(repository);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void attachProductToPromotion_shouldThrowExceptionWhenProductIdIsNull() {
        // Arrange
        var repository = mock(ProductPromotionRepository.class);
        var store = mock(PromotionStore.class);
        var eventPublisher = mock(PromotionEventPublisher.class);
        var service = new ProductPromotionService(repository, store, eventPublisher);

        UUID promotionId = UUID.randomUUID();
        double discountRate = 0.25;

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            service.attachProductToPromotion(promotionId, null, discountRate);
        });

        // Verify no interactions with repository or event publisher
        verifyNoInteractions(repository);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void attachProductToPromotion_shouldThrowExceptionWhenPromotionDoesNotExist() {
        // Arrange
        var repository = mock(ProductPromotionRepository.class);
        var store = mock(PromotionStore.class);
        var eventPublisher = mock(PromotionEventPublisher.class);
        var service = new ProductPromotionService(repository, store, eventPublisher);

        UUID promotionId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        double discountRate = 0.25;

        when(store.promotionExists(promotionId)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.attachProductToPromotion(promotionId, productId, discountRate);
        });

        assertTrue(exception.getMessage().contains("Promotion not found"));
        assertTrue(exception.getMessage().contains(promotionId.toString()));

        // Verify repository save was NOT called
        verify(repository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void attachProductToPromotion_shouldThrowExceptionWhenDiscountRateIsNegative() {
        // Arrange
        var repository = mock(ProductPromotionRepository.class);
        var store = mock(PromotionStore.class);
        var eventPublisher = mock(PromotionEventPublisher.class);
        var service = new ProductPromotionService(repository, store, eventPublisher);

        UUID promotionId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        double invalidDiscountRate = -0.1;

        when(store.promotionExists(promotionId)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.attachProductToPromotion(promotionId, productId, invalidDiscountRate);
        });

        assertTrue(exception.getMessage().contains("Discount rate must be between 0.0 and 1.0"));

        // Verify repository save was NOT called
        verify(repository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void attachProductToPromotion_shouldThrowExceptionWhenDiscountRateIsGreaterThanOne() {
        // Arrange
        var repository = mock(ProductPromotionRepository.class);
        var store = mock(PromotionStore.class);
        var eventPublisher = mock(PromotionEventPublisher.class);
        var service = new ProductPromotionService(repository, store, eventPublisher);

        UUID promotionId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        double invalidDiscountRate = 1.5;

        when(store.promotionExists(promotionId)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.attachProductToPromotion(promotionId, productId, invalidDiscountRate);
        });

        assertTrue(exception.getMessage().contains("Discount rate must be between 0.0 and 1.0"));

        // Verify repository save was NOT called
        verify(repository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void attachProductToPromotion_shouldAcceptZeroDiscountRate() {
        // Arrange
        var repository = mock(ProductPromotionRepository.class);
        var store = mock(PromotionStore.class);
        var eventPublisher = mock(PromotionEventPublisher.class);
        var service = new ProductPromotionService(repository, store, eventPublisher);

        UUID promotionId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        double discountRate = 0.0;

        ProductPromotion savedProductPromotion = new ProductPromotion();
        savedProductPromotion.setId(UUID.randomUUID());
        savedProductPromotion.setPromotionId(promotionId);
        savedProductPromotion.setProductId(productId);

        when(store.promotionExists(promotionId)).thenReturn(true);
        when(repository.save(any(ProductPromotion.class))).thenReturn(savedProductPromotion);

        // Act
        ProductPromotion result = service.attachProductToPromotion(promotionId, productId, discountRate);

        // Assert
        assertNotNull(result);
        verify(repository).save(any(ProductPromotion.class));
        verify(eventPublisher).publishPromotionProductUpdated(any(PromotionProductUpdatedEvent.class));
    }

    @Test
    void attachProductToPromotion_shouldAcceptOneAsDiscountRate() {
        // Arrange
        var repository = mock(ProductPromotionRepository.class);
        var store = mock(PromotionStore.class);
        var eventPublisher = mock(PromotionEventPublisher.class);
        var service = new ProductPromotionService(repository, store, eventPublisher);

        UUID promotionId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        double discountRate = 1.0;

        ProductPromotion savedProductPromotion = new ProductPromotion();
        savedProductPromotion.setId(UUID.randomUUID());
        savedProductPromotion.setPromotionId(promotionId);
        savedProductPromotion.setProductId(productId);

        when(store.promotionExists(promotionId)).thenReturn(true);
        when(repository.save(any(ProductPromotion.class))).thenReturn(savedProductPromotion);

        // Act
        ProductPromotion result = service.attachProductToPromotion(promotionId, productId, discountRate);

        // Assert
        assertNotNull(result);
        verify(repository).save(any(ProductPromotion.class));
        verify(eventPublisher).publishPromotionProductUpdated(any(PromotionProductUpdatedEvent.class));
    }

    @Test
    void detachProductFromPromotion_shouldReturnTrueWhenProductIsDetached() {
        // Arrange
        var repository = mock(ProductPromotionRepository.class);
        var store = mock(PromotionStore.class);
        var eventPublisher = mock(PromotionEventPublisher.class);
        var service = new ProductPromotionService(repository, store, eventPublisher);

        UUID promotionId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID productPromotionId = UUID.randomUUID();

        ProductPromotion existingProductPromotion = new ProductPromotion();
        existingProductPromotion.setId(productPromotionId);
        existingProductPromotion.setPromotionId(promotionId);
        existingProductPromotion.setProductId(productId);

        when(store.findProductPromotions(productId)).thenReturn(List.of(existingProductPromotion));

        // Act
        boolean result = service.detachProductFromPromotion(promotionId, productId);

        // Assert
        assertTrue(result, "Should return true when product is detached");
        verify(repository).deleteById(productPromotionId);
    }

    @Test
    void detachProductFromPromotion_shouldReturnFalseWhenLinkDoesNotExist() {
        // Arrange
        var repository = mock(ProductPromotionRepository.class);
        var store = mock(PromotionStore.class);
        var eventPublisher = mock(PromotionEventPublisher.class);
        var service = new ProductPromotionService(repository, store, eventPublisher);

        UUID promotionId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        when(store.findProductPromotions(productId)).thenReturn(Collections.emptyList());

        // Act
        boolean result = service.detachProductFromPromotion(promotionId, productId);

        // Assert
        assertFalse(result, "Should return false when link doesn't exist");
        verify(repository, never()).deleteById(any());
    }

    @Test
    void detachProductFromPromotion_shouldReturnFalseWhenPromotionIdDoesNotMatch() {
        // Arrange
        var repository = mock(ProductPromotionRepository.class);
        var store = mock(PromotionStore.class);
        var eventPublisher = mock(PromotionEventPublisher.class);
        var service = new ProductPromotionService(repository, store, eventPublisher);

        UUID promotionId = UUID.randomUUID();
        UUID differentPromotionId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID productPromotionId = UUID.randomUUID();

        ProductPromotion existingProductPromotion = new ProductPromotion();
        existingProductPromotion.setId(productPromotionId);
        existingProductPromotion.setPromotionId(differentPromotionId); // Different promotion
        existingProductPromotion.setProductId(productId);

        when(store.findProductPromotions(productId)).thenReturn(List.of(existingProductPromotion));

        // Act
        boolean result = service.detachProductFromPromotion(promotionId, productId);

        // Assert
        assertFalse(result, "Should return false when promotion ID doesn't match");
        verify(repository, never()).deleteById(any());
    }

    @Test
    void detachProductFromPromotion_shouldThrowExceptionWhenPromotionIdIsNull() {
        // Arrange
        var repository = mock(ProductPromotionRepository.class);
        var store = mock(PromotionStore.class);
        var eventPublisher = mock(PromotionEventPublisher.class);
        var service = new ProductPromotionService(repository, store, eventPublisher);

        UUID productId = UUID.randomUUID();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            service.detachProductFromPromotion(null, productId);
        });

        verifyNoInteractions(repository);
        verifyNoInteractions(store);
    }

    @Test
    void detachProductFromPromotion_shouldThrowExceptionWhenProductIdIsNull() {
        // Arrange
        var repository = mock(ProductPromotionRepository.class);
        var store = mock(PromotionStore.class);
        var eventPublisher = mock(PromotionEventPublisher.class);
        var service = new ProductPromotionService(repository, store, eventPublisher);

        UUID promotionId = UUID.randomUUID();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            service.detachProductFromPromotion(promotionId, null);
        });

        verifyNoInteractions(repository);
        verifyNoInteractions(store);
    }

    @Test
    void detachProductFromPromotion_shouldHandleMultipleProductPromotions() {
        // Arrange
        var repository = mock(ProductPromotionRepository.class);
        var store = mock(PromotionStore.class);
        var eventPublisher = mock(PromotionEventPublisher.class);
        var service = new ProductPromotionService(repository, store, eventPublisher);

        UUID targetPromotionId = UUID.randomUUID();
        UUID otherPromotionId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID targetProductPromotionId = UUID.randomUUID();
        UUID otherProductPromotionId = UUID.randomUUID();

        ProductPromotion targetProductPromotion = new ProductPromotion();
        targetProductPromotion.setId(targetProductPromotionId);
        targetProductPromotion.setPromotionId(targetPromotionId);
        targetProductPromotion.setProductId(productId);

        ProductPromotion otherProductPromotion = new ProductPromotion();
        otherProductPromotion.setId(otherProductPromotionId);
        otherProductPromotion.setPromotionId(otherPromotionId);
        otherProductPromotion.setProductId(productId);

        when(store.findProductPromotions(productId))
            .thenReturn(List.of(otherProductPromotion, targetProductPromotion));

        // Act
        boolean result = service.detachProductFromPromotion(targetPromotionId, productId);

        // Assert
        assertTrue(result, "Should return true when target promotion is found");
        verify(repository).deleteById(targetProductPromotionId);
        verify(repository, never()).deleteById(otherProductPromotionId);
    }
}
