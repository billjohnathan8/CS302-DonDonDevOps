package org.ddk.promotions.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PromotionTest {
    @Test
    void isActiveAt_respects_boundaries() {
        var start = Instant.parse("2025-10-29T00:00:00Z");
        var end = Instant.parse("2025-10-30T00:00:00Z");
        var p = new Promotion("BF", start, end, 0.15);

        assertNotNull(p.getId());
        assertFalse(p.isActiveAt(start.minusSeconds(1)));
        assertTrue(p.isActiveAt(start));
        assertTrue(p.isActiveAt(start.plusSeconds(3600)));
        assertTrue(p.isActiveAt(end));
        assertFalse(p.isActiveAt(end.plusSeconds(1)));
    }

    @Test
    void getters_and_setters_work() {
        var p = new Promotion();
        var start = Instant.parse("2025-01-01T00:00:00Z");
        var end = Instant.parse("2025-12-31T23:59:59Z");

        p.setName("Holiday");
        p.setStartTime(start);
        p.setEndTime(end);
        p.setDiscountRate(0.25);

        assertEquals("Holiday", p.getName());
        assertEquals(start, p.getStartTime());
        assertEquals(end, p.getEndTime());
        assertEquals(0.25, p.getDiscountRate(), 1e-9);
        assertNotNull(p.getId());
    }
}
