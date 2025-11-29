package org.ddk.promotions.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.ddk.promotions.model.Promotion;
import org.springframework.stereotype.Repository;

import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Repository that provides CRUD helpers for {@link Promotion} entities.
 */
@Repository
public class PromotionRepository extends DynamoDbTableRepository<Promotion> {

    /**
     * Creates the repository with the configured DynamoDB clients.
     *
     * @param client Low-level DynamoDB client.
     * @param enhancedClient Enhanced client wrapper.
     */
    public PromotionRepository(DynamoDbClient client, DynamoDbEnhancedClient enhancedClient) {
        super(client, enhancedClient, Promotion.class);
    }

    /**
     * Finds a promotion by identifier.
     *
     * @param id Identifier to look up.
     * @return Promotion when found, otherwise empty.
     */
    public Optional<Promotion> findById(UUID id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(table.getItem(keyFrom(id)));
    }

    /**
     * Retrieves every promotion stored in the table.
     *
     * @return List of promotions.
     */
    public List<Promotion> findAll() {
        List<Promotion> out = new ArrayList<>();
        table.scan().items().forEach(out::add);
        return out;
    }

    /**
     * Indicates whether a promotion with the given id exists.
     *
     * @param id Identifier to test.
     * @return {@code true} when present, {@code false} otherwise.
     */
    public boolean existsById(UUID id) {
        Objects.requireNonNull(id, "id");
        return table.getItem(keyFrom(id)) != null;
    }

    /**
     * Deletes a promotion by identifier.
     *
     * @param id Identifier to delete.
     */
    public void deleteById(UUID id) {
        Objects.requireNonNull(id, "id");
        table.deleteItem(keyFrom(id));
    }

    /**
     * Removes every promotion from the table.
     */
    public void deleteAll() {
        table.scan().items().forEach(item -> table.deleteItem(keyFrom(item.getId())));
    }

    private Key keyFrom(UUID id) {
        return Key.builder().partitionValue(id.toString()).build();
    }
}
