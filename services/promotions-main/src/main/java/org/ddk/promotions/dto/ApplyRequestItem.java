package org.ddk.promotions.dto;

/**
 * Individual line item in an {@link ApplyRequest}.
 *
 * @param productId Identifier of the product being priced.
 * @param quantity Quantity purchased so volume-sensitive logic can be applied.
 * @param unitPrice Original unit price before any promotion is applied.
 */
public record ApplyRequestItem(String productId, int quantity, double unitPrice) {}
