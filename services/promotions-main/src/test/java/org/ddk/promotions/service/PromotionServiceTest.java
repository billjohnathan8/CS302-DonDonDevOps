package org.ddk.promotions.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.ddk.promotions.dto.PromotionEndedEvent;
import org.ddk.promotions.dto.PromotionStartedEvent;
import org.ddk.promotions.event.publisher.PromotionEventPublisher;
import org.ddk.promotions.model.Promotion;
import org.ddk.promotions.model.ProductPromotion;
import org.ddk.promotions.store.PromotionStore;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Test Class for PromotionService.
 */
class PromotionServiceTest {
    /** 
     * Unit Test. 
     * Try to pick out the highest active discount among multiple promotions. 
     * 
     * @Return Test the Function findByProductId()
     */
    @Test
    void pickHighestActiveDiscount() {
        //Arrange
        //TestDB
        var store = Mockito.mock(PromotionStore.class);
        var eventPublisher = Mockito.mock(PromotionEventPublisher.class);
        var service = new PromotionService(store, eventPublisher);

        //Tested Data
        UUID productId = Objects.requireNonNull(UUID.randomUUID());
        UUID promotionId1 = Objects.requireNonNull(UUID.randomUUID());
        UUID promotionId2 = Objects.requireNonNull(UUID.randomUUID());

        var now = Instant.parse("2025-10-29T12:00:00Z");

        var promo1 = new Promotion();
        promo1.setId(promotionId1);
        promo1.setStartTime(now.minusSeconds(3600));
        promo1.setEndTime(now.plusSeconds(3600));
        promo1.setDiscountRate(0.20);

        var promo2 = new Promotion();
        promo2.setId(promotionId2);
        promo2.setStartTime(now.minusSeconds(3600));
        promo2.setEndTime(now.plusSeconds(3600));
        promo2.setDiscountRate(0.50);

        //Mock findByProductId()
        //Act
        Mockito.when(store.findProductPromotions(productId)).thenReturn(List.of(
                new ProductPromotion() {
                    {
                        setPromotionId(promotionId1);
                        setProductId(productId);
                    }
                },
                new ProductPromotion() {
                    {
                        setPromotionId(promotionId2);
                        setProductId(productId);
                    }
                }));

        Mockito.when(store.findPromotion(promotionId1))
               .thenReturn(Optional.of(promo1));

        Mockito.when(store.findPromotion(promotionId2))
               .thenReturn(Optional.of(promo2));
        
        //Assert
        assertEquals(0.50, service.bestDiscountFor(productId, now).orElseThrow(), 1e-9);
    }

    @Test
    void bestDiscountFor_returns_empty_when_no_promotions() {
        var store = Mockito.mock(PromotionStore.class);
        var eventPublisher = Mockito.mock(PromotionEventPublisher.class);
        var service = new PromotionService(store, eventPublisher);
        UUID productId = Objects.requireNonNull(UUID.randomUUID());
        Instant now = Instant.now();

        Mockito.when(store.findProductPromotions(productId)).thenReturn(List.of());

        assertTrue(service.bestDiscountFor(productId, now).isEmpty(), "no promotions means empty result");
        Mockito.verify(store).findProductPromotions(productId);
        Mockito.verifyNoMoreInteractions(store);
    }

    @Test
    void bestDiscountFor_skips_missing_or_inactive_promotions() {
        var store = Mockito.mock(PromotionStore.class);
        var eventPublisher = Mockito.mock(PromotionEventPublisher.class);
        var service = new PromotionService(store, eventPublisher);
        UUID productId = Objects.requireNonNull(UUID.randomUUID());
        UUID promotionId = Objects.requireNonNull(UUID.randomUUID());
        Instant now = Instant.parse("2025-10-29T12:00:00Z");

        Mockito.when(store.findProductPromotions(productId)).thenReturn(List.of(new ProductPromotion(promotionId, productId)));
        Mockito.when(store.findPromotion(promotionId)).thenReturn(Optional.of(new Promotion(
            "Later",
            now.plusSeconds(60),
            now.plusSeconds(600),
            0.9
        )));

        assertTrue(service.bestDiscountFor(productId, now).isEmpty(), "promotions outside window should not count");
    }

    @Test
    void createPromotion_delegates_to_store() {
        var store = Mockito.mock(PromotionStore.class);
        var eventPublisher = Mockito.mock(PromotionEventPublisher.class);
        var service = new PromotionService(store, eventPublisher);
        var promotion = new Promotion("Test", Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-01-31T23:59:59Z"), 0.2);

        Mockito.when(store.savePromotion(promotion)).thenReturn(promotion);

        assertSame(promotion, service.createPromotion(promotion));
        Mockito.verify(store).savePromotion(promotion);
    }

    @Test
    void replacePromotion_updates_existing_entity() {
        var store = Mockito.mock(PromotionStore.class);
        var eventPublisher = Mockito.mock(PromotionEventPublisher.class);
        var service = new PromotionService(store, eventPublisher);
        UUID promotionId = Objects.requireNonNull(UUID.randomUUID());
        var existing = new Promotion("Old", Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-03-01T00:00:00Z"), 0.1);
        existing.setId(promotionId);
        var updated = new Promotion("New", Instant.parse("2025-02-01T00:00:00Z"), Instant.parse("2025-04-01T00:00:00Z"), 0.4);

        Mockito.when(store.findPromotion(promotionId)).thenReturn(Optional.of(existing));
        Mockito.when(store.savePromotion(existing)).thenReturn(existing);

        Promotion result = service.replacePromotion(promotionId, updated).orElseThrow();

        assertEquals("New", result.getName());
        assertEquals(updated.getStartTime(), result.getStartTime());
        assertEquals(updated.getEndTime(), result.getEndTime());
        assertEquals(0.4, result.getDiscountRate(), 1e-9);
        Mockito.verify(store).savePromotion(existing);
    }

    @Test
    void replacePromotion_returns_empty_when_not_found() {
        var store = Mockito.mock(PromotionStore.class);
        var eventPublisher = Mockito.mock(PromotionEventPublisher.class);
        var service = new PromotionService(store, eventPublisher);
        UUID promotionId = Objects.requireNonNull(UUID.randomUUID());

        Mockito.when(store.findPromotion(promotionId)).thenReturn(Optional.empty());

        assertTrue(service.replacePromotion(promotionId, new Promotion()).isEmpty());
        Mockito.verify(store).findPromotion(promotionId);
        Mockito.verify(store, Mockito.never()).savePromotion(Mockito.any());
    }

    @Test
    void patchPromotion_updates_only_provided_fields() {
        var store = Mockito.mock(PromotionStore.class);
        var eventPublisher = Mockito.mock(PromotionEventPublisher.class);
        var service = new PromotionService(store, eventPublisher);
        UUID promotionId = Objects.requireNonNull(UUID.randomUUID());
        var existing = new Promotion("Orig", Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-03-01T00:00:00Z"), 0.1);
        existing.setId(promotionId);

        Mockito.when(store.findPromotion(promotionId)).thenReturn(Optional.of(existing));
        Mockito.when(store.savePromotion(existing)).thenReturn(existing);

        Instant newStart = Instant.parse("2025-01-15T00:00:00Z");
        Instant newEnd = Instant.parse("2025-04-01T00:00:00Z");
        Promotion patched = service.patchPromotion(promotionId, "Updated", newStart, newEnd, 0.25).orElseThrow();

        assertEquals("Updated", patched.getName());
        assertEquals(newStart, patched.getStartTime());
        assertEquals(newEnd, patched.getEndTime());
        assertEquals(0.25, patched.getDiscountRate(), 1e-9);
        Mockito.verify(store).savePromotion(existing);
    }

    @Test
    void patchPromotion_returns_empty_when_not_found() {
        var store = Mockito.mock(PromotionStore.class);
        var eventPublisher = Mockito.mock(PromotionEventPublisher.class);
        var service = new PromotionService(store, eventPublisher);
        UUID promotionId = Objects.requireNonNull(UUID.randomUUID());

        Mockito.when(store.findPromotion(promotionId)).thenReturn(Optional.empty());

        assertTrue(service.patchPromotion(promotionId, "Name", Instant.now(), Instant.now(), 0.1).isEmpty());
        Mockito.verify(store, Mockito.never()).savePromotion(Mockito.any());
    }

    @Test
    void deletePromotion_returns_false_when_missing() {
        var store = Mockito.mock(PromotionStore.class);
        var eventPublisher = Mockito.mock(PromotionEventPublisher.class);
        var service = new PromotionService(store, eventPublisher);
        UUID promotionId = Objects.requireNonNull(UUID.randomUUID());

        Mockito.when(store.promotionExists(promotionId)).thenReturn(false);

        assertFalse(service.deletePromotion(promotionId));
        Mockito.verify(store, Mockito.never()).deletePromotion(Mockito.any());
    }

    @Test
    void deletePromotion_deletes_when_present() {
        var store = Mockito.mock(PromotionStore.class);
        var eventPublisher = Mockito.mock(PromotionEventPublisher.class);
        var service = new PromotionService(store, eventPublisher);
        UUID promotionId = Objects.requireNonNull(UUID.randomUUID());

        Mockito.when(store.promotionExists(promotionId)).thenReturn(true);

        assertTrue(service.deletePromotion(promotionId));
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        Mockito.verify(store).deletePromotion(idCaptor.capture());
        assertEquals(promotionId, idCaptor.getValue());
    }

    /**
     * Test that createPromotion publishes a PromotionStartedEvent for active promotions.
     */
    @Test
    void createPromotion_shouldPublishStartedEvent_whenPromotionIsActive() {
        // Arrange
        var store = mock(PromotionStore.class);
        var eventPublisher = mock(PromotionEventPublisher.class);
        var service = new PromotionService(store, eventPublisher);

        UUID promotionId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant startTime = now.minusSeconds(3600); // Started 1 hour ago
        Instant endTime = now.plusSeconds(3600);    // Ends in 1 hour

        Promotion promotion = new Promotion();
        promotion.setName("Active Promotion");
        promotion.setStartTime(startTime);
        promotion.setEndTime(endTime);
        promotion.setDiscountRate(0.25);

        Promotion savedPromotion = new Promotion();
        savedPromotion.setId(promotionId);
        savedPromotion.setName("Active Promotion");
        savedPromotion.setStartTime(startTime);
        savedPromotion.setEndTime(endTime);
        savedPromotion.setDiscountRate(0.25);

        when(store.savePromotion(any(Promotion.class))).thenReturn(savedPromotion);

        // Act
        service.createPromotion(promotion);

        // Assert
        verify(eventPublisher).publishPromotionStarted(any(PromotionStartedEvent.class));
    }

    /**
     * Test that deletePromotion publishes a PromotionEndedEvent.
     */
    @Test
    void deletePromotion_shouldPublishEndedEvent_whenPromotionExists() {
        // Arrange
        var store = mock(PromotionStore.class);
        var eventPublisher = mock(PromotionEventPublisher.class);
        var service = new PromotionService(store, eventPublisher);

        UUID promotionId = UUID.randomUUID();
        when(store.promotionExists(promotionId)).thenReturn(true);

        // Act
        boolean result = service.deletePromotion(promotionId);

        // Assert
        assertTrue(result);
        verify(store).deletePromotion(promotionId);
        verify(eventPublisher).publishPromotionEnded(any(PromotionEndedEvent.class));
    }

    /**
     * Test that deletePromotion does not publish event when promotion doesn't exist.
     */
    @Test
    void deletePromotion_shouldNotPublishEvent_whenPromotionDoesNotExist() {
        // Arrange
        var store = mock(PromotionStore.class);
        var eventPublisher = mock(PromotionEventPublisher.class);
        var service = new PromotionService(store, eventPublisher);

        UUID promotionId = UUID.randomUUID();
        when(store.promotionExists(promotionId)).thenReturn(false);

        // Act
        boolean result = service.deletePromotion(promotionId);

        // Assert
        assertFalse(result);
        verify(store, never()).deletePromotion(any());
        verifyNoInteractions(eventPublisher);
    }
}
