package com.ecommerce.catalog.seed;

import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Seeds the catalog on first boot.
 *
 * Two deliberate differences from the monolith's DataInitializer, both of which it
 * had to have for this service to be able to own stock at all:
 *
 *  1. It seeds only when the collection is empty. The monolith called deleteAll() and
 *     reseeded on EVERY boot, so any stock decrement, and every price, was thrown
 *     away on the next restart. A catalog that is the system of record for stock
 *     cannot do that.
 *
 *  2. The featured / new / on-sale flags and the sale discount are derived
 *     deterministically from the product id instead of from a fresh Random on each
 *     boot. Same distribution, same look in the UI, but now reproducible, which is
 *     what makes the stock tests and scripts/smoke.sh assertable.
 *
 * The product data itself is the monolith's hardcoded seed classes, carried over
 * verbatim. Note that the monolith also tried to read ecommerce.products.json first
 * and ALWAYS failed with `Unrecognized field "_id"`, silently falling back to these
 * classes, so these classes are what production has always actually served. That
 * dead JSON path is not carried over.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "catalog.seed.enabled", havingValue = "true", matchIfMissing = true)
public class ProductSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductSeeder.class);

    private static final long THIRTY_DAYS_MILLIS = 30L * 24 * 60 * 60 * 1000;

    private final ProductRepository productRepository;

    @Override
    public void run(ApplicationArguments args) {
        long existing = productRepository.count();
        if (existing > 0) {
            log.info("Catalog already holds {} products, leaving it alone", existing);
            return;
        }

        List<Product> products = new ArrayList<>();
        products.addAll(ElectronicsProducts.getProducts());
        products.addAll(FashionProducts.getProducts());
        products.addAll(FashionProductsExtra.getProducts());
        products.addAll(HomeAndGardenProducts.getProducts());
        products.addAll(SportsProducts.getProducts());

        long now = System.currentTimeMillis();
        for (Product product : products) {
            decorate(product, now);
        }

        productRepository.saveAll(products);
        log.info("Seeded {} products into an empty catalog", products.size());
    }

    /**
     * Derives the merchandising flags from a stable hash of the product id, so the
     * same product always gets the same badges and the same sale price.
     */
    static void decorate(Product product, long now) {
        int hash = Math.abs(product.getId().hashCode());

        // Roughly one product in five is featured, as before.
        product.setFeatured(hash % 5 == 0);

        if (product.getCreatedAt() == 0) {
            // Spread creation dates over the last 60 days, deterministically.
            product.setCreatedAt(now - (hash % 60) * 24L * 60 * 60 * 1000);
        }
        product.setNew(now - product.getCreatedAt() < THIRTY_DAYS_MILLIS);

        // Roughly three products in ten go on sale, at 30% to 70% off, as before.
        if (hash % 10 < 3) {
            double discountPercentage = 30 + (hash % 41);
            double discounted = product.getPrice() * (1 - discountPercentage / 100.0);
            product.setDiscountPrice(Math.round(discounted * 100.0) / 100.0);
            product.setOnSale(true);
        } else {
            product.setDiscountPrice(0.0);
            product.setOnSale(false);
        }

        if (product.getUpdatedAt() == 0) {
            product.setUpdatedAt(product.getCreatedAt());
        }
        if (product.getSlug() == null || product.getSlug().isEmpty()) {
            product.setSlug(product.getId().toLowerCase());
        }
    }
}
