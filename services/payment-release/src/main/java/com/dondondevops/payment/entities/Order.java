package com.dondondevops.payment.entities;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.dondondevops.payment.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@AllArgsConstructor
@TableName("Orders")
@DynamoDbBean
public class Order {
    private UUID id;
    private Map<UUID, Integer> items;
    private BigDecimal totalPrice;
    private Receipt receipt;
    
    public Order() {
        this.id = null;
        this.items = new HashMap<>();
        this.totalPrice = BigDecimal.ZERO;
        this.receipt = null;
    }
    
    @DynamoDbPartitionKey
    public UUID getId() {
        return id;
    }
    
    public boolean isPaid() {
        return receipt != null;
    }

    public static class Builder {
        private final Order instance;

        public Builder() {
            this(new Order());
        }
        
        public Builder(@NonNull Order order) {
            instance = order;
        }

        public Order build() {
            if (instance.id == null) {
                instance.id = UUID.randomUUID();
            }
            return instance;
        }

        public Builder withId(@NonNull UUID id) {
            instance.id = id;
            return this;
        }
        
        public Builder addItem(Item item, int quantity) {
            if (quantity < 1) {
                throw new IllegalArgumentException("Quantity must be greater than 0");
            }
            instance.items.put(item.getId(), quantity);
            return this;
        }
        
        public Builder withTotalPrice(BigDecimal totalPrice) {
            if (totalPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Total price cannot be negative");
            }
            instance.totalPrice = totalPrice;
            return this;
        }

        public Builder withTotalPrice(double totalPrice) {
            return withTotalPrice(BigDecimal.valueOf(totalPrice));
        }
    }
}