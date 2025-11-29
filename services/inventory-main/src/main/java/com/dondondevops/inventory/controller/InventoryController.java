package com.dondondevops.inventory.controller;

import java.util.UUID;

import com.dondondevops.inventory.model.Product;
import com.dondondevops.inventory.model.ReduceStockRequest;
import com.dondondevops.inventory.model.RestockRequest;
import com.dondondevops.inventory.service.InventoryService;
import com.dondondevops.inventory.service.ProductService;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;

@Controller("/api/inventory")
public class InventoryController {

    @Inject
    private InventoryService inventoryService;

    @Inject
    private ProductService productService;
    

    @Post("/restock")
    public HttpResponse<Product> restock(@Body RestockRequest request) {
        Product updatedProducts = inventoryService.restock(request);
        return HttpResponse.ok().body(updatedProducts);
    }

    @Post("/reduce-stock/{id}")
    public HttpResponse<Product> reduceStock(@PathVariable @NonNull UUID id, @Body ReduceStockRequest request) {
        Product targetProduct = productService.get(id);
        Product updatedProduct = inventoryService.reduce(targetProduct, request);
        return HttpResponse.ok().body(updatedProduct);
    }
}
