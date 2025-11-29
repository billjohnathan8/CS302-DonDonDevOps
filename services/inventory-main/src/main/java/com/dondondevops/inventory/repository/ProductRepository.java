package com.dondondevops.inventory.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.dondondevops.inventory.model.Product;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Inject;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

@Requires(beans = { DynamoDbClient.class, DynamoDbEnhancedClient.class })
@Bean
public class ProductRepository {

    private DynamoDbClient client;

    private DynamoDbEnhancedClient enhancedClient;

    private DynamoDbTable<Product> table;

    @Inject
    public ProductRepository(@NonNull DynamoDbClient client, @NonNull DynamoDbEnhancedClient enhancedClient) {
        this.client = client;
        this.enhancedClient = enhancedClient;
        initTable();
    }

    private void initTable() {
        table = enhancedClient.table("Product", TableSchema.fromBean(Product.class));

        List<String> tableNames = client.listTables().tableNames();
        boolean tableExists = tableNames.contains("Product");

        if(!tableExists) {
            try {
                table.createTable();
                DynamoDbWaiter waiter = DynamoDbWaiter.builder().client(client).build();
                ResponseOrException<DescribeTableResponse> response = waiter.waitUntilTableExists(b -> b.tableName("Product").build()).matched();
                response.response().orElseThrow(
                    () -> new RuntimeException("Product table was not created."));
            } catch (DynamoDbException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public List<Product> getAll() {
        List<Product> products = new ArrayList<>();
        PageIterable<Product>  productPageIterable = table.scan();

        for(Page<Product> product : productPageIterable) {
            for(Product p : product.items()) {
                products.add(p);
            }
        }
        return products;
    }

    public Optional<Product> getById(UUID id) {
        return Optional.ofNullable(table.getItem(Key.builder().partitionValue(id.toString()).build()));
    }

    public Product save(Product value) {
        table.putItemWithResponse(
                PutItemEnhancedRequest.builder(Product.class).item(value).build());
        return getById(value.getProductID()).get();
    }

    public void delete(Product product) {
        table.deleteItem(product);
    }

    public Product update(Product updatedProduct) {
        return table.updateItem(updatedProduct);
    }
}
