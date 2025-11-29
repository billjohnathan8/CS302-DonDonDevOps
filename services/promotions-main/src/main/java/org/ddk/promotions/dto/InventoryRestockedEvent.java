package org.ddk.promotions.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event published by Inventory service when products are restocked.
 * Promotions service listens to this event and cancels active low-stock promotions.
 */
public record InventoryRestockedEvent(
    @JsonProperty("eventType") String eventType,
    @JsonProperty("item") RestockItem item,
    @JsonProperty("occurredAt") Instant occurredAt
) {
    /**
     * Represents a single restocked item.
     */
    public record RestockItem(
        @JsonProperty("productId") UUID productId,
        @JsonProperty("added") int added,
        @JsonProperty("stockAfter") int stockAfter
    ) {}
}
