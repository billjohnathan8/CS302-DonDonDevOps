package com.dondondevops.inventory.model;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@Valid
@Serdeable
public class ReduceStockRequest {

    @Positive
    private int quantity;

    public ReduceStockRequest(@Positive int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}