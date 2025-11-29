package org.ddk.promotions.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.ddk.promotions.model.Promotion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@ExtendWith(MockitoExtension.class)
class PromotionRepositoryUnitTest {

    @Mock DynamoDbClient dynamoDbClient;
    @Mock DynamoDbEnhancedClient enhancedClient;
    @Mock DynamoDbTable<Promotion> table;

    PromotionRepository repository;

    @BeforeEach
    void setUp() {
        when(
                enhancedClient.table(
                    anyString(),
                    ArgumentMatchers.<TableSchema<Promotion>>any()))
            .thenReturn(table);
        repository = new PromotionRepository(dynamoDbClient, enhancedClient);
    }

    @Test
    void findById_returns_item_when_present() {
        UUID id = UUID.randomUUID();
        Promotion promo = promotion(id, 0.3);
        when(table.getItem(any(Key.class))).thenReturn(promo);

        assertTrue(repository.findById(id).isPresent());
    }

    @Test
    void findAll_returns_every_item_from_scan() {
        Promotion first = promotion(UUID.randomUUID(), 0.1);
        Promotion second = promotion(UUID.randomUUID(), 0.2);
        when(table.scan()).thenReturn(scanPage(first, second));

        List<Promotion> all = repository.findAll();

        assertEquals(List.of(first, second), all);
    }

    @Test
    void existsById_checks_table_directly() {
        UUID id = UUID.randomUUID();
        when(table.getItem(any(Key.class))).thenReturn(promotion(id, 0.5));

        assertTrue(repository.existsById(id));
    }

    @Test
    void deleteAll_removes_every_item_from_scan() {
        Promotion first = promotion(UUID.randomUUID(), 0.1);
        Promotion second = promotion(UUID.randomUUID(), 0.2);
        when(table.scan()).thenReturn(scanPage(first, second));

        repository.deleteAll();

        ArgumentCaptor<Key> captor = ArgumentCaptor.forClass(Key.class);
        verify(table, times(2)).deleteItem(captor.capture());
        List<String> deletedIds = captor.getAllValues().stream()
            .map(key -> key.partitionKeyValue().s())
            .toList();
        assertEquals(List.of(first.getId().toString(), second.getId().toString()), deletedIds);
    }

    private static Promotion promotion(UUID id, double discountRate) {
        Promotion promotion = new Promotion();
        promotion.setId(id);
        promotion.setName("Promo-" + id);
        promotion.setStartTime(Instant.parse("2025-01-01T00:00:00Z"));
        promotion.setEndTime(Instant.parse("2025-12-31T00:00:00Z"));
        promotion.setDiscountRate(discountRate);
        return promotion;
    }

    private static PageIterable<Promotion> scanPage(Promotion... items) {
        List<Promotion> list = Arrays.asList(items);
        SdkIterable<Page<Promotion>> pages =
            () -> List.of(Page.builder(Promotion.class).items(list).build()).iterator();
        return PageIterable.create(pages);
    }
}
