package org.ddk.promotions.event.listener;

import org.ddk.promotions.config.RabbitMQConfig;
import org.ddk.promotions.dto.InventoryLowStockEvent;
import org.ddk.promotions.dto.InventoryRestockedEvent;
import org.ddk.promotions.service.LowStockPromotionService;
import org.ddk.promotions.service.RestockPromotionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listener for inventory events from the Inventory service.
 * Handles inventory.low_stock and inventory.restocked events.
 */
@Component
public class InventoryEventListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventListener.class);
    private final LowStockPromotionService lowStockPromotionService;
    private final RestockPromotionService restockPromotionService;

    public InventoryEventListener(LowStockPromotionService lowStockPromotionService,
                                  RestockPromotionService restockPromotionService) {
        this.lowStockPromotionService = lowStockPromotionService;
        this.restockPromotionService = restockPromotionService;
    }

    /**
     * Listens to inventory.low_stock events and triggers flash sale creation.
     * When a product falls below stock threshold, this automatically creates a flash sale promotion.
     *
     * @param event The inventory low stock event from the message queue
     * @throws Exception if promotion creation fails (message will be requeued or sent to DLQ)
     */
    @RabbitListener(queues = RabbitMQConfig.LOW_STOCK_QUEUE)
    public void handleLowStock(InventoryLowStockEvent event) {
        log.info("Received inventory.low_stock event for productId={}, stock={}, threshold={}",
            event.productId(), event.stock(), event.threshold());

        try {
            // Create flash sale promotion for the low-stock product
            lowStockPromotionService.createLowStockPromotion(
                event.productId(),
                event.stock(),
                event.threshold()
            );

            log.info("Successfully created flash sale promotion for productId={}", event.productId());
        } catch (Exception e) {
            log.error("Failed to create flash sale promotion for productId={}", event.productId(), e);
            // Rethrow to trigger retry policy or DLQ routing
            throw e;
        }
    }

    /**
     * Listens to inventory.restocked events and cancels active low-stock promotions.
     * When products are restocked, this automatically ends flash sale promotions for those products.
     *
     * @param event The inventory restocked event from the message queue
     * @throws Exception if promotion cancellation fails (message will be requeued or sent to DLQ)
     */
    @RabbitListener(queues = RabbitMQConfig.RESTOCKED_QUEUE)
    public void handleRestocked(InventoryRestockedEvent event) {
        log.info("Received inventory.restocked event for {}", event.item());

        try {
            InventoryRestockedEvent.RestockItem item = event.item();
            
            log.info("Processing restocked item: productId={}, added={}, stockAfter={}",
                item.productId(), item.added(), item.stockAfter());

            // Cancel low-stock promotions for this product
            int numPromotionsCanceled = restockPromotionService.cancelLowStockPromotions(
                item.productId(),
                item.stockAfter()
            );

            log.info("Successfully processed restocked event, canceled {} promotion(s)", numPromotionsCanceled);
        } catch (Exception e) {
            log.error("Failed to process restocked event", e);
            // Rethrow to trigger retry policy or DLQ routing
            throw e;
        }
    }
}
