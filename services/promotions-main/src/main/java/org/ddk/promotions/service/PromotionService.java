package org.ddk.promotions.service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.ddk.promotions.dto.PromotionEndedEvent;
import org.ddk.promotions.dto.PromotionStartedEvent;
import org.ddk.promotions.event.publisher.PromotionEventPublisher;
import org.ddk.promotions.model.ProductPromotion;
import org.ddk.promotions.model.Promotion;
import org.ddk.promotions.store.PromotionStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business service that orchestrates promotion operations and delegates persistence
 * to {@link PromotionStore}.
 */
@Service
public class PromotionService {
    private final PromotionStore store;
    private final PromotionEventPublisher eventPublisher;

    /**
     * Main Constructor for the PromotionService Class.
     *
     * @param store promotion store abstraction.
     * @param eventPublisher Publisher for domain events
     */
    public PromotionService(PromotionStore store, PromotionEventPublisher eventPublisher) {
        this.store = store;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Finds the best (highest) discount rate available for the given product at the provided time.
     *
     * @param productId Identifier of the product whose promotions are analyzed.
     * @param now Instant used to check promotion validity.
     * @return Optional containing the best discount rate.
     */
    public Optional<Double> bestDiscountFor(UUID productId, Instant now) {
        double best = 0d;

        for (ProductPromotion pp : store.findProductPromotions(productId)) {
            Promotion pr = store.findPromotion(Objects.requireNonNull(pp.getPromotionId())).orElse(null);

            if (pr != null && pr.isActiveAt(now)) {
                best = Math.max(best, pr.getDiscountRate());
            }
        }

        return best > 0 ? Optional.of(best) : Optional.empty();
    }

    public List<Promotion> getPromotions() {
        return store.findAllPromotions();
    }

    /**
     * Creates and persists a new promotion.
     * Publishes a promotion.started event if the promotion is active or will be active.
     *
     * @param promotion Promotion data to persist.
     * @return Saved promotion instance.
     */
    @Transactional
    public Promotion createPromotion(Promotion promotion) {
        Promotion saved = store.savePromotion(Objects.requireNonNull(promotion));

        // Publish event if promotion is currently active or will start in the future
        Instant now = Instant.now();
        if (saved.isActiveAt(now) || saved.getStartTime().isAfter(now)) {
            eventPublisher.publishPromotionStarted(new PromotionStartedEvent(
                saved.getId(),
                saved.getName(),
                saved.getStartTime(),
                saved.getEndTime()
            ));
        }

        return saved;
    }

    /**
     * Replaces an existing promotion with new values.
     *
     * @param id Identifier of the promotion to replace.
     * @param updated Promotion containing the desired values.
     * @return Updated promotion or empty when not found.
     */
    public Optional<Promotion> replacePromotion(UUID id, Promotion updated) {
        return store.findPromotion(Objects.requireNonNull(id)).map(existing -> {
            existing.setName(updated.getName());
            existing.setStartTime(updated.getStartTime());
            existing.setEndTime(updated.getEndTime());
            existing.setDiscountRate(updated.getDiscountRate());
            return store.savePromotion(existing);
        });
    }

    /**
     * Applies a partial update to an existing promotion.
     *
     * @param id Identifier of the promotion to patch.
     * @param name Optional promotion name.
     * @param startTime Optional start time.
     * @param endTime Optional end time.
     * @param discountRate Optional discount rate override.
     * @return Updated promotion or empty when not found.
     */
    public Optional<Promotion> patchPromotion(UUID id, String name, Instant startTime, Instant endTime, Double discountRate) {
        return store.findPromotion(Objects.requireNonNull(id)).map(existing -> {
            if (name != null) {
                existing.setName(name);
            }
            if (startTime != null) {
                existing.setStartTime(startTime);
            }
            if (endTime != null) {
                existing.setEndTime(endTime);
            }
            if (discountRate != null) {
                existing.setDiscountRate(discountRate);
            }
            return store.savePromotion(Objects.requireNonNull(existing));
        });
    }

    /**
     * Removes a promotion when present.
     * Publishes a promotion.ended event after successful deletion.
     *
     * @param id Identifier of the promotion to delete.
     * @return True when deletion occurred, false otherwise.
     */
    @Transactional
    public boolean deletePromotion(UUID id) {
        if (!store.promotionExists(Objects.requireNonNull(id))) {
            return false;
        }

        store.deletePromotion(id);

        // Publish promotion ended event
        eventPublisher.publishPromotionEnded(new PromotionEndedEvent(id));

        return true;
    }
}
