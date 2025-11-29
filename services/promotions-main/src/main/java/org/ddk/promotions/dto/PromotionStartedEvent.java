package org.ddk.promotions.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a promotion starts or is created as active.
 * Consumed by: Inventory service (for price cache invalidation), Orders service (for cart recalculation).
 */
public record PromotionStartedEvent(
    @JsonProperty("eventType") String eventType,
    @JsonProperty("promotionId") UUID promotionId,
    @JsonProperty("name") String name,
    @JsonProperty("startDate") Instant startDate,
    @JsonProperty("endDate") Instant endDate,
    @JsonProperty("occurredAt") Instant occurredAt
) {
    /**
     * Convenience constructor that sets eventType and occurredAt automatically.
     */
    public PromotionStartedEvent(UUID promotionId, String name, Instant startDate, Instant endDate) {
        this("promotion.started", promotionId, name, startDate, endDate, Instant.now());
    }
}
