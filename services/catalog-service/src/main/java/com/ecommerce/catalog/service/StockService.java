package com.ecommerce.catalog.service;

import com.ecommerce.catalog.model.Product;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

/**
 * The only writer of Product.stock.
 *
 * Decrementing uses a single atomic findAndModify with a guard on the current stock
 * level, so two concurrent decrements cannot both read 5 and both write 4, and stock
 * can never be driven below zero by a race. The guard is the reason this is not a
 * read-modify-write in application code.
 */
@Service
@RequiredArgsConstructor
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    private final MongoTemplate mongoTemplate;
    private final CacheManager cacheManager;

    /**
     * Atomically decrement stock, refusing to go below zero.
     *
     * @return the product as it is after the decrement, or null when the product does
     *         not exist or does not have enough stock left.
     */
    public Product decrementStock(String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive, got " + quantity);
        }

        Query query = Query.query(Criteria.where("_id").is(productId).and("stock").gte(quantity));
        Update update = new Update().inc("stock", -quantity).set("updatedAt", System.currentTimeMillis());

        Product updated = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                Product.class
        );

        if (updated == null) {
            log.warn("Stock not decremented for product {} by {}: product missing or insufficient stock",
                    productId, quantity);
            return null;
        }

        // The write just invalidated the cached copy of this product. Evicting here,
        // next to the write itself, is what keeps the invalidation rule simple:
        // the only thing that mutates a product is a stock change, and a stock change
        // always evicts that product's key.
        evict(productId);
        log.info("Decremented stock for product {} by {}, now {}", productId, quantity, updated.getStock());
        return updated;
    }

    private void evict(String productId) {
        var products = cacheManager.getCache("products");
        if (products != null) {
            products.evictIfPresent(productId);
        }
    }
}
