package com.dondondevops.inventory.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dondondevops.inventory.event.dto.LowStockEvent;
import com.dondondevops.inventory.event.dto.RestockedEvent;
import com.dondondevops.inventory.event.dto.RestockedItem;
import com.dondondevops.inventory.event.publisher.InventoryEventPublisher;
import com.dondondevops.inventory.exception.UUIDNotFoundException;
import com.dondondevops.inventory.model.Product;
import com.dondondevops.inventory.model.ReduceStockRequest;
import com.dondondevops.inventory.model.RestockRequest;
import com.dondondevops.inventory.repository.ProductRepository;

import io.micronaut.context.annotation.Bean;
import jakarta.inject.Inject;

@Bean
public class InventoryService {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryService.class);
    private static final int LOW_STOCK_THRESHOLD = 10;

    @Inject
    private ProductRepository repository;

    @Inject
    private InventoryEventPublisher eventPublisher;

    public Product restock(RestockRequest request) {
        /**
         * If any one of the RestockRequest fails, we return the error immediately.
         * Consider changing it to transactional or return a list of errors instead.
         */
        Product product = repository.getById(request.getProductId())
            .orElseThrow(() -> new UUIDNotFoundException(request.getProductId()));

            product.setStock(product.getStock() + request.getQuantity());
            product.setExpiryDate(request.getExpiryDate());
            product.setUpdatedAt(Instant.now());

            Product updatedProduct = repository.update(product);

            // Collect restocked items for event
            RestockedItem restockedItem = new RestockedItem(
                updatedProduct.getProductID(),
                request.getQuantity(),
                updatedProduct.getStock()
            );

        // Publish inventory.restocked event
        try {
            RestockedEvent event = new RestockedEvent(restockedItem);
            LOG.info("Attempting to publish inventory.restocked event for {}", restockedItem.toString());
            eventPublisher.publishRestocked(event);
            LOG.info("Successfully published inventory.restocked event for {}", restockedItem.toString());
        } catch (Exception e) {
            LOG.error("Failed to publish inventory.restocked event", e);
        }

        return updatedProduct;
    }

    public Product reduce(Product product, ReduceStockRequest request) {
        int previousStock = product.getStock();
        product.setStock(product.getStock() - request.getQuantity());
        product.setUpdatedAt(Instant.now());

        Product updatedProduct = repository.update(product);

        // Publish inventory.low_stock event if stock falls below threshold
        if (updatedProduct.getStock() < LOW_STOCK_THRESHOLD && previousStock >= LOW_STOCK_THRESHOLD) {
            try {
                LowStockEvent event = new LowStockEvent(
                    updatedProduct.getProductID(),
                    updatedProduct.getStock(),
                    LOW_STOCK_THRESHOLD
                );
                LOG.info("Attempting to publish inventory.low_stock event for product {} - stock: {}, threshold: {}",
                    updatedProduct.getProductID(), updatedProduct.getStock(), LOW_STOCK_THRESHOLD);
                eventPublisher.publishLowStock(event);
                LOG.info("Successfully published inventory.low_stock event for product {}",
                    updatedProduct.getProductID());
            } catch (Exception e) {
                LOG.error("Failed to publish inventory.low_stock event for product {}",
                    updatedProduct.getProductID(), e);
                // Don't fail the reduce operation if event publishing fails
            }
        }

        return updatedProduct;
    }
}
