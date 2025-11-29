package com.dondondevops.inventory.event.publisher;

import com.dondondevops.inventory.event.dto.LowStockEvent;
import com.dondondevops.inventory.event.dto.RestockedEvent;

import io.micronaut.rabbitmq.annotation.Binding;
import io.micronaut.rabbitmq.annotation.RabbitClient;

/**
 * Declarative RabbitMQ client for publishing inventory domain events.
 * Uses Micronaut's @RabbitClient for automatic message publishing.
 */
@RabbitClient("inventory.events")
public interface InventoryEventPublisher {

    /**
     * Publishes a low stock event when product inventory falls below threshold.
     *
     * @param event The low stock event containing product ID, current stock, and threshold
     */
    @Binding("inventory.low_stock")
    void publishLowStock(LowStockEvent event);

    /**
     * Publishes a restocked event when products are replenished.
     *
     * @param event The restocked event containing list of restocked items
     */
    @Binding("inventory.restocked")
    void publishRestocked(RestockedEvent event);
}
