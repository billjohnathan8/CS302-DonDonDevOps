package org.ddk.promotions.controller;

import org.ddk.promotions.model.Promotion;
import org.ddk.promotions.service.PromotionService;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.ddk.promotions.dto.ApplyRequest;
import org.ddk.promotions.dto.ApplyRequestItem;
import org.ddk.promotions.dto.ApplyResponse;
import org.ddk.promotions.dto.ApplyResponseItem;

/**
 * REST controller exposing CRUD endpoints for promotions as well as the discount
 * application endpoint.
 */
@RestController
@RequestMapping("/promotions")
public class PromotionController {

    record PromotionRequest(String name, Instant startTime, Instant endTime, Double discountRate) {}

    record PromotionPatchRequest(String name, Instant startTime, Instant endTime, Double discountRate) {}

    record PromotionResponse(UUID id, String name, Instant startTime, Instant endTime, double discountRate) {
        static PromotionResponse from(Promotion promotion) {
            return new PromotionResponse(
                promotion.getId(),
                promotion.getName(),
                promotion.getStartTime(),
                promotion.getEndTime(),
                promotion.getDiscountRate()
            );
        }
    }

    private final PromotionService service;

    /**
     * Creates a controller that delegates to the provided service.
     *
     * @param service Promotion business service.
     */
    public PromotionController(PromotionService service) {
        this.service = service;
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
    public ResponseEntity<List<Promotion>> getPromotions() {
        return ResponseEntity.ok(service.getPromotions());
    }

    /**
     * Creates a new promotion when the request is valid.
     *
     * @param req Promotion definition.
     * @return {@link ResponseEntity} carrying the created promotion or validation errors.
     */
    @PostMapping
    public ResponseEntity<PromotionResponse> createPromotion(@RequestBody PromotionRequest req) {
        if (req.name() == null || req.startTime() == null || req.endTime() == null || req.discountRate() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (invalidWindow(req.startTime(), req.endTime()) || invalidRate(req.discountRate())) {
            return ResponseEntity.badRequest().build();
        }

        Promotion created = service.createPromotion(new Promotion(req.name(), req.startTime(), req.endTime(), req.discountRate()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getId())
            .toUri();
        return ResponseEntity.created(location).body(PromotionResponse.from(created));
    }

    /**
     * Fully replaces an existing promotion.
     *
     * @param promotionId Identifier of the promotion to replace.
     * @param req New promotion payload.
     * @return Updated promotion or {@code 404} when it does not exist.
     */
    @PutMapping("/{promotionId}")
    public ResponseEntity<PromotionResponse> replacePromotion(@PathVariable UUID promotionId, @RequestBody PromotionRequest req) {
        if (req.name() == null || req.startTime() == null || req.endTime() == null || req.discountRate() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (invalidWindow(req.startTime(), req.endTime()) || invalidRate(req.discountRate())) {
            return ResponseEntity.badRequest().build();
        }

        Promotion updatedData = new Promotion(req.name(), req.startTime(), req.endTime(), req.discountRate());
        return service.replacePromotion(promotionId, updatedData)
            .map(PromotionResponse::from)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Applies partial updates to the specified promotion.
     *
     * @param promotionId Identifier of the promotion to patch.
     * @param req Request payload carrying optional fields.
     * @return Patched promotion or {@code 404} when not found.
     */
    @PatchMapping("/{promotionId}")
    public ResponseEntity<PromotionResponse> patchPromotion(@PathVariable UUID promotionId, @RequestBody PromotionPatchRequest req) {
        if (req.name() == null && req.startTime() == null && req.endTime() == null && req.discountRate() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (req.startTime() != null && req.endTime() != null && invalidWindow(req.startTime(), req.endTime())) {
            return ResponseEntity.badRequest().build();
        }
        if (req.discountRate() != null && invalidRate(req.discountRate())) {
            return ResponseEntity.badRequest().build();
        }

        return service.patchPromotion(promotionId, req.name(), req.startTime(), req.endTime(), req.discountRate())
            .map(PromotionResponse::from)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes the promotion identified by the provided id.
     *
     * @param promotionId Identifier to delete.
     * @return {@code 204} when deleted, {@code 404} otherwise.
     */
    @DeleteMapping("/{promotionId}")
    public ResponseEntity<Void> deletePromotion(@PathVariable UUID promotionId) {
        return service.deletePromotion(promotionId)
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }

    /**
     * Applies promotions to the supplied basket of products and returns pricing details.
     *
     * @param req Apply request payload.
     * @return Response describing discounts for each product.
     */
    @PostMapping("/apply")
    public ResponseEntity<ApplyResponse> apply(@RequestBody ApplyRequest req) {
        Instant t = (req.now() == null ? Instant.now() : Instant.parse(Objects.requireNonNull(req.now())));
        List<ApplyResponseItem> out = new ArrayList<>();

        for (ApplyRequestItem li : req.items()) {
            var productId = Objects.requireNonNull(UUID.fromString(li.productId()));
            var rate = service.bestDiscountFor(productId, t).orElse(0d);
            double finalUnit = li.unitPrice() * (1 - rate);
            out.add(new ApplyResponseItem(li.productId(), rate, li.unitPrice() - finalUnit, finalUnit));
        }

        return ResponseEntity.ok(new ApplyResponse(out));
    }

    private boolean invalidWindow(Instant startTime, Instant endTime) {
        return startTime != null && endTime != null && endTime.isBefore(startTime);
    }

    private boolean invalidRate(Double discountRate) {
        return discountRate == null || discountRate < 0 || discountRate > 1;
    }
}
