package org.ddk.promotions.dto;

import java.util.UUID;

import org.ddk.promotions.model.ProductPromotion;

public record ProductPromotionResponse(UUID id, UUID promotionId, UUID productId) {
    public static ProductPromotionResponse from(ProductPromotion link) {
        return new ProductPromotionResponse(link.getId(), link.getPromotionId(), link.getProductId());
    }
}
