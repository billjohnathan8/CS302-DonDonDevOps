package org.ddk.promotions.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a product is attached to a promotion or its discount rate changes.
 * Consumed by: Inventory service (for price cache invalidation), Orders service (for cart recalculation).
 */
public record PromotionProductUpdatedEvent(
    @JsonProperty("eventType") String eventType,
    @JsonProperty("promotionId") UUID promotionId,
    @JsonProperty("productId") UUID productId,
    @JsonProperty("discountRate") double discountRate,
    @JsonProperty("occurredAt") Instant occurredAt
) {
    /**
     * Convenience constructor that sets eventType and occurredAt automatically.
     */
    public PromotionProductUpdatedEvent(UUID promotionId, UUID productId, double discountRate) {
        this("promotion.product_updated", promotionId, productId, discountRate, Instant.now());
    }
}
