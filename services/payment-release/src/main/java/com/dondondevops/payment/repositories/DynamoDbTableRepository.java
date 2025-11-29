package com.dondondevops.payment.repositories;

import org.springframework.stereotype.Repository;

import com.dondondevops.payment.TableName;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

@Repository
public abstract class DynamoDbTableRepository<T> {
    protected final Class<T> entityClass;
    protected final String tableName;
    protected final DynamoDbClient client;
    protected final DynamoDbTable<T> table;

    public DynamoDbTableRepository(DynamoDbClient client, DynamoDbEnhancedClient enhancedClient, Class<T> entityClass) {
        this.client = client;
        this.tableName = entityClass.getAnnotation(TableName.class).value();
        var isImmutable = entityClass.isAnnotationPresent(DynamoDbImmutable.class);
        var schema = isImmutable ? TableSchema.fromImmutableClass(entityClass) : TableSchema.fromBean(entityClass);
        this.table = enhancedClient.table(tableName, schema);
        this.entityClass = entityClass;
    }

    public String getTableName() {
        return tableName;
    }

    @PostConstruct
    protected void init() {
        ensureTableExists();
    }

    private void ensureTableExists() {
        try {
            client.describeTable(DescribeTableRequest.builder().tableName(tableName).build());
            System.out.println("Table " + tableName + " already exists");
        } catch (ResourceNotFoundException e) {
            System.out.println("Creating table " + tableName);
            table.createTable();
            DynamoDbWaiter waiter = client.waiter();
            waiter.waitUntilTableExists(DescribeTableRequest.builder().tableName(tableName).build());
            System.out.println("Table " + tableName + " created successfully");
        }
    }

    public T save(T item) {
        return table.updateItem(item);
    }
}
