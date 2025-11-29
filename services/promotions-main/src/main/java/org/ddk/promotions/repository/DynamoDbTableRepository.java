package org.ddk.promotions.repository;

import org.ddk.promotions.TableName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

/**
 * Base repository that wires DynamoDB SDK helpers and transparently ensures that
 * the backing table exists before use.
 *
 * @param <T> Entity type handled by the repository.
 */
@Repository
public abstract class DynamoDbTableRepository<T> {
    private static final Logger logger = LoggerFactory.getLogger(DynamoDbTableRepository.class);

    /** Java class representing the DynamoDB entity schema. */
    protected final Class<T> entityClass;
    /** Logical table name resolved from the {@link TableName} annotation. */
    protected final String tableName;
    /** Low-level DynamoDB client used for administrative operations. */
    protected final DynamoDbClient client;
    /** Enhanced table abstraction used for CRUD operations. */
    protected final DynamoDbTable<T> table;

    /**
     * Creates a repository bound to the provided entity class.
     *
     * @param client Low-level DynamoDB client.
     * @param enhancedClient Enhanced client used to obtain table references.
     * @param entityClass Entity type annotated with {@link TableName}.
     */
    protected DynamoDbTableRepository(DynamoDbClient client, DynamoDbEnhancedClient enhancedClient, Class<T> entityClass) {
        this.client = client;
        this.tableName = entityClass.getAnnotation(TableName.class).value();
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(entityClass));
        this.entityClass = entityClass;
    }

    /**
     * Returns the name of the physical table backing this repository as a string.
     * 
     * @return Physical table name backing this repository.
     */
    public String getTableName() {
        return tableName;
    }

    @PostConstruct
    void init() {
        ensureTableExists();
    }

    private void ensureTableExists() {
        try {
            client.describeTable(DescribeTableRequest.builder().tableName(tableName).build());
            logger.info("Table {} already exists", tableName);
        } catch (ResourceNotFoundException e) {
            logger.info("Creating table {}", tableName);
            table.createTable();
            DynamoDbWaiter waiter = client.waiter();
            waiter.waitUntilTableExists(DescribeTableRequest.builder().tableName(tableName).build());
            logger.info("Table {} created successfully", tableName);
        }
    }

    /**
     * Persists (or overwrites) the provided entity instance.
     *
     * @param item Entity to store.
     * @return Same entity instance for chaining.
     */
    public T save(T item) {
        table.putItem(item);
        return item;
    }
}
