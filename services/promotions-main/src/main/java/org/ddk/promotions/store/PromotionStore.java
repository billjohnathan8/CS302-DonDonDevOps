package org.ddk.promotions.store;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.ddk.promotions.model.ProductPromotion;
import org.ddk.promotions.model.Promotion;

/**
 * Abstraction layer for persisting promotions, allowing the application to
 * swap persistence technologies if needed.
 */
public interface PromotionStore {

    List<Promotion> findAllPromotions();

    /**
     * Persists the supplied promotion entity.
     *
     * @param promotion Promotion to store (new or updated).
     * @return Saved promotion instance, potentially with generated fields populated.
     */
    Promotion savePromotion(Promotion promotion);

    /**
     * Loads a promotion by identifier.
     *
     * @param id Unique identifier of the promotion.
     * @return Promotion when found, otherwise empty.
     */
    Optional<Promotion> findPromotion(UUID id);

    /**
     * Checks whether the given promotion id already exists.
     *
     * @param id Promotion identifier to look up.
     * @return {@code true} when a promotion is present, {@code false} otherwise.
     */
    boolean promotionExists(UUID id);

    /**
     * Deletes the promotion with the provided identifier.
     *
     * @param id Promotion identifier to remove.
     */
    void deletePromotion(UUID id);

    /**
     * Retrieves the product/promotion join entities for the supplied product id.
     *
     * @param productId Product identifier whose applicable promotions should be fetched.
     * @return List of product-promotion associations.
     */
    List<ProductPromotion> findProductPromotions(UUID productId);
}
