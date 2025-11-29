package org.ddk.promotions.dto;

import java.time.Instant;
import java.util.UUID;

import org.ddk.promotions.model.Promotion;

/**
 * Response projection representing a promotion associated with a product.
 */
public record ProductPromotionDetailResponse(
    UUID id,
    String name,
    Instant startTime,
    Instant endTime,
    double discountRate
) {
    public static ProductPromotionDetailResponse from(Promotion promotion) {
        return new ProductPromotionDetailResponse(
            promotion.getId(),
            promotion.getName(),
            promotion.getStartTime(),
            promotion.getEndTime(),
            promotion.getDiscountRate()
        );
    }
}
