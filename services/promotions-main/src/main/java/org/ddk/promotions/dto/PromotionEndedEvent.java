package org.ddk.promotions.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a promotion ends (expired or deleted).
 * Consumed by: Inventory service (for price cache invalidation), Orders service (for cart recalculation).
 */
public record PromotionEndedEvent(
    @JsonProperty("eventType") String eventType,
    @JsonProperty("promotionId") UUID promotionId,
    @JsonProperty("occurredAt") Instant occurredAt
) {
    /**
     * Convenience constructor that sets eventType and occurredAt automatically.
     */
    public PromotionEndedEvent(UUID promotionId) {
        this("promotion.ended", promotionId, Instant.now());
    }
}
