package org.ddk.promotions.repository;

import org.ddk.promotions.model.ProductPromotion;
import org.ddk.promotions.model.Promotion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("it")
@Tag("integration")
@SpringBootTest
class ProductPromotionRepositoryTest {
    @Autowired ProductPromotionRepository linkRepository;
    @Autowired PromotionRepository promotionRepository;

    @BeforeEach
    void clean() {
        linkRepository.deleteAll();
        promotionRepository.deleteAll();
    }

    private Promotion newPromotion(String name) {
        return new Promotion(
                name,
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-12-31T00:00:00Z"),
                0.25);
    }

    @Test
    void findByProductId_returns_only_matching_links() {
        var promo = promotionRepository.save(new Promotion("X", Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-12-31T00:00:00Z"), 0.4));
        var productA = Objects.requireNonNull(UUID.randomUUID());
        var productB = Objects.requireNonNull(UUID.randomUUID());

        linkRepository.save(new ProductPromotion(promo.getId(), productA));
        linkRepository.save(new ProductPromotion(promo.getId(), productA));
        linkRepository.save(new ProductPromotion(promo.getId(), productB));

        List<ProductPromotion> aLinks = linkRepository.findByProductId(productA);
        assertEquals(2, aLinks.size());
        assertTrue(aLinks.stream().allMatch(pp -> pp.getProductId().equals(productA)));

        List<ProductPromotion> bLinks = linkRepository.findByProductId(productB);
        assertEquals(1, bLinks.size());
        assertEquals(promo.getId(), bLinks.get(0).getPromotionId());
    }

    @Test
    void findByPromotionId_returns_only_matching_links() {
        var promoA = promotionRepository.save(newPromotion("PromoA"));
        var promoB = promotionRepository.save(newPromotion("PromoB"));
        var productX = UUID.randomUUID();
        var productY = UUID.randomUUID();

        linkRepository.save(new ProductPromotion(promoA.getId(), productX));
        linkRepository.save(new ProductPromotion(promoA.getId(), productY));
        linkRepository.save(new ProductPromotion(promoB.getId(), productX));

        var links = linkRepository.findByPromotionId(promoA.getId());
        assertEquals(2, links.size());
        assertTrue(links.stream().allMatch(link -> link.getPromotionId().equals(promoA.getId())));
    }

    @Test
    void deleteById_removes_link() {
        var promo = promotionRepository.save(newPromotion("Delete"));
        var productId = UUID.randomUUID();
        var link = linkRepository.save(new ProductPromotion(promo.getId(), productId));

        linkRepository.deleteById(link.getId());

        assertTrue(linkRepository.findById(link.getId()).isEmpty());
    }

    @Test
    void deleteAll_collection_removes_specified_links() {
        var promo = promotionRepository.save(newPromotion("Collection"));
        var linkA = linkRepository.save(new ProductPromotion(promo.getId(), UUID.randomUUID()));
        var linkB = linkRepository.save(new ProductPromotion(promo.getId(), UUID.randomUUID()));
        var linkC = linkRepository.save(new ProductPromotion(promo.getId(), UUID.randomUUID()));

        linkRepository.deleteAll(List.of(linkA, linkB));

        assertTrue(linkRepository.findById(linkA.getId()).isEmpty());
        assertTrue(linkRepository.findById(linkB.getId()).isEmpty());
        assertTrue(linkRepository.findById(linkC.getId()).isPresent());
    }

    @Test
    void deleteAll_removes_every_link() {
        var promo = promotionRepository.save(newPromotion("Cleanup"));
        var productA = UUID.randomUUID();
        var productB = UUID.randomUUID();
        linkRepository.save(new ProductPromotion(promo.getId(), productA));
        linkRepository.save(new ProductPromotion(promo.getId(), productB));

        linkRepository.deleteAll();

        assertTrue(linkRepository.findByProductId(productA).isEmpty());
        assertTrue(linkRepository.findByProductId(productB).isEmpty());
    }
}
