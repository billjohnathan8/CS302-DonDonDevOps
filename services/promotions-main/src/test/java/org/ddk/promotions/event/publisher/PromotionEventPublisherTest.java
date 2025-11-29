package org.ddk.promotions.event.publisher;

import org.ddk.promotions.config.RabbitMQConfig;
import org.ddk.promotions.dto.PromotionEndedEvent;
import org.ddk.promotions.dto.PromotionProductUpdatedEvent;
import org.ddk.promotions.dto.PromotionStartedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PromotionEventPublisher.
 * Tests verify that events are published to the correct exchange with correct routing keys.
 */
@ExtendWith(MockitoExtension.class)
class PromotionEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private PromotionEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new PromotionEventPublisher(rabbitTemplate);
    }

    @Test
    void publishPromotionStarted_shouldPublishToCorrectExchangeWithCorrectRoutingKey() {
        // Given
        UUID promotionId = UUID.randomUUID();
        Instant startDate = Instant.now();
        Instant endDate = startDate.plusSeconds(86400); // 1 day later
        PromotionStartedEvent event = new PromotionStartedEvent(
            promotionId, "Summer Sale", startDate, endDate
        );

        // When
        publisher.publishPromotionStarted(event);

        // Then
        ArgumentCaptor<PromotionStartedEvent> eventCaptor = ArgumentCaptor.forClass(PromotionStartedEvent.class);
        verify(rabbitTemplate).convertAndSend(
            eq(RabbitMQConfig.PROMOTIONS_EXCHANGE),
            eq(RabbitMQConfig.PROMOTION_STARTED_KEY),
            eventCaptor.capture()
        );

        PromotionStartedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(promotionId, capturedEvent.promotionId());
        assertEquals("Summer Sale", capturedEvent.name());
        assertEquals("promotion.started", capturedEvent.eventType());
    }

    @Test
    void publishPromotionStarted_whenRabbitThrowsException_shouldLogErrorAndNotPropagateException() {
        // Given
        PromotionStartedEvent event = new PromotionStartedEvent(
            UUID.randomUUID(), "Test", Instant.now(), Instant.now().plusSeconds(3600)
        );
        doThrow(new RuntimeException("RabbitMQ connection failed"))
            .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        // When / Then - should not throw exception
        assertDoesNotThrow(() -> publisher.publishPromotionStarted(event));
    }

    @Test
    void publishPromotionEnded_shouldPublishToCorrectExchangeWithCorrectRoutingKey() {
        // Given
        UUID promotionId = UUID.randomUUID();
        PromotionEndedEvent event = new PromotionEndedEvent(promotionId);

        // When
        publisher.publishPromotionEnded(event);

        // Then
        ArgumentCaptor<PromotionEndedEvent> eventCaptor = ArgumentCaptor.forClass(PromotionEndedEvent.class);
        verify(rabbitTemplate).convertAndSend(
            eq(RabbitMQConfig.PROMOTIONS_EXCHANGE),
            eq(RabbitMQConfig.PROMOTION_ENDED_KEY),
            eventCaptor.capture()
        );

        PromotionEndedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(promotionId, capturedEvent.promotionId());
        assertEquals("promotion.ended", capturedEvent.eventType());
    }

    @Test
    void publishPromotionProductUpdated_shouldPublishToCorrectExchangeWithCorrectRoutingKey() {
        // Given
        UUID promotionId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        double discountRate = 0.25;
        PromotionProductUpdatedEvent event = new PromotionProductUpdatedEvent(
            promotionId, productId, discountRate
        );

        // When
        publisher.publishPromotionProductUpdated(event);

        // Then
        ArgumentCaptor<PromotionProductUpdatedEvent> eventCaptor =
            ArgumentCaptor.forClass(PromotionProductUpdatedEvent.class);
        verify(rabbitTemplate).convertAndSend(
            eq(RabbitMQConfig.PROMOTIONS_EXCHANGE),
            eq(RabbitMQConfig.PROMOTION_PRODUCT_UPDATED_KEY),
            eventCaptor.capture()
        );

        PromotionProductUpdatedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(promotionId, capturedEvent.promotionId());
        assertEquals(productId, capturedEvent.productId());
        assertEquals(discountRate, capturedEvent.discountRate());
        assertEquals("promotion.product_updated", capturedEvent.eventType());
    }
}
