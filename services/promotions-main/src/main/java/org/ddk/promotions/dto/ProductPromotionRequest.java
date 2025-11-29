package org.ddk.promotions.dto;

import java.util.UUID; 

public record ProductPromotionRequest(UUID promotionId, UUID productId) {}