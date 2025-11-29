package org.ddk.promotions.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.ddk.promotions.model.ProductPromotion;
import org.springframework.stereotype.Repository;

import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Repository providing CRUD utilities for {@link ProductPromotion} link entities.
 */
@Repository
public class ProductPromotionRepository extends DynamoDbTableRepository<ProductPromotion> {

    /**
     * Creates the repository with the necessary DynamoDB clients.
     *
     * @param client Low-level DynamoDB client.
     * @param enhancedClient Enhanced client that exposes mapper APIs.
     */
    public ProductPromotionRepository(DynamoDbClient client, DynamoDbEnhancedClient enhancedClient) {
        super(client, enhancedClient, ProductPromotion.class);
    }

    /**
     * Finds a link entity by its identifier.
     *
     * @param id Identifier to look up.
     * @return Matching entity, if present.
     */
    public Optional<ProductPromotion> findById(UUID id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(table.getItem(keyFrom(id)));
    }

    /**
     * Returns every {@link ProductPromotion} stored in the backing table.
     *
     * @return Immutable list of all link entities.
     */
    public List<ProductPromotion> findAll() {
        List<ProductPromotion> results = new ArrayList<>();
        table.scan().items().forEach(results::add);
        return List.copyOf(results);
    }

    /**
     * Retrieves all product links for a given product.
     *
     * @param productId Product identifier.
     * @return List of matching associations.
     */
    public List<ProductPromotion> findByProductId(UUID productId) {
        Objects.requireNonNull(productId, "productId");
        List<ProductPromotion> results = new ArrayList<>();
        table.scan().items().forEach(item -> {
            if (productId.equals(item.getProductId())) {
                results.add(item);
            }
        });
        return results;
    }

    /**
     * Retrieves all link entities tied to a specific promotion.
     *
     * @param promotionId Promotion identifier.
     * @return List of matching associations.
     */
    public List<ProductPromotion> findByPromotionId(UUID promotionId) {
        Objects.requireNonNull(promotionId, "promotionId");
        List<ProductPromotion> results = new ArrayList<>();
        table.scan().items().forEach(item -> {
            if (promotionId.equals(item.getPromotionId())) {
                results.add(item);
            }
        });
        return results;
    }

    /**
     * Deletes a link by its identifier.
     *
     * @param id Identifier to delete.
     */
    public void deleteById(UUID id) {
        Objects.requireNonNull(id, "id");
        table.deleteItem(keyFrom(id));
    }

    /**
     * Deletes every link stored in the table.
     */
    public void deleteAll() {
        table.scan().items().forEach(item -> table.deleteItem(keyFrom(item.getId())));
    }

    /**
     * Deletes the provided collection of link entities.
     *
     * @param links Links to delete.
     */
    public void deleteAll(Collection<ProductPromotion> links) {
        Objects.requireNonNull(links, "links");
        links.forEach(link -> table.deleteItem(keyFrom(link.getId())));
    }

    private Key keyFrom(UUID id) {
        return Key.builder().partitionValue(id.toString()).build();
    }
}
