package org.ddk.promotions.service;

import org.ddk.promotions.model.Promotion;
import org.ddk.promotions.model.ProductPromotion;
import org.ddk.promotions.store.PromotionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service for canceling low-stock promotions when products are restocked.
 * Implements the event-driven flow: Inventory restocked → Promotions cancels flash sales.
 */
@Service
public class RestockPromotionService {

    private static final Logger log = LoggerFactory.getLogger(RestockPromotionService.class);
    private static final String LOW_STOCK_PROMOTION_PREFIX = "Flash Sale - Low Stock";

    private final PromotionStore store;
    private final PromotionService promotionService;

    public RestockPromotionService(PromotionStore store,
                                   PromotionService promotionService) {
        this.store = store;
        this.promotionService = promotionService;
    }

    /**
     * Cancels active low-stock promotions for a restocked product.
     * This ends the flash sale by setting the end time to now.
     *
     * @param productId The product ID that was restocked
     * @param stockAfter The new stock level after restocking
     * @return The number of promotions canceled
     */
    @Transactional
    public int cancelLowStockPromotions(UUID productId, int stockAfter) {
        log.info("Checking for low-stock promotions to cancel for productId={}, stockAfter={}",
            productId, stockAfter);

        int canceledCount = 0;
        Instant now = Instant.now();

        // Find all product-promotion links for this product
        List<ProductPromotion> productPromotions = store.findProductPromotions(productId);

        for (ProductPromotion pp : productPromotions) {
            UUID promotionId = pp.getPromotionId();

            // Load the promotion
            Promotion promotion = store.findPromotion(promotionId).orElse(null);

            if (promotion == null) {
                continue;
            }

            // Check if this is a low-stock promotion and is still active
            if (promotion.getName().startsWith(LOW_STOCK_PROMOTION_PREFIX) && promotion.isActiveAt(now)) {
                // Cancel the promotion by deleting it
                boolean deleted = promotionService.deletePromotion(promotionId);

                if (deleted) {
                    canceledCount++;
                    log.info("Canceled low-stock promotion id={} name='{}' for productId={}",
                        promotionId, promotion.getName(), productId);
                }
            }
        }

        if (canceledCount > 0) {
            log.info("Canceled {} low-stock promotion(s) for productId={}", canceledCount, productId);
        } else {
            log.debug("No active low-stock promotions found for productId={}", productId);
        }

        return canceledCount;
    }
}
