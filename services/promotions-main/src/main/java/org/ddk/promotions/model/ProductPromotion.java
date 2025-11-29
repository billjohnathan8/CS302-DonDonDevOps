package org.ddk.promotions.model;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.Objects;
import java.util.UUID;

import org.ddk.promotions.TableName;

/**
 * Represents a relationship between a promotion and a product targeted by the promotion.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ProductPromotion")
@DynamoDbBean
public class ProductPromotion {
    private UUID id = Objects.requireNonNull(UUID.randomUUID());
    private UUID promotionId;
    private UUID productId;

    /**
     * Convenience constructor for creating lightweight link entities when only the foreign keys
     * are known (the id will be generated automatically).
     *
     * @param promotionId Identifier of the promotion.
     * @param productId Identifier of the product.
     */
    public ProductPromotion(UUID promotionId, UUID productId) {
        this.promotionId = promotionId;
        this.productId = productId;
    }

    /**
     * Returns the ID of the ProductPromotion.
     * 
     * @return The ID of the ProductPromotion as a UUID.
     */
    @DynamoDbPartitionKey
    public UUID getId() {
        return id;
    }

    /**
     * Returns the PromotionID of the Promotion (Foreign Key).
     * 
     * @return The ID of the Promotion as a UUID.
     */
    public UUID getPromotionId() {
        return promotionId;
    }

    /**
     * Sets the ID of the Promotion (Foreign Key).
     * @param promotionId The new ID of the Promotion as a UUID.
     */
    public void setPromotionId(UUID promotionId) {
        this.promotionId = promotionId;
    }

    /**
     * Returns the ProductID of the Product (Foreign Key).
     * 
     * @return The ID of the Product as a UUID.
     */
    public UUID getProductId() {
        return productId;
    }

    /**
     * Sets the ID of the Product (Foreign Key). 
     * @param productId The new ID of the Product as a UUID.
     */
    public void setProductId(UUID productId) {
        this.productId = productId; 
    }

    // No discount rate field: rate is stored in Promotion entity.
}
