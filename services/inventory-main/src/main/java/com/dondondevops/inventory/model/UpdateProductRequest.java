package com.dondondevops.inventory.model;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;

@Valid
@Serdeable
public class UpdateProductRequest {

    @Nullable
    private String name;

    @Nullable
    private String category;

    @Nullable
    private String brand;

    @PositiveOrZero
    private double price;


    public UpdateProductRequest(@Nullable String name, @Nullable String category, @Nullable String brand,
            @PositiveOrZero double price) {
        this.name = name;
        this.category = category;
        this.brand = brand;
        this.price = price;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
