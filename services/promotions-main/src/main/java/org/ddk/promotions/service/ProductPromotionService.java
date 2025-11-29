package org.ddk.promotions.service;

import org.ddk.promotions.dto.PromotionProductUpdatedEvent;
import org.ddk.promotions.event.publisher.PromotionEventPublisher;
import org.ddk.promotions.model.ProductPromotion;
import org.ddk.promotions.model.Promotion;
import org.ddk.promotions.repository.ProductPromotionRepository;
import org.ddk.promotions.store.PromotionStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing product-to-promotion attachments.
 * Handles attaching products to promotions with discount rates and publishes events.
 */
@Service
public class ProductPromotionService {

    private final ProductPromotionRepository productPromotionRepository;
    private final PromotionStore store;
    private final PromotionEventPublisher eventPublisher;

    public ProductPromotionService(ProductPromotionRepository productPromotionRepository,
                                  PromotionStore store,
                                  PromotionEventPublisher eventPublisher) {
        this.productPromotionRepository = productPromotionRepository;
        this.store = store;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Returns every stored product/promotion association.
     *
     * @return complete list of {@link ProductPromotion} records.
     */
    public List<ProductPromotion> getAllProductPromotions() {
        return productPromotionRepository.findAll();
    }

    /**
     * Returns a single {@link ProductPromotion} by identifier.
     *
     * @param id identifier of the mapping.
     * @return optional result.
     */
    public Optional<ProductPromotion> getProductPromotion(UUID id) {
        Objects.requireNonNull(id, "id cannot be null");
        return productPromotionRepository.findById(id);
    }

    /**
     * Creates a new mapping between a product and promotion using the persisted
     * discount rate configured on the promotion itself.
     *
     * @param promotionId promotion identifier.
     * @param productId product identifier.
     * @return saved mapping.
     */
    @Transactional
    public ProductPromotion createProductPromotion(UUID promotionId, UUID productId) {
        double discountRate = resolveDiscountRate(promotionId);
        return attachProductToPromotion(promotionId, productId, discountRate);
    }

    /**
     * Replaces the product/promotion relationship for the given identifier.
     *
     * @param linkId mapping identifier to update.
     * @param promotionId new promotion id.
     * @param productId new product id.
     * @return updated mapping if it exists.
     */
    @Transactional
    public Optional<ProductPromotion> updateProductPromotion(UUID linkId, UUID promotionId, UUID productId) {
        Objects.requireNonNull(linkId, "linkId cannot be null");
        Objects.requireNonNull(promotionId, "promotionId cannot be null");
        Objects.requireNonNull(productId, "productId cannot be null");

        double discountRate = resolveDiscountRate(promotionId);

        return productPromotionRepository.findById(linkId).map(existing -> {
            existing.setPromotionId(promotionId);
            existing.setProductId(productId);
            ProductPromotion saved = productPromotionRepository.save(existing);
            publishProductPromotionUpdated(promotionId, productId, discountRate);
            return saved;
        });
    }

    /**
     * Deletes the mapping with the provided identifier.
     *
     * @param id mapping identifier.
     * @return true when a row was removed.
     */
    @Transactional
    public boolean deleteProductPromotion(UUID id) {
        Objects.requireNonNull(id, "id cannot be null");
        return productPromotionRepository.findById(id)
            .map(existing -> {
                productPromotionRepository.deleteById(existing.getId());
                return true;
            })
            .orElse(false);
    }

    /**
     * Fetches mappings for the supplied product id.
     *
     * @param productId product identifier.
     * @return list of mappings.
     */
    public List<ProductPromotion> findProductPromotionsByProduct(UUID productId) {
        Objects.requireNonNull(productId, "productId cannot be null");
        return productPromotionRepository.findByProductId(productId);
    }

    /**
     * Returns the promotion entities linked to the provided product.
     *
     * @param productId product identifier.
     * @return list of promotions.
     */
    public List<Promotion> findPromotionsForProduct(UUID productId) {
        Objects.requireNonNull(productId, "productId cannot be null");

        return productPromotionRepository.findByProductId(productId).stream()
            .map(ProductPromotion::getPromotionId)
            .map(store::findPromotion)
            .flatMap(Optional::stream)
            .collect(Collectors.toList());
    }

    /**
     * Fetches mappings for the supplied promotion id.
     *
     * @param promotionId promotion identifier.
     * @return list of mappings.
     */
    public List<ProductPromotion> findProductPromotionsByPromotion(UUID promotionId) {
        Objects.requireNonNull(promotionId, "promotionId cannot be null");
        return productPromotionRepository.findByPromotionId(promotionId);
    }

    /**
     * Attaches a product to a promotion with a specific discount rate.
     * Publishes a promotion.product_updated event after successful attachment.
     *
     * @param promotionId The promotion to attach the product to
     * @param productId The product to attach
     * @param discountRate The discount rate (0.0 to 1.0)
     * @return The created or updated ProductPromotion
     * @throws IllegalArgumentException if promotion doesn't exist or discount rate is invalid
     */
    @Transactional
    public ProductPromotion attachProductToPromotion(UUID promotionId, UUID productId, double discountRate) {
        Objects.requireNonNull(promotionId, "promotionId cannot be null");
        Objects.requireNonNull(productId, "productId cannot be null");

        // Validate promotion exists
        if (!store.promotionExists(promotionId)) {
            throw new IllegalArgumentException("Promotion not found: " + promotionId);
        }

        // Validate discount rate
        if (discountRate < 0.0 || discountRate > 1.0) {
            throw new IllegalArgumentException("Discount rate must be between 0.0 and 1.0");
        }

        // Create or update the product-promotion link
        ProductPromotion productPromotion = new ProductPromotion();
        productPromotion.setPromotionId(promotionId);
        productPromotion.setProductId(productId);
        // Note: discountRate is stored in Promotion entity, not ProductPromotion

        ProductPromotion saved = productPromotionRepository.save(productPromotion);

        publishProductPromotionUpdated(promotionId, productId, discountRate);

        return saved;
    }

    /**
     * Detaches a product from a promotion.
     *
     * @param promotionId The promotion ID
     * @param productId The product ID to detach
     * @return true if product was detached, false if link didn't exist
     */
    @Transactional
    public boolean detachProductFromPromotion(UUID promotionId, UUID productId) {
        Objects.requireNonNull(promotionId, "promotionId cannot be null");
        Objects.requireNonNull(productId, "productId cannot be null");

        // Find and delete the link
        return store.findProductPromotions(productId).stream()
            .filter(pp -> pp.getPromotionId().equals(promotionId))
            .findFirst()
            .map(pp -> {
                productPromotionRepository.deleteById(pp.getId());
                return true;
            })
            .orElse(false);
    }

    private double resolveDiscountRate(UUID promotionId) {
        Objects.requireNonNull(promotionId, "promotionId cannot be null");
        Promotion promotion = store.findPromotion(promotionId)
            .orElseThrow(() -> new IllegalArgumentException("Promotion not found: " + promotionId));
        return promotion.getDiscountRate();
    }

    private void publishProductPromotionUpdated(UUID promotionId, UUID productId, double discountRate) {
        eventPublisher.publishPromotionProductUpdated(new PromotionProductUpdatedEvent(
            promotionId,
            productId,
            discountRate
        ));
    }
}
