package org.ddk.promotions.event.publisher;

import org.ddk.promotions.config.RabbitMQConfig;
import org.ddk.promotions.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publisher for promotion domain events to RabbitMQ.
 * Provides methods to publish promotion-related events directly to the message broker.
 */
@Component
public class PromotionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PromotionEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public PromotionEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publishes promotion.started event to RabbitMQ.
     * Called when a new promotion is created or becomes active.
     *
     * @param event The promotion started event to publish
     */
    public void publishPromotionStarted(PromotionStartedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.PROMOTIONS_EXCHANGE,
                RabbitMQConfig.PROMOTION_STARTED_KEY,
                event
            );
            log.info("Published promotion.started event for promotionId={}, name='{}'",
                event.promotionId(), event.name());
        } catch (Exception e) {
            log.error("Failed to publish promotion.started event for promotionId={}",
                event.promotionId(), e);
            // Note: Event is lost. Consider implementing outbox pattern for guaranteed delivery.
        }
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_PROMOTION_STARTED_KEY,
                event
            );
            log.info("Published notification.promotion.started event for promotionId={}, name='{}'",
                event.promotionId(), event.name());
        } catch (Exception e) {
            log.error("Failed to publish notification.promotion event for promotionId={}",
                event.promotionId(), e);
            // Note: Event is lost. Consider implementing outbox pattern for guaranteed delivery.
        }
    }

    /**
     * Publishes promotion.ended event to RabbitMQ.
     * Called when a promotion is deleted or expires.
     *
     * @param event The promotion ended event to publish
     */
    public void publishPromotionEnded(PromotionEndedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.PROMOTIONS_EXCHANGE,
                RabbitMQConfig.PROMOTION_ENDED_KEY,
                event
            );
            log.info("Published promotion.ended event for promotionId={}", event.promotionId());
        } catch (Exception e) {
            log.error("Failed to publish promotion.ended event for promotionId={}",
                event.promotionId(), e);
        }
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_PROMOTION_ENDED_KEY,
                event
            );
            log.info("Published notification.promotion.ended event for promotionId={}", event.promotionId());
        } catch (Exception e) {
            log.error("Failed to publish notification.promotion.ended event for promotionId={}",
                event.promotionId(), e);
        }
    }

    /**
     * Publishes promotion.product_updated event to RabbitMQ.
     * Called when a product is attached to a promotion or discount rate changes.
     *
     * @param event The promotion product updated event to publish
     */
    public void publishPromotionProductUpdated(PromotionProductUpdatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.PROMOTIONS_EXCHANGE,
                RabbitMQConfig.PROMOTION_PRODUCT_UPDATED_KEY,
                event
            );
            log.info("Published promotion.product_updated event for promotionId={}, productId={}, rate={}",
                event.promotionId(), event.productId(), event.discountRate());
        } catch (Exception e) {
            log.error("Failed to publish promotion.product_updated event for promotionId={}, productId={}",
                event.promotionId(), event.productId(), e);
        }
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_PROMOTION_UPDATED_KEY,
                event
            );
            log.info("Published notification.promotion.updated event for promotionId={}, productId={}, rate={}",
                event.promotionId(), event.productId(), event.discountRate());
        } catch (Exception e) {
            log.error("Failed to publish notification.promotion.updated event for promotionId={}, productId={}",
                event.promotionId(), event.productId(), e);
        }
    }
}
