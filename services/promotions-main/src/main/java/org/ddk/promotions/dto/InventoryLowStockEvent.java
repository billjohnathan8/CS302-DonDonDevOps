package org.ddk.promotions.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

/**
 * Event published by Inventory service when product stock falls below threshold.
 * Promotions service listens to this event and auto-creates flash sale promotions.
 */
public record InventoryLowStockEvent(
    @JsonProperty("eventType") String eventType,
    @JsonProperty("productId") UUID productId,
    @JsonProperty("stock") int stock,
    @JsonProperty("threshold") int threshold,
    @JsonProperty("occurredAt") Instant occurredAt
) {}
