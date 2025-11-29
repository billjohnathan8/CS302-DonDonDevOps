package com.dondondevops.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.dondondevops.inventory.model.CreateProductRequest;
import com.dondondevops.inventory.model.Product;
import com.dondondevops.inventory.service.ProductService;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
public class ProductControllerTest {
    
    @Inject
    ProductService service;

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void testGetAllReturnsOkEmptyList() throws Exception {
        when(service.getAll())
            .thenReturn(new ArrayList<Product>());
        
        HttpResponse<List<Product>> response = client.toBlocking().exchange("/api/product");

        assertEquals(HttpStatus.OK, response.getStatus());
    }

    @Test
    void testCreateProductReturnsOk() throws Exception {
        // Arrange
        UUID fakeUUID = UUID.randomUUID();
        Instant nowInstant = Instant.now();;
        Instant expiryInstant = nowInstant.plus(365, ChronoUnit.DAYS);

        when(service.createProduct(any(CreateProductRequest.class)))
        .thenAnswer(invocation -> {
            CreateProductRequest req = invocation.getArgument(0);
            Product product = new Product();

            product.setProductID(fakeUUID);
            product.setName(req.getName());
            product.setBrand(req.getBrand());
            product.setCategory(req.getCategory());
            product.setPriceInSGD(req.getPrice());
            product.setStock(req.getStock());
            product.setExpiryDate(req.getExpiryDate());
            product.setCreatedAt(nowInstant);
            product.setUpdatedAt(nowInstant);

            return product;
        });

        // Act
        CreateProductRequest createProductRequest = new CreateProductRequest(
            "Panadol", 
            "Medicine", 
            "Panadol", 
            100, 
            9.9, 
            expiryInstant);

        HttpRequest<CreateProductRequest> request = HttpRequest.POST("/api/product", createProductRequest);
        HttpResponse<Product> response = client.toBlocking().exchange(request, Product.class);


        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertNotNull(response.body());
        Product product = response.body();

        assertEquals(fakeUUID, product.getProductID());
        assertEquals("Panadol", product.getName());
        assertEquals("Medicine", product.getCategory());
        assertEquals("Panadol", product.getBrand());
        assertEquals(9.9, product.getPriceInSGD());
        assertEquals(100, product.getStock());
        
        assertEquals(expiryInstant.toString(), product.getExpiryDate().toString());

        assertEquals(nowInstant.toString(), product.getCreatedAt().toString());
        assertEquals(nowInstant.toString(), product.getUpdatedAt().toString());
    }

    @Test
    void testGetNonExistentRouteReturns404() throws Exception {
        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () -> client.toBlocking().retrieve("/non-existent-route"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getResponse().body().toString().contains("/non-existent-route does not exist."));
    }
    
    @MockBean(ProductService.class)
    ProductService productService() {
        return mock(ProductService.class);
    }
}
