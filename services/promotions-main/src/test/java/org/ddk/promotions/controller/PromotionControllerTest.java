package org.ddk.promotions.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.ddk.promotions.dto.ApplyRequest;
import org.ddk.promotions.dto.ApplyRequestItem;
import org.ddk.promotions.model.Promotion;
import org.ddk.promotions.service.PromotionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(PromotionController.class)
class PromotionControllerTest {
    private static final MediaType JSON = java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON);
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockitoBean PromotionService promotionService;

    @Test
    void health_returns_ok() throws Exception {
        mvc.perform(get("/promotions/health").accept(JSON))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(Objects.requireNonNull(JSON)))
           .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void apply_calculates_discount_and_totals() throws Exception {
        var productId = Objects.requireNonNull(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        when(promotionService.bestDiscountFor(eq(productId), any(Instant.class)))
            .thenReturn(Optional.of(0.5));

        var req = new ApplyRequest(
            "2025-10-29T12:00:00Z",
            List.of(new ApplyRequestItem(
                productId.toString(),
                1,
                10.0
            ))
        );

        mvc.perform(post("/promotions/apply")
                .contentType(Objects.requireNonNull(JSON))
                .content(Objects.requireNonNull(mapper.writeValueAsString(req))))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(Objects.requireNonNull(JSON)))
           .andExpect(jsonPath("$.items[0].discountRate").value(0.5))
           .andExpect(jsonPath("$.items[0].discountAmount").value(5.0))
           .andExpect(jsonPath("$.items[0].finalUnitPrice").value(5.0));
    }

    @Test
    void apply_passes_parsed_now_and_productId_to_service() throws Exception {
        var productId = Objects.requireNonNull(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
        var at = Instant.parse("2025-11-01T00:00:00Z");
        when(promotionService.bestDiscountFor(any(UUID.class), any(Instant.class)))
            .thenReturn(Optional.of(0.2));

        var req = new ApplyRequest(
            at.toString(),
            List.of(new ApplyRequestItem(
                productId.toString(),
                2,
                30.0
            ))
        );

        mvc.perform(post("/promotions/apply")
                .contentType(Objects.requireNonNull(JSON))
                .content(Objects.requireNonNull(mapper.writeValueAsString(req))))
           .andExpect(status().isOk());

        var uuidCap = ArgumentCaptor.forClass(UUID.class);
        var instantCap = ArgumentCaptor.forClass(Instant.class);
        verify(promotionService).bestDiscountFor(uuidCap.capture(), instantCap.capture());
        org.junit.jupiter.api.Assertions.assertEquals(productId, uuidCap.getValue());
        org.junit.jupiter.api.Assertions.assertEquals(at, instantCap.getValue());
    }

    @Test
    void apply_defaults_now_and_zero_rate_when_missing() throws Exception {
        var productId = Objects.requireNonNull(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"));
        when(promotionService.bestDiscountFor(eq(productId), any(Instant.class))).thenReturn(Optional.empty());

        var req = new ApplyRequest(
            null,
            List.of(new ApplyRequestItem(
                productId.toString(),
                1,
                40.0
            ))
        );

        var before = Instant.now();
        mvc.perform(post("/promotions/apply")
                .contentType(Objects.requireNonNull(JSON))
                .content(Objects.requireNonNull(mapper.writeValueAsString(req))))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.items[0].discountRate").value(0.0))
           .andExpect(jsonPath("$.items[0].discountAmount").value(0.0))
           .andExpect(jsonPath("$.items[0].finalUnitPrice").value(40.0));
        var after = Instant.now();

        var instantCap = ArgumentCaptor.forClass(Instant.class);
        verify(promotionService).bestDiscountFor(eq(productId), instantCap.capture());
        var usedInstant = instantCap.getValue();
        org.junit.jupiter.api.Assertions.assertFalse(usedInstant.isBefore(before));
        org.junit.jupiter.api.Assertions.assertFalse(usedInstant.isAfter(after.plusSeconds(1)));
    }

    @Test
    void getAllPromotions_returns_list_of_promotions() throws Exception {
        UUID promoId = Objects.requireNonNull(UUID.fromString("11111111-2222-3333-4444-555555555555"));
        var start = Instant.parse("2025-05-01T00:00:00Z");
        var end = Instant.parse("2025-05-31T23:59:59Z");
        var created = new Promotion("Flash", start, end, 0.3);
        created.setId(promoId);
        List<Promotion> allPromotions = List.of(created);

        when(promotionService.getPromotions()).thenReturn(allPromotions);

        mvc.perform(get("/promotions")
            .contentType(Objects.requireNonNull(JSON))
            .content(allPromotions.toString()))
            .andExpect(status().isOk());

            verify(promotionService).getPromotions();
    }

    @Test
    void createPromotion_persists_and_returns_created_response() throws Exception {
        var promoId = Objects.requireNonNull(UUID.fromString("11111111-2222-3333-4444-555555555555"));
        var start = Instant.parse("2025-05-01T00:00:00Z");
        var end = Instant.parse("2025-05-31T23:59:59Z");
        var created = new Promotion("Flash", start, end, 0.3);
        created.setId(promoId);
        when(promotionService.createPromotion(any(Promotion.class))).thenReturn(created);

        mvc.perform(post("/promotions")
                .contentType(Objects.requireNonNull(JSON))
                .content(Objects.requireNonNull(promotionPayload("Flash", start.toString(), end.toString(), 0.3))))
           .andExpect(status().isCreated())
           .andExpect(header().string("Location", Objects.requireNonNull(containsString(promoId.toString()))))
           .andExpect(jsonPath("$.id").value(promoId.toString()))
           .andExpect(jsonPath("$.discountRate").value(0.3));

        var promotionCaptor = ArgumentCaptor.forClass(Promotion.class);
        verify(promotionService).createPromotion(promotionCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals("Flash", promotionCaptor.getValue().getName());
    }

    @Test
    void createPromotion_rejects_missing_fields() throws Exception {
        mvc.perform(post("/promotions")
                .contentType(Objects.requireNonNull(JSON))
                .content(Objects.requireNonNull(mapper.writeValueAsString(Map.of("name", "Incomplete")))))
           .andExpect(status().isBadRequest());

        verifyNoInteractions(promotionService);
    }

    @Test
    void createPromotion_rejects_invalid_window_or_rate() throws Exception {
        var start = Instant.parse("2025-05-01T00:00:00Z");
        var endBefore = Instant.parse("2025-04-01T00:00:00Z");

        mvc.perform(post("/promotions")
                .contentType(Objects.requireNonNull(JSON))
                .content(Objects.requireNonNull(promotionPayload("BadWindow", start.toString(), endBefore.toString(), 0.2))))
           .andExpect(status().isBadRequest());

        mvc.perform(post("/promotions")
                .contentType(Objects.requireNonNull(JSON))
                .content(Objects.requireNonNull(promotionPayload("BadRate", start.toString(), start.plusSeconds(3600).toString(), 1.5))))
           .andExpect(status().isBadRequest());

        verifyNoInteractions(promotionService);
    }

    @Test
    void createPromotion_rejects_negative_discount_rate() throws Exception {
        var start = Instant.parse("2025-05-01T00:00:00Z");
        var end = Instant.parse("2025-05-31T23:59:59Z");

        mvc.perform(post("/promotions")
                .contentType(Objects.requireNonNull(JSON))
                .content(Objects.requireNonNull(promotionPayload("BadRate", start.toString(), end.toString(), -0.2))))
           .andExpect(status().isBadRequest());

        verifyNoInteractions(promotionService);
    }

    @Test
    void replacePromotion_updates_existing_promotion() throws Exception {
        var promoId = Objects.requireNonNull(UUID.randomUUID());
        var start = Instant.parse("2025-01-01T00:00:00Z");
        var end = Instant.parse("2025-03-01T00:00:00Z");
        var updated = new Promotion("Updated", start, end, 0.25);
        updated.setId(promoId);
        when(promotionService.replacePromotion(eq(promoId), any(Promotion.class))).thenReturn(Optional.of(updated));

        mvc.perform(put("/promotions/{id}", promoId)
                .contentType(Objects.requireNonNull(JSON))
                .content(Objects.requireNonNull(promotionPayload("Updated", start.toString(), end.toString(), 0.25))))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").value(promoId.toString()))
           .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void replacePromotion_returns_not_found_when_missing() throws Exception {
        var promoId = Objects.requireNonNull(UUID.randomUUID());
        var start = Instant.parse("2025-01-01T00:00:00Z");
        var end = Instant.parse("2025-03-01T00:00:00Z");
        when(promotionService.replacePromotion(eq(promoId), any(Promotion.class))).thenReturn(Optional.empty());

        mvc.perform(put("/promotions/{id}", promoId)
                .contentType(Objects.requireNonNull(JSON))
                .content(Objects.requireNonNull(promotionPayload("Updated", start.toString(), end.toString(), 0.25))))
           .andExpect(status().isNotFound());
    }

    @Test
    void replacePromotion_rejects_missing_fields() throws Exception {
        var promoId = Objects.requireNonNull(UUID.randomUUID());

        mvc.perform(put("/promotions/{id}", promoId)
                .contentType(Objects.requireNonNull(JSON))
                .content(Objects.requireNonNull(mapper.writeValueAsString(Map.of("name", "MissingFields")))))
           .andExpect(status().isBadRequest());

        verifyNoInteractions(promotionService);
    }

    @Test
    void replacePromotion_rejects_invalid_window_or_rate() throws Exception {
        var promoId = Objects.requireNonNull(UUID.randomUUID());
        var start = Instant.parse("2025-01-01T00:00:00Z");
        var end = Instant.parse("2025-02-01T00:00:00Z");

        mvc.perform(put("/promotions/{id}", promoId)
                .contentType(Objects.requireNonNull(JSON))
                .content(Objects.requireNonNull(promotionPayload("BadWindow", start.toString(), start.minusSeconds(1).toString(), 0.2))))
           .andExpect(status().isBadRequest());

        mvc.perform(put("/promotions/{id}", promoId)
                .contentType(Objects.requireNonNull(JSON))
                .content(Objects.requireNonNull(promotionPayload("BadRate", start.toString(), end.toString(), 2.0))))
           .andExpect(status().isBadRequest());

        verify(promotionService, never()).replacePromotion(any(UUID.class), any(Promotion.class));
    }

    @Test
    void patchPromotion_rejects_empty_body() throws Exception {
        var promoId = Objects.requireNonNull(UUID.randomUUID());

        mvc.perform(patch("/promotions/{id}", promoId)
                .contentType(Objects.requireNonNull(JSON))
                .content(Objects.requireNonNull(mapper.writeValueAsString(Map.of()))))
           .andExpect(status().isBadRequest());

        verify(promotionService, never()).patchPromotion(any(UUID.class), any(), any(), any(), any());
    }

    @Test
    void patchPromotion_rejects_invalid_window_or_rate() throws Exception {
        var promoId = Objects.requireNonNull(UUID.randomUUID());
        var start = Instant.parse("2025-01-01T00:00:00Z");
        
        mvc.perform(patch("/promotions/{id}", promoId)
                .contentType(Objects.requireNonNull(JSON))
                .content(Objects.requireNonNull(mapper.writeValueAsString(Map.of(
                    "startTime", start.toString(),
                    "endTime", start.minusSeconds(60).toString()
                )))))
           .andExpect(status().isBadRequest());

        mvc.perform(patch("/promotions/{id}", promoId)
                .contentType(Objects.requireNonNull(JSON))
                .content(Objects.requireNonNull(mapper.writeValueAsString(Map.of(
                    "discountRate", -0.1
                )))))
           .andExpect(status().isBadRequest());
    }

    @Test
    void patchPromotion_rejects_discount_rate_above_one() throws Exception {
        var promoId = Objects.requireNonNull(UUID.randomUUID());

        mvc.perform(patch("/promotions/{id}", promoId)
                .contentType(Objects.requireNonNull(JSON))
                .content(Objects.requireNonNull(mapper.writeValueAsString(Map.of(
                    "discountRate", 1.1
                )))))
           .andExpect(status().isBadRequest());

        verify(promotionService, never()).patchPromotion(any(UUID.class), any(), any(), any(), any());
    }

    @Test
    void patchPromotion_updates_when_found() throws Exception {
        var promoId = Objects.requireNonNull(UUID.randomUUID());
        var start = Instant.parse("2025-05-01T00:00:00Z");
        var end = Instant.parse("2025-06-01T00:00:00Z");
        var patched = new Promotion("Patched", start, end, 0.4);
        patched.setId(promoId);
        when(promotionService.patchPromotion(eq(promoId), any(), any(), any(), any())).thenReturn(Optional.of(patched));

        mvc.perform(patch("/promotions/{id}", promoId)
                .contentType(Objects.requireNonNull(JSON))
                .content(Objects.requireNonNull(mapper.writeValueAsString(Map.of(
                    "name", "Patched",
                    "startTime", start.toString(),
                    "endTime", end.toString(),
                    "discountRate", 0.4
                )))))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.name").value("Patched"))
           .andExpect(jsonPath("$.discountRate").value(0.4));
    }

    @Test
    void patchPromotion_returns_not_found_when_missing() throws Exception {
        var promoId = Objects.requireNonNull(UUID.randomUUID());
        when(promotionService.patchPromotion(eq(promoId), any(), any(), any(), any())).thenReturn(Optional.empty());

        mvc.perform(patch("/promotions/{id}", promoId)
                .contentType(Objects.requireNonNull(JSON))
                .content(Objects.requireNonNull(mapper.writeValueAsString(Map.of("name", "X")))))
           .andExpect(status().isNotFound());
    }

    @Test
    void deletePromotion_returns_status_from_service() throws Exception {
        var promoId = Objects.requireNonNull(UUID.randomUUID());
        when(promotionService.deletePromotion(promoId)).thenReturn(true);

        mvc.perform(delete("/promotions/{id}", promoId))
           .andExpect(status().isNoContent());

        when(promotionService.deletePromotion(promoId)).thenReturn(false);

        mvc.perform(delete("/promotions/{id}", promoId))
           .andExpect(status().isNotFound());
    }

    private String promotionPayload(String name, String startTime, String endTime, Double rate) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", name);
        payload.put("startTime", startTime);
        payload.put("endTime", endTime);
        payload.put("discountRate", rate);
        return mapper.writeValueAsString(payload);
    }
}
