package org.ddk.promotions.dto;

import java.util.List;

/**
 * Request payload for the apply API that evaluates multiple products against the
 * current promotion catalog.
 *
 * @param now ISO-8601 timestamp (UTC) used to evaluate promotions; when {@code null}
 *            the service falls back to the current instant.
 * @param items Collection of products that should be evaluated for discounts.
 */
public record ApplyRequest(String now, List<ApplyRequestItem> items) {}
