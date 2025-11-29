package org.ddk.promotions.model;

import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductPromotionTest {
    @Test
    void constructor_and_getters_setters_work() {
        var promoId = Objects.requireNonNull(UUID.randomUUID());
        var productId = Objects.requireNonNull(UUID.randomUUID());
        var pp = new ProductPromotion(promoId, productId);

        assertEquals(promoId, pp.getPromotionId());
        assertEquals(productId, pp.getProductId());
        assertNotNull(pp.getId());

        var newPromoId = Objects.requireNonNull(UUID.randomUUID());
        var newProductId = Objects.requireNonNull(UUID.randomUUID());
        pp.setPromotionId(newPromoId);
        pp.setProductId(newProductId);

        assertEquals(newPromoId, pp.getPromotionId());
        assertEquals(newProductId, pp.getProductId());
    }
}
