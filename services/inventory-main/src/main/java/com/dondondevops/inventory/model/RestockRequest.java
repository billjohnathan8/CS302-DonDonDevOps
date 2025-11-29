package com.dondondevops.inventory.model;

import java.time.Instant;
import java.util.UUID;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;

@Serdeable
@Valid
public class RestockRequest {

    @NotNull
    private UUID productId;

    @Positive
    private int quantity;

    @Future
    @NotNull
    private Instant expiryDate;

    private String note;

    public RestockRequest(@NotNull UUID productId, @Positive int quantity, @Future Instant expiry,
            String note) {
        this.productId = productId;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.note = note;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    @DynamoDbConvertedBy(InstantZConverter.class)
    public Instant getExpiryDate() {
        return expiryDate;
    }

    public String getNote() {
        return note;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
