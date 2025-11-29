package com.dondondevops.payment.repositories;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.dondondevops.payment.entities.Product;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Repository
public class ProductRepository extends DynamoDbTableRepository<Product>{
    
    @Autowired
    public ProductRepository(DynamoDbClient client, DynamoDbEnhancedClient enhancedClient) {
        super(client, enhancedClient, Product.class);
    }
    
    @PostConstruct
    protected void init() {
        super.init();
    }
    
    public Product findById(UUID id) {
        var key = Key.builder().partitionValue(id.toString()).build();
        return super.table.getItem(key);
    }
}
