package com.ecommerce.catalog.service;

import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Read side of the catalog.
 *
 * The findProducts cascade is carried over from the monolith exactly as it was,
 * including its quirks: search beats every other filter and matches on name only,
 * and a price range is honoured only when no category, subcategory or search was
 * given. Preserving the quirks is deliberate, because the live frontend was built
 * against this behaviour.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Page<Product> findProducts(
            String category,
            String subcategory,
            String search,
            Double minPrice,
            Double maxPrice,
            PageRequest pageRequest
    ) {
        if (search != null && !search.isEmpty()) {
            return productRepository.findByNameContainingIgnoreCase(search, pageRequest);
        }
        if (category != null && subcategory != null) {
            return productRepository.findByCategoryAndSubcategory(category, subcategory, pageRequest);
        }
        if (category != null) {
            return productRepository.findByCategory(category, pageRequest);
        }
        if (subcategory != null) {
            return productRepository.findBySubcategory(subcategory, pageRequest);
        }
        if (minPrice != null && maxPrice != null) {
            return productRepository.findByPriceRange(minPrice, maxPrice, pageRequest);
        }
        return productRepository.findAll(pageRequest);
    }

    /**
     * The one read that is worth caching: a single product by id, which is what the
     * product detail page and order-service's re-pricing call both hit.
     *
     * Cached under key "products::{id}". Evicted by StockService whenever that
     * product's stock changes, which is the only write path in this service.
     * Collection reads are not cached, because a Page is awkward to serialise and
     * every list query carries a different filter and sort combination, so the hit
     * rate would be poor and the invalidation rule would stop being obvious.
     *
     * @Cacheable sits on the method callers actually call. An earlier version had a
     * separate cached method that this one delegated to, which silently cached
     * nothing: a call from inside the same bean does not go through the Spring proxy,
     * so the annotation never fired. ProductCacheTest asserts the key really lands
     * in Redis so that cannot come back.
     *
     * A miss is not cached, hence the unless clause: the Redis cache is configured
     * with disableCachingNullValues, and an unknown product id is usually a typo or
     * a scan rather than something worth remembering.
     *
     * The unless expression compares against null rather than calling isPresent():
     * Spring unwraps an Optional return value before evaluating the expression, so
     * #result here is the Product itself, or null on a miss.
     */
    @Cacheable(cacheNames = "products", key = "#id", unless = "#result == null")
    public Optional<Product> findById(String id) {
        return productRepository.findById(id);
    }

    public Page<Product> findByCategory(String category, PageRequest pageRequest) {
        return productRepository.findByCategory(category, pageRequest);
    }

    public List<Product> findFeaturedProducts() {
        return productRepository.findByIsFeatured(true);
    }

    public List<Product> findNewProducts() {
        return productRepository.findByIsNew(true);
    }

    public List<Product> findOnSaleProducts() {
        return productRepository.findByIsOnSale(true);
    }

    /**
     * Kept as a full scan to match the monolith's output exactly. Cached, because it
     * reads every document to build the map and the result only changes when the
     * catalog itself changes.
     */
    @Cacheable(cacheNames = "categoryTree", key = "'all'")
    public Map<String, List<String>> getAllCategories() {
        List<Product> products = productRepository.findAll();
        Map<String, Set<String>> categoryMap = new HashMap<>();
        for (Product product : products) {
            categoryMap.computeIfAbsent(product.getCategory(), k -> new HashSet<>())
                    .add(product.getSubcategory());
        }
        return categoryMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue())));
    }

    public long countByCategory(String category) {
        return productRepository.countByCategory(category);
    }
}
