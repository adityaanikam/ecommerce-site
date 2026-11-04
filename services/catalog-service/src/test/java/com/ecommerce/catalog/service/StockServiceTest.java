package com.ecommerce.catalog.service;

import com.ecommerce.catalog.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache productsCache;

    private StockService stockService;

    @BeforeEach
    void setUp() {
        stockService = new StockService(mongoTemplate, cacheManager);
    }

    @Test
    @DisplayName("decrement issues one atomic findAndModify guarded on available stock")
    void decrementIsAtomicAndGuarded() {
        Product after = Product.builder().id("ELEC001").stock(7).build();
        when(cacheManager.getCache("products")).thenReturn(productsCache);
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class),
                any(FindAndModifyOptions.class), eq(Product.class))).thenReturn(after);

        Product result = stockService.decrementStock("ELEC001", 3);

        assertThat(result).isNotNull();
        assertThat(result.getStock()).isEqualTo(7);

        ArgumentCaptor<Query> query = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> update = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate).findAndModify(query.capture(), update.capture(),
                any(FindAndModifyOptions.class), eq(Product.class));

        // The guard is the important part: the query only matches when there is
        // already at least `quantity` in stock, so two concurrent decrements cannot
        // both succeed off the same starting value and stock cannot go negative.
        String queryJson = query.getValue().getQueryObject().toJson();
        assertThat(queryJson).contains("ELEC001").contains("$gte");
        assertThat(update.getValue().getUpdateObject().toJson()).contains("$inc").contains("-3");
    }

    @Test
    @DisplayName("a successful decrement evicts that product from the cache")
    void decrementEvictsCache() {
        when(cacheManager.getCache("products")).thenReturn(productsCache);
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class),
                any(FindAndModifyOptions.class), eq(Product.class)))
                .thenReturn(Product.builder().id("ELEC001").stock(1).build());

        stockService.decrementStock("ELEC001", 1);

        verify(productsCache).evictIfPresent("ELEC001");
    }

    @Test
    @DisplayName("insufficient stock returns null and does not touch the cache")
    void insufficientStockReturnsNull() {
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class),
                any(FindAndModifyOptions.class), eq(Product.class))).thenReturn(null);

        assertThat(stockService.decrementStock("ELEC001", 999)).isNull();

        verifyNoInteractions(productsCache);
        verify(productsCache, never()).evictIfPresent(any());
    }

    @Test
    @DisplayName("a non-positive quantity is rejected before reaching the database")
    void rejectsNonPositiveQuantity() {
        assertThatThrownBy(() -> stockService.decrementStock("ELEC001", 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> stockService.decrementStock("ELEC001", -5))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(mongoTemplate);
    }
}
