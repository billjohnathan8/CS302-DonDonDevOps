package org.ddk.promotions.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.ddk.promotions.model.ProductPromotion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

@ExtendWith(MockitoExtension.class)
class ProductPromotionRepositoryUnitTest {

    @Mock DynamoDbClient dynamoDbClient;
    @Mock DynamoDbEnhancedClient enhancedClient;
    @Mock DynamoDbTable<ProductPromotion> table;

    ProductPromotionRepository repository;

    @BeforeEach
    void setUp() {
        when(enhancedClient.table(anyString(), ArgumentMatchers.<TableSchema<ProductPromotion>>any()))
            .thenReturn(table);
        repository = new ProductPromotionRepository(dynamoDbClient, enhancedClient);
    }

    @Test
    void findByProductId_filters_scan_results() {
        UUID productA = UUID.randomUUID();
        UUID productB = UUID.randomUUID();
        ProductPromotion a1 = link(UUID.randomUUID(), UUID.randomUUID(), productA);
        ProductPromotion a2 = link(UUID.randomUUID(), UUID.randomUUID(), productA);
        ProductPromotion b = link(UUID.randomUUID(), UUID.randomUUID(), productB);
        when(table.scan()).thenReturn(scanPage(a1, a2, b));

        List<ProductPromotion> results = repository.findByProductId(productA);

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(pp -> pp.getProductId().equals(productA)));
    }

    @Test
    void findByPromotionId_filters_scan_results() {
        UUID promotion = UUID.randomUUID();
        UUID otherPromotion = UUID.randomUUID();
        ProductPromotion first = link(UUID.randomUUID(), promotion, UUID.randomUUID());
        ProductPromotion second = link(UUID.randomUUID(), promotion, UUID.randomUUID());
        ProductPromotion other = link(UUID.randomUUID(), otherPromotion, UUID.randomUUID());
        when(table.scan()).thenReturn(scanPage(first, second, other));

        List<ProductPromotion> results = repository.findByPromotionId(promotion);

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(pp -> pp.getPromotionId().equals(promotion)));
    }

    @Test
    void deleteAll_collection_removes_each_key() {
        ProductPromotion one = link(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        ProductPromotion two = link(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        repository.deleteAll(List.of(one, two));

        ArgumentCaptor<Key> captor = ArgumentCaptor.forClass(Key.class);
        verify(table, times(2)).deleteItem(captor.capture());
        List<String> deletedIds = captor.getAllValues().stream()
            .map(key -> key.partitionKeyValue().s())
            .toList();
        assertTrue(deletedIds.containsAll(List.of(one.getId().toString(), two.getId().toString())));
    }

    @Test
    void deleteAll_removes_every_item_from_scan() {
        ProductPromotion one = link(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        ProductPromotion two = link(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        when(table.scan()).thenReturn(scanPage(one, two));

        repository.deleteAll();

        ArgumentCaptor<Key> captor = ArgumentCaptor.forClass(Key.class);
        verify(table, times(2)).deleteItem(captor.capture());
        List<String> deletedIds = captor.getAllValues().stream()
            .map(key -> key.partitionKeyValue().s())
            .toList();
        assertEquals(List.of(one.getId().toString(), two.getId().toString()), deletedIds);
    }

    private static ProductPromotion link(UUID id, UUID promotionId, UUID productId) {
        return new ProductPromotion(id, promotionId, productId);
    }

    private static PageIterable<ProductPromotion> scanPage(ProductPromotion... items) {
        List<ProductPromotion> list = Arrays.asList(items);
        SdkIterable<Page<ProductPromotion>> pages =
            () -> List.of(Page.builder(ProductPromotion.class).items(list).build()).iterator();
        return PageIterable.create(pages);
    }
}
