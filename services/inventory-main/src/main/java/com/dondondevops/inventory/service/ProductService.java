package com.dondondevops.inventory.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.dondondevops.inventory.exception.UUIDNotFoundException;
import com.dondondevops.inventory.model.CreateProductRequest;
import com.dondondevops.inventory.model.Product;
import com.dondondevops.inventory.model.UpdateProductRequest;
import com.dondondevops.inventory.repository.ProductRepository;

import io.micronaut.context.annotation.Bean;
import jakarta.inject.Inject;

@Bean
public class ProductService {
    
    @Inject
    private ProductRepository repository;

    public List<Product> getAll() {
        return repository.getAll();
    }

    public Product get(UUID id) {
        return repository.getById(id).orElseThrow(() -> new UUIDNotFoundException(id));
    }

    public Product createProduct(CreateProductRequest request) {
        Product product = Product.builder().fromRequest(request).build();
        return repository.save(product);
    }

    public void delete(UUID id) {
        Product toDelete = get(id);
        repository.delete(toDelete);
    }

    public Product update(Product product, UpdateProductRequest request) {
        
        if(request.getName() != null) {
            product.setName(request.getName());
        }

        if(request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }

        if(request.getBrand() != null) {
            product.setCategory(request.getBrand());
        }
        
        product.setPriceInSGD(request.getPrice());
        product.setUpdatedAt(Instant.now());

        return repository.update(product);
    }
}
