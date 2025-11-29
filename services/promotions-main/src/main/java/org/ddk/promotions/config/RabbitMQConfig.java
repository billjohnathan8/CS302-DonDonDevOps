package org.ddk.promotions.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for the Promotions service.
 * Configures exchanges, queues, and bindings for event-driven communication.
 */
@Configuration
public class RabbitMQConfig {

    // ============= Exchange Names =============
    public static final String PROMOTIONS_EXCHANGE = "promotions.events";
    public static final String INVENTORY_EXCHANGE = "inventory.events";
    public static final String NOTIFICATION_EXCHANGE = "notifications.events";

    // ============= Routing Keys (Outbound) =============
    public static final String PROMOTION_STARTED_KEY = "promotion.started";
    public static final String PROMOTION_ENDED_KEY = "promotion.ended";
    public static final String PROMOTION_PRODUCT_UPDATED_KEY = "promotion.product_updated";
    public static final String NOTIFICATION_PROMOTION_STARTED_KEY = "notification.promotion.started";
    public static final String NOTIFICATION_PROMOTION_ENDED_KEY = "notification.promotion.ended";
    public static final String NOTIFICATION_PROMOTION_UPDATED_KEY = "notification.promotion.updated";

    // ============= Routing Keys (Inbound) =============
    public static final String INVENTORY_LOW_STOCK_KEY = "inventory.low_stock";
    public static final String INVENTORY_RESTOCKED_KEY = "inventory.restocked";

    // ============= Queue Names =============
    public static final String LOW_STOCK_QUEUE = "promotions.low_stock.queue";
    public static final String RESTOCKED_QUEUE = "promotions.restocked.queue";

    // ============= Outbound: Promotions Exchange =============

    /**
     * Topic exchange for publishing promotion events.
     * Other services (Inventory, Orders) can bind queues to specific routing keys.
     */
    @Bean
    public TopicExchange promotionsExchange() {
        return ExchangeBuilder
            .topicExchange(PROMOTIONS_EXCHANGE)
            .durable(true)
            .build();
    }

    // ============= Inbound: Inventory Exchange + Queue =============

    /**
     * Topic exchange for inventory events (created by Inventory service).
     * We bind our queues to listen for inventory events.
     */
    @Bean
    public TopicExchange inventoryExchange() {
        return ExchangeBuilder
            .topicExchange(INVENTORY_EXCHANGE)
            .durable(true)
            .build();
    }

    /**
     * Queue for receiving inventory.low_stock events.
     * When products fall below stock threshold, we auto-create flash sale promotions.
     */
    @Bean
    public Queue lowStockQueue() {
        return QueueBuilder
            .durable(LOW_STOCK_QUEUE)
            .build();
    }

    /**
     * Binding to route inventory.low_stock messages to our queue.
     */
    @Bean
    public Binding lowStockBinding(Queue lowStockQueue, TopicExchange inventoryExchange) {
        return BindingBuilder
            .bind(lowStockQueue)
            .to(inventoryExchange)
            .with(INVENTORY_LOW_STOCK_KEY);
    }

    /**
     * Queue for receiving inventory.restocked events.
     * When products are restocked, we cancel active low-stock promotions.
     */
    @Bean
    public Queue restockedQueue() {
        return QueueBuilder
            .durable(RESTOCKED_QUEUE)
            .build();
    }

    /**
     * Binding to route inventory.restocked messages to our queue.
     */
    @Bean
    public Binding restockedBinding(Queue restockedQueue, TopicExchange inventoryExchange) {
        return BindingBuilder
            .bind(restockedQueue)
            .to(inventoryExchange)
            .with(INVENTORY_RESTOCKED_KEY);
    }

    // ============= Message Converter =============

    /**
     * JSON message converter for serializing/deserializing events.
     * Uses Jackson to convert POJOs to/from JSON.
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate configured with JSON converter for publishing messages.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
