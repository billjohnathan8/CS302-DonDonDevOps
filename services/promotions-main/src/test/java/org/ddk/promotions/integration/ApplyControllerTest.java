package org.ddk.promotions.integration;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.ddk.promotions.dto.ApplyRequest;
import org.ddk.promotions.dto.ApplyRequestItem;
import org.ddk.promotions.dto.ApplyResponse;
import org.ddk.promotions.dto.ApplyResponseItem;
import org.ddk.promotions.model.ProductPromotion;
import org.ddk.promotions.model.Promotion;
import org.ddk.promotions.repository.ProductPromotionRepository;
import org.ddk.promotions.repository.PromotionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration Test using DynamoDB Testcontainers for the PromotionController Class.
 */
@ActiveProfiles("it")
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplyControllerTest {
    @LocalServerPort int port; 

    @Autowired PromotionRepository promoRepo;
    @Autowired ProductPromotionRepository linkRepo;

    /**
     * Seed or Mock a value into each Test Stub before a integration test run.
     * @Return Test Fixture at Runtime
     */
    @BeforeEach 
    void seed() {
        linkRepo.deleteAll();
        promoRepo.deleteAll();

        var promotion = new Promotion();
        promotion.setName("Expiring");
        promotion.setStartTime(Instant.parse("2025-10-29T00:00:00Z"));
        promotion.setEndTime(Instant.parse("2025-12-31T00:00:00Z"));
        promotion.setDiscountRate(0.5);
        promoRepo.save(promotion);

        var link = new ProductPromotion(); 
        link.setPromotionId(promotion.getId());
        link.setProductId(Objects.requireNonNull(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")));
        linkRepo.save(link);
    }

    /**
     * Integration Test.
     * Tries to verify whether a discount is actually applied during server runtime.
     * @Return 
     */
    @Test
    void appliesDiscount() {
        var restTemplate = new TestRestTemplate();
        var reqBody = new ApplyRequest(
            "2025-10-29T12:00:00Z",
            List.of(new ApplyRequestItem(
                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                1,
                10.0
            ))
        );

        var res = restTemplate.postForEntity("http://localhost:" + port + "/promotions/apply", reqBody, ApplyResponse.class);

        ApplyResponseItem first = java.util.Objects.requireNonNull(res.getBody()).items().get(0);

        Assertions.assertEquals(0.5, first.discountRate(), 1e-9);
        Assertions.assertEquals(5.0, first.finalUnitPrice(), 1e-9);
    }
}
