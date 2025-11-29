package com.dondondevops.inventory.model;

import java.time.Instant;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

@Serdeable
@Valid
public class CreateProductRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String category;

    @NotBlank
    private String brand;

    @PositiveOrZero
    private int stock;

    @PositiveOrZero
    private double price;

    @Future
    private Instant expiryDate;

    public CreateProductRequest(@NotBlank String name, @NotBlank String category, @NotBlank String brand,
            @PositiveOrZero int stock, @PositiveOrZero double price, @Future Instant expiryDate) {
        this.name = name;
        this.category = category;
        this.brand = brand;
        this.stock = stock;
        this.price = price;
        this.expiryDate = expiryDate;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getBrand() {
        return brand;
    }

    public int getStock() {
        return stock;
    }

    public double getPrice() {
        return price;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

}
