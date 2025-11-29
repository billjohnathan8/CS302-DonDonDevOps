package org.ddk.promotions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ddk.promotions.model.ProductPromotion;
import org.ddk.promotions.model.Promotion;
import org.ddk.promotions.service.ProductPromotionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductPromotionController.class)
class ProductPromotionControllerTest {

    private static final MediaType JSON = MediaType.APPLICATION_JSON;

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockitoBean ProductPromotionService productPromotionService;

    @Test
    void health_returns_ok() throws Exception {
        mvc.perform(get("/productpromotions/health").accept(JSON))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(JSON))
           .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void listAll_returns_entire_table() throws Exception {
        UUID linkId = UUID.randomUUID();
        UUID promotionId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        ProductPromotion link = new ProductPromotion();
        link.setId(linkId);
        link.setPromotionId(promotionId);
        link.setProductId(productId);

        when(productPromotionService.getAllProductPromotions()).thenReturn(List.of(link));

        mvc.perform(get("/productpromotions").accept(JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(linkId.toString()))
            .andExpect(jsonPath("$[0].promotionId").value(promotionId.toString()))
            .andExpect(jsonPath("$[0].productId").value(productId.toString()));
    }

    @Test
    void getById_returns_404_when_missing() throws Exception {
        UUID linkId = UUID.randomUUID();
        when(productPromotionService.getProductPromotion(linkId)).thenReturn(Optional.empty());

        mvc.perform(get("/productpromotions/{id}", linkId).accept(JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void create_persists_mapping() throws Exception {
        UUID promotionId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID linkId = UUID.randomUUID();

        ProductPromotion saved = new ProductPromotion();
        saved.setId(linkId);
        saved.setPromotionId(promotionId);
        saved.setProductId(productId);

        when(productPromotionService.createProductPromotion(promotionId, productId)).thenReturn(saved);

        Map<String, String> payload = Map.of(
            "promotionId", promotionId.toString(),
            "productId", productId.toString()
        );

        mvc.perform(post("/productpromotions")
                .contentType(JSON)
                .content(mapper.writeValueAsString(payload)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", containsString("/productpromotions/" + linkId)))
            .andExpect(jsonPath("$.id").value(linkId.toString()));
    }

    @Test
    void create_returns_bad_request_when_service_rejects() throws Exception {
        UUID promotionId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        when(productPromotionService.createProductPromotion(promotionId, productId))
            .thenThrow(new IllegalArgumentException("Promotion not found"));

        Map<String, String> payload = Map.of(
            "promotionId", promotionId.toString(),
            "productId", productId.toString()
        );

        mvc.perform(post("/productpromotions")
                .contentType(JSON)
                .content(mapper.writeValueAsString(payload)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(containsString("Promotion not found")));
    }

    @Test
    void update_changes_mapping_when_present() throws Exception {
        UUID linkId = UUID.randomUUID();
        UUID promotionId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        ProductPromotion updated = new ProductPromotion();
        updated.setId(linkId);
        updated.setPromotionId(promotionId);
        updated.setProductId(productId);

        when(productPromotionService.updateProductPromotion(linkId, promotionId, productId))
            .thenReturn(Optional.of(updated));

        Map<String, String> payload = Map.of(
            "promotionId", promotionId.toString(),
            "productId", productId.toString()
        );

        mvc.perform(put("/productpromotions/{id}", linkId)
                .contentType(JSON)
                .content(mapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(linkId.toString()));
    }

    @Test
    void update_returns_not_found_when_missing() throws Exception {
        UUID linkId = UUID.randomUUID();
        UUID promotionId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        when(productPromotionService.updateProductPromotion(linkId, promotionId, productId))
            .thenReturn(Optional.empty());

        Map<String, String> payload = Map.of(
            "promotionId", promotionId.toString(),
            "productId", productId.toString()
        );

        mvc.perform(put("/productpromotions/{id}", linkId)
                .contentType(JSON)
                .content(mapper.writeValueAsString(payload)))
            .andExpect(status().isNotFound());
    }

    @Test
    void delete_invokes_service() throws Exception {
        UUID linkId = UUID.randomUUID();
        when(productPromotionService.deleteProductPromotion(linkId)).thenReturn(true);

        mvc.perform(delete("/productpromotions/{id}", linkId))
            .andExpect(status().isNoContent());

        verify(productPromotionService).deleteProductPromotion(linkId);
    }

    @Test
    void delete_returns_not_found_when_service_returns_false() throws Exception {
        UUID linkId = UUID.randomUUID();
        when(productPromotionService.deleteProductPromotion(linkId)).thenReturn(false);

        mvc.perform(delete("/productpromotions/{id}", linkId))
            .andExpect(status().isNotFound());
    }

    @Test
    void getByProductId_returns_promotion_details() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID promotionId = UUID.randomUUID();
        Promotion promotion = new Promotion();
        promotion.setId(promotionId);
        promotion.setName("Flash Sale");
        promotion.setStartTime(Instant.parse("2025-11-01T00:00:00Z"));
        promotion.setEndTime(Instant.parse("2025-11-02T00:00:00Z"));
        promotion.setDiscountRate(0.2);

        when(productPromotionService.findPromotionsForProduct(productId)).thenReturn(List.of(promotion));

        mvc.perform(get("/productpromotions/product/{productId}", productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(promotionId.toString()))
            .andExpect(jsonPath("$[0].name").value("Flash Sale"))
            .andExpect(jsonPath("$[0].discountRate").value(0.2));

        verify(productPromotionService).findPromotionsForProduct(productId);
    }

    @Test
    void getByPromotionId_returns_product_list() throws Exception {
        UUID promotionId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        ProductPromotion link = new ProductPromotion();
        link.setId(UUID.randomUUID());
        link.setPromotionId(promotionId);
        link.setProductId(productId);

        when(productPromotionService.findProductPromotionsByPromotion(promotionId)).thenReturn(List.of(link));

        mvc.perform(get("/productpromotions/promotion/{promotionId}", promotionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].productId").value(productId.toString()));

        verify(productPromotionService).findProductPromotionsByPromotion(promotionId);
    }

    @Test
    void create_validates_payload_before_calling_service() throws Exception {
        mvc.perform(post("/productpromotions")
                .contentType(JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(productPromotionService);
    }
}
