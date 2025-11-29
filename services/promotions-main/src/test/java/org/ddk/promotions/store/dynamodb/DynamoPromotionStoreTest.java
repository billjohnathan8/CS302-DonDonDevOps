package org.ddk.promotions.store.dynamodb;

import org.ddk.promotions.model.ProductPromotion;
import org.ddk.promotions.model.Promotion;
import org.ddk.promotions.repository.ProductPromotionRepository;
import org.ddk.promotions.repository.PromotionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamoPromotionStoreTest {

    @Mock PromotionRepository promotionRepository;
    @Mock ProductPromotionRepository linkRepository;

    @InjectMocks DynamoPromotionStore store;

    @Test
    void savePromotion_delegates_to_repository() {
        var promotion = new Promotion("Save", Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-02-01T00:00:00Z"), 0.2);
        when(promotionRepository.save(promotion)).thenReturn(promotion);

        assertSame(promotion, store.savePromotion(promotion));
        verify(promotionRepository).save(promotion);
    }

    @Test
    void findPromotion_and_exists_delegate() {
        UUID id = UUID.randomUUID();
        var promotion = new Promotion("Find", Instant.parse("2025-03-01T00:00:00Z"), Instant.parse("2025-04-01T00:00:00Z"), 0.3);
        when(promotionRepository.findById(id)).thenReturn(Optional.of(promotion));
        when(promotionRepository.existsById(id)).thenReturn(true);

        assertSame(promotion, store.findPromotion(id).orElseThrow());
        assertTrue(store.promotionExists(id));
        verify(promotionRepository).findById(id);
        verify(promotionRepository).existsById(id);
    }

    @Test
    void deletePromotion_cleans_up_links_when_present() {
        UUID id = UUID.randomUUID();
        List<ProductPromotion> links = List.of(new ProductPromotion(id, UUID.randomUUID()));
        when(linkRepository.findByPromotionId(id)).thenReturn(links);

        store.deletePromotion(id);

        verify(promotionRepository).deleteById(id);
        verify(linkRepository).findByPromotionId(id);
        verify(linkRepository).deleteAll(links);
    }

    @Test
    void deletePromotion_skips_cleanup_when_no_links() {
        UUID id = UUID.randomUUID();
        when(linkRepository.findByPromotionId(id)).thenReturn(List.of());

        store.deletePromotion(id);

        verify(promotionRepository).deleteById(id);
        verify(linkRepository).findByPromotionId(id);
        verify(linkRepository, never()).deleteAll(anyList());
    }

    @Test
    void findProductPromotions_delegates_to_link_repository() {
        UUID productId = UUID.randomUUID();
        var link = new ProductPromotion(UUID.randomUUID(), productId);
        when(linkRepository.findByProductId(productId)).thenReturn(List.of(link));

        List<ProductPromotion> result = store.findProductPromotions(productId);

        assertEquals(1, result.size());
        assertSame(link, result.get(0));
        verify(linkRepository).findByProductId(productId);
    }
}
