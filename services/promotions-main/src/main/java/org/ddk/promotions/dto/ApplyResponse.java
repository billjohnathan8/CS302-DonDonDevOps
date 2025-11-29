package org.ddk.promotions.dto;

import java.util.List;

/**
 * Response payload returned by the apply API summarizing discount outcomes.
 *
 * @param items Per-product discount details calculated by the service.
 */
public record ApplyResponse(List<ApplyResponseItem> items) {}
