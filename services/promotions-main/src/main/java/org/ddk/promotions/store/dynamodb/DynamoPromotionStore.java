package org.ddk.promotions.store.dynamodb;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.ddk.promotions.model.ProductPromotion;
import org.ddk.promotions.model.Promotion;
import org.ddk.promotions.repository.ProductPromotionRepository;
import org.ddk.promotions.repository.PromotionRepository;
import org.ddk.promotions.store.PromotionStore;
import org.springframework.stereotype.Component;

/**
 * DynamoDB-backed implementation of the {@link PromotionStore} abstraction.
 */
@Component
public class DynamoPromotionStore implements PromotionStore {

    private final PromotionRepository promotionRepository;
    private final ProductPromotionRepository linkRepository;

    /**
     * Creates a new store that orchestrates promotion and linking repositories.
     *
     * @param promotionRepository Repository for Promotion entities.
     * @param linkRepository Repository for ProductPromotion relations.
     */
    public DynamoPromotionStore(PromotionRepository promotionRepository, ProductPromotionRepository linkRepository) {
        this.promotionRepository = promotionRepository;
        this.linkRepository = linkRepository;
    }

    @Override
    public List<Promotion> findAllPromotions() {
        return promotionRepository.findAll();
    }

    @Override
    public Promotion savePromotion(Promotion promotion) {
        return promotionRepository.save(Objects.requireNonNull(promotion));
    }

    @Override
    public Optional<Promotion> findPromotion(UUID id) {
        return promotionRepository.findById(Objects.requireNonNull(id));
    }

    @Override
    public boolean promotionExists(UUID id) {
        return promotionRepository.existsById(Objects.requireNonNull(id));
    }

    @Override
    public void deletePromotion(UUID id) {
        UUID promotionId = Objects.requireNonNull(id);
        promotionRepository.deleteById(promotionId);
        List<ProductPromotion> links = linkRepository.findByPromotionId(promotionId);
        if (!links.isEmpty()) {
            linkRepository.deleteAll(links);
        }
    }

    @Override
    public List<ProductPromotion> findProductPromotions(UUID productId) {
        return linkRepository.findByProductId(Objects.requireNonNull(productId));
    }
}
