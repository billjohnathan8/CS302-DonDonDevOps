package com.dondondevops.inventory.event.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.util.UUID;

/**
 * Represents a single item in the restocked event payload.
 */
@Serdeable
public class RestockedItem {

    private UUID productId;
    private Integer added;
    private Integer stockAfter;

    public RestockedItem() {
    }

    public RestockedItem(UUID productId, Integer added, Integer stockAfter) {
        this.productId = productId;
        this.added = added;
        this.stockAfter = stockAfter;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public Integer getAdded() {
        return added;
    }

    public void setAdded(Integer added) {
        this.added = added;
    }

    public Integer getStockAfter() {
        return stockAfter;
    }

    public void setStockAfter(Integer stockAfter) {
        this.stockAfter = stockAfter;
    }

    @Override
    public String toString() {
        return "RestockedItem{" +
                "productId=" + productId +
                ", added=" + added +
                ", stockAfter=" + stockAfter +
                '}';
    }
}
