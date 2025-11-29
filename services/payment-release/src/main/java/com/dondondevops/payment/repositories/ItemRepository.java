package com.dondondevops.payment.repositories;

import java.util.List;
import java.util.SequencedCollection;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.dondondevops.payment.entities.Item;
import com.dondondevops.payment.entities.Product;

/**
 * Adapter class for product table
 */
@Repository
public class ItemRepository {
    private final ProductRepository products;
    
    @Autowired
    public ItemRepository(ProductRepository repository) {
        products = repository;
    }

    public Item findById(UUID id) {
        var product = products.findById(id);
        if (product == null) {
            return null;
        }
        var item = new Item.Builder()
            .withId(product.getProductID())
            .withName(product.getName())
            .build();
        return item;
    }
    
    /**
     * Find all orders with given IDs
     */
    public List<Item> findByIds(SequencedCollection<UUID> ids) {
        return ids.stream()
            .map(id -> findById(id))
            .toList();
    }
    
    /**
     * @deprecated
     * Creating new items shouldn't be supported. This method exists for test compatibility reasons.
     */
    @Deprecated(forRemoval = true)
    public Item save(Item item) {
        Product product = Product.builder()
            .setProductID(item.getId())
            .setName(item.getName())
            .setPriceInSGD(0)
            .build();
        products.save(product);
        var newItem = new Item.Builder(item)
            .withId(product.getProductID())
            .build();
        return newItem;
    }
}
