package org.ddk.promotions.dto;

/**
 * Discount calculation result for a single product.
 *
 * @param productId Identifier of the evaluated product.
 * @param discountRate Best promotion rate expressed between 0 and 1.
 * @param discountAmount Monetary amount subtracted from the original unit price.
 * @param finalUnitPrice Unit price after the discount has been applied.
 */
public record ApplyResponseItem(String productId, double discountRate, double discountAmount, double finalUnitPrice) {}
