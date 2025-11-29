package com.dondondevops.inventory.event.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Event published when product stock falls below threshold.
 * Consumed by Promotions service to auto-create flash sale promotions.
 */
@Serdeable
public class LowStockEvent {

    private String eventType = "inventory.low_stock";
    private UUID productId;
    private Integer stock;
    private Integer threshold;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant occurredAt;

    public LowStockEvent() {
        this.occurredAt = Instant.now();
    }

    public LowStockEvent(UUID productId, Integer stock, Integer threshold) {
        this.productId = productId;
        this.stock = stock;
        this.threshold = threshold;
        this.occurredAt = Instant.now();
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    @Override
    public String toString() {
        return "LowStockEvent{" +
                "eventType='" + eventType + '\'' +
                ", productId=" + productId +
                ", stock=" + stock +
                ", threshold=" + threshold +
                ", occurredAt=" + occurredAt +
                '}';
    }
}
