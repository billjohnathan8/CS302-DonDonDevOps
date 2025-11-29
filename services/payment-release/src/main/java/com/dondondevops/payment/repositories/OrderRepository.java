package com.dondondevops.payment.repositories;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.dondondevops.payment.entities.Order;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Repository
public class OrderRepository extends DynamoDbTableRepository<Order> {
    
    @Autowired
    public OrderRepository(DynamoDbClient client, DynamoDbEnhancedClient enhancedClient) {
        super(client, enhancedClient, Order.class);
    }
    
    @PostConstruct
    protected void init() {
        super.init();
    }
    
    public Order findById(UUID id) {
        Key key = Key.builder().partitionValue(id.toString()).build();
        return super.table.getItem(key);
    }

    public List<Order> scan(UUID startId, int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("Scan limit must be greater than 0");
        }
        
        var start = findById(startId);

        if (start == null) {
            throw new IllegalArgumentException("Order " + startId + " does not exist");
        }

        if (limit == 1) {
            return List.of(start);
        }

        var pages = super.table.scan(r -> r
                .exclusiveStartKey(
                        Map.of("id", AttributeValue.fromS(startId.toString())))
                .limit(limit - 1));

        return Stream.concat(
                Stream.of(start),
                pages.items().stream().limit(limit - 1))
                .toList();
    }

    public List<Order> scan(UUID startId) {
        return scan(startId, 10);
    }

    public List<Order> scan(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("Scan limit must be greater than 0");
        }

        var pages = super.table.scan(r -> r.limit(limit));
        return pages.items().stream().limit(limit).toList();
    }

    public List<Order> scanAll() {
        return super.table.scan().items().stream().toList();
    }
}
