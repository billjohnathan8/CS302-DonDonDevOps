package com.dondondevops.inventory.event.dto;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Event published when products are restocked.
 * Consumed by Promotions service to cancel low-stock flash sale promotions.
 */
@Serdeable
public class RestockedEvent {

    private String eventType = "inventory.restocked";
    private RestockedItem item;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant occurredAt;

    public RestockedEvent() {
        this.occurredAt = Instant.now();
    }

    public RestockedEvent(RestockedItem item) {
        this.item = item;
        this.occurredAt = Instant.now();
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public RestockedItem getItem() {
        return item;
    }

    public void setItem(RestockedItem item) {
        this.item = item;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    @Override
    public String toString() {
        return "RestockedEvent [eventType=" + eventType + ", item=" + item + ", occurredAt=" + occurredAt + "]";
    }
}
