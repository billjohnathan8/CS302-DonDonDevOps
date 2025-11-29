package com.dondondevops.inventory.model;

import java.time.Instant;
import java.util.UUID;

import io.micronaut.serde.annotation.Serdeable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Serdeable
@DynamoDbBean
public class Product {
    
    private UUID productID;
    private String Name;
    private String Category;
    private String Brand;
    private int Stock;
    private double PriceInSGD;
    private Instant expiryDate;
    private Instant createdAt;
    private Instant updatedAt;


    public Product() {}

    public Product(ProductBuilder builder) {
        this.productID = UUID.randomUUID();
        this.Name = builder.name;
        this.Brand = builder.brand;
        this.Category = builder.category;
        this.Stock = builder.stock;
        this.PriceInSGD = builder.price;
        this.expiryDate = builder.expiry;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @DynamoDbPartitionKey
    public UUID getProductID() {
        return productID;
    }

    public void setProductID(UUID productID) {
        this.productID = productID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public String getBrand() {
        return Brand;
    }

    public void setBrand(String brand) {
        Brand = brand;
    }

    public int getStock() {
        return Stock;
    }

    public void setStock(int stock) {
        Stock = stock;
    }

    public double getPriceInSGD() {
        return PriceInSGD;
    }

    public void setPriceInSGD(double priceInSGD) {
        PriceInSGD = priceInSGD;
    }

    @DynamoDbConvertedBy(InstantZConverter.class)
    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    @DynamoDbConvertedBy(InstantZConverter.class)
    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @DynamoDbConvertedBy(InstantZConverter.class)
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Product [productID=" + productID + ", Name=" + Name + ", Category=" + Category + ", Brand=" + Brand
                + ", Stock=" + Stock + ", PriceInSGD=" + PriceInSGD + ", expiryDate=" + expiryDate + ", createdAt="
                + createdAt + ", updatedAt=" + updatedAt + "]";
    }

    public static ProductBuilder builder() {
        return new ProductBuilder();
    }

    public static class ProductBuilder {

        private String name;
        private String category;
        private String brand;
        private int stock;
        private double price;
        private Instant expiry;

        private ProductBuilder() {}

        public ProductBuilder fromRequest(CreateProductRequest request) {
            name = request.getName();
            brand = request.getBrand();
            category = request.getCategory();
            stock = request.getStock();
            price = request.getPrice();
            expiry = request.getExpiryDate();
            return this;
        }

        public Product build() {
            return new Product(this);
        }
    }
}
