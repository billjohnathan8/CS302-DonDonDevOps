package org.ddk.promotions.controller;

import org.ddk.promotions.model.ProductPromotion;
import org.ddk.promotions.service.ProductPromotionService;
import org.ddk.promotions.dto.PromotionProductDetailResponse;
import org.ddk.promotions.dto.ProductPromotionDetailResponse;
import org.ddk.promotions.dto.ProductPromotionRequest;
import org.ddk.promotions.dto.ProductPromotionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller that exposes CRUD operations for {@link ProductPromotion}.
 */
@RestController
@RequestMapping("/productpromotions")
public class ProductPromotionController {

    private final ProductPromotionService productPromotionService;

    public ProductPromotionController(ProductPromotionService productPromotionService) {
        this.productPromotionService = productPromotionService;
    }

    /**
     * Health probe endpoint used by monitoring systems.
     *
     * @return Simple status map indicating the service is up.
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @GetMapping
    public ResponseEntity<List<ProductPromotionResponse>> listAll() {
        List<ProductPromotionResponse> payload = productPromotionService.getAllProductPromotions()
            .stream()
            .map(ProductPromotionResponse::from)
            .toList();
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductPromotionResponse> getById(@PathVariable UUID id) {
        return productPromotionService.getProductPromotion(id)
            .map(ProductPromotionResponse::from)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductPromotionDetailResponse>> getByProductId(@PathVariable UUID productId) {
        List<ProductPromotionDetailResponse> payload = productPromotionService.findPromotionsForProduct(productId)
            .stream()
            .map(ProductPromotionDetailResponse::from)
            .toList();
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/promotion/{promotionId}")
    public ResponseEntity<List<PromotionProductDetailResponse>> getByPromotionId(@PathVariable UUID promotionId) {
        List<PromotionProductDetailResponse> payload = productPromotionService.findProductPromotionsByPromotion(promotionId)
            .stream()
            .map(pp -> new PromotionProductDetailResponse(pp.getProductId()))
            .toList();
        return ResponseEntity.ok(payload);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProductPromotionRequest request) {
        if (request == null || request.promotionId() == null || request.productId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "promotionId and productId are required"));
        }

        try {
            ProductPromotion created = productPromotionService.createProductPromotion(request.promotionId(), request.productId());
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
            return ResponseEntity.created(location).body(ProductPromotionResponse.from(created));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody ProductPromotionRequest request) {
        if (request == null || request.promotionId() == null || request.productId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "promotionId and productId are required"));
        }

        try {
            return productPromotionService.updateProductPromotion(id, request.promotionId(), request.productId())
                .map(ProductPromotionResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return productPromotionService.deleteProductPromotion(id)
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }
}
