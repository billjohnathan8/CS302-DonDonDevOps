package org.ddk.promotions.repository;

import org.ddk.promotions.model.Promotion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("it")
@Tag("integration")
@SpringBootTest
class PromotionRepositoryTest {
    @Autowired PromotionRepository promotionRepository;

    @BeforeEach
    void clean() {
        promotionRepository.deleteAll();
    }

    private Promotion newPromotion(String name) {
        return new Promotion(
                name,
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-12-31T00:00:00Z"),
                0.15);
    }

    @Test
    void save_and_findById() {
        var p = new Promotion("Test", Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-12-31T00:00:00Z"), 0.2);
        var saved = promotionRepository.save(p);

        var found = promotionRepository.findById(Objects.requireNonNull(saved.getId())).orElseThrow();
        assertEquals("Test", found.getName());
        assertEquals(p.getStartTime(), found.getStartTime());
        assertEquals(p.getEndTime(), found.getEndTime());
        assertEquals(0.2, found.getDiscountRate(), 1e-9);
    }

    @Test
    void findAll_returns_list() {
        promotionRepository.save(new Promotion("A", Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-02-01T00:00:00Z"), 0.1));
        promotionRepository.save(new Promotion("B", Instant.parse("2025-03-01T00:00:00Z"), Instant.parse("2025-04-01T00:00:00Z"), 0.2));

        var all = promotionRepository.findAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void existsById_reflects_current_state() {
        var saved = promotionRepository.save(newPromotion("Exists"));
        var unknownId = UUID.randomUUID();

        assertTrue(promotionRepository.existsById(saved.getId()));
        assertFalse(promotionRepository.existsById(unknownId));
    }

    @Test
    void deleteById_removes_single_entry() {
        var first = promotionRepository.save(newPromotion("First"));
        var second = promotionRepository.save(newPromotion("Second"));

        promotionRepository.deleteById(first.getId());

        assertTrue(promotionRepository.findById(first.getId()).isEmpty());
        assertTrue(promotionRepository.findById(second.getId()).isPresent());
    }

    @Test
    void deleteAll_removes_everything() {
        promotionRepository.save(newPromotion("One"));
        promotionRepository.save(newPromotion("Two"));

        promotionRepository.deleteAll();

        assertTrue(promotionRepository.findAll().isEmpty());
    }
}
