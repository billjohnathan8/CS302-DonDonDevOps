package org.ddk.promotions.service;

import org.ddk.promotions.model.Promotion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Service for auto-creating flash sale promotions when products fall below stock threshold.
 * Implements the event-driven flow: Inventory detects low stock → Promotions creates flash sale.
 */
@Service
public class LowStockPromotionService {

    private static final Logger log = LoggerFactory.getLogger(LowStockPromotionService.class);
    private static final double DEFAULT_LOW_STOCK_DISCOUNT_RATE = 0.2; // 20% off
    private static final Duration FLASH_SALE_DURATION = Duration.ofHours(48); // 48 hours
    private static final String LOW_STOCK_PROMOTION_PREFIX = "Flash Sale - Low Stock";

    private final PromotionService promotionService;
    private final ProductPromotionService productPromotionService;

    public LowStockPromotionService(PromotionService promotionService,
                                    ProductPromotionService productPromotionService) {
        this.promotionService = promotionService;
        this.productPromotionService = productPromotionService;
    }

    /**
     * Creates a flash sale promotion for a product with low stock.
     * The promotion starts immediately and lasts for 48 hours.
     *
     * @param productId The product ID with low stock
     * @param stock Current stock level
     * @param threshold The stock threshold that triggered this event
     * @return The created promotion
     */
    @Transactional
    public Promotion createLowStockPromotion(UUID productId, int stock, int threshold) {
        log.info("Creating low-stock flash sale for productId={}, stock={}, threshold={}",
            productId, stock, threshold);

        // Create promotion name
        String promotionName = String.format("%s - %d items left", LOW_STOCK_PROMOTION_PREFIX, stock);

        // Create promotion starting now for 48 hours
        Instant startTime = Instant.now();
        Instant endTime = startTime.plus(FLASH_SALE_DURATION);

        Promotion promotion = new Promotion(
            promotionName,
            startTime,
            endTime,
            DEFAULT_LOW_STOCK_DISCOUNT_RATE
        );

        // Save the promotion (this will trigger promotion.started event)
        Promotion savedPromotion = promotionService.createPromotion(promotion);

        // Attach the product to the promotion (this will trigger promotion.product_updated event)
        productPromotionService.attachProductToPromotion(
            savedPromotion.getId(),
            productId,
            DEFAULT_LOW_STOCK_DISCOUNT_RATE
        );

        log.info("Created low-stock flash sale id={} for productId={}, valid until {}",
            savedPromotion.getId(), productId, endTime);

        return savedPromotion;
    }
}
