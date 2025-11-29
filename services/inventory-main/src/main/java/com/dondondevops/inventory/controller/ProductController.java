package com.dondondevops.inventory.controller;

import java.util.List;
import java.util.UUID;

import com.dondondevops.inventory.model.CreateProductRequest;
import com.dondondevops.inventory.model.Product;
import com.dondondevops.inventory.model.UpdateProductRequest;
import com.dondondevops.inventory.service.ProductService;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;

@Controller("/api/product")
public class ProductController {

    @Inject
    private ProductService service;

    @Get()
    public HttpResponse<List<Product>> get() {
        return HttpResponse.ok(service.getAll());
    }

    @Get("{id}")
    public HttpResponse<Product> get(@PathVariable @NonNull UUID id) {
        return HttpResponse.ok(service.get(id));
    }

    @Post()
    public HttpResponse<Product> post(@Body CreateProductRequest request) {
        return HttpResponse.created(service.createProduct(request));
    }

    @Delete("{id}")
    public HttpResponse<?> delete(@PathVariable @NonNull UUID id) {
        service.delete(id);
        return HttpResponse.noContent();
    }

    @Patch("{id}")
    public HttpResponse<Product> update(@PathVariable @NonNull UUID id, @Body UpdateProductRequest request) {
        Product targetProduct = service.get(id);
        Product updatedProduct = service.update(targetProduct, request);
        return HttpResponse.ok().body(updatedProduct);
    }
}
