package com.ecommerce.catalog.seed;

import com.ecommerce.catalog.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The monolith re-rolled these flags from a fresh Random on every boot, which is why
 * prices moved on every restart. They are now derived from the product id, so the
 * same catalog always looks the same, which is what lets smoke.sh assert on it.
 */
class ProductSeederDecorationTest {

    private static Product product(String id, double price) {
        return Product.builder().id(id).price(price).build();
    }

    @Test
    @DisplayName("decoration is deterministic for a given product id")
    void decorationIsDeterministic() {
        long now = 1_700_000_000_000L;
        Product first = product("ELEC001", 999.99);
        Product second = product("ELEC001", 999.99);

        ProductSeeder.decorate(first, now);
        ProductSeeder.decorate(second, now);

        assertThat(first.isFeatured()).isEqualTo(second.isFeatured());
        assertThat(first.isOnSale()).isEqualTo(second.isOnSale());
        assertThat(first.getDiscountPrice()).isEqualTo(second.getDiscountPrice());
        assertThat(first.getCreatedAt()).isEqualTo(second.getCreatedAt());
    }

    @Test
    @DisplayName("a sale price is below list price, and a non-sale product has no sale price")
    void salePriceIsCoherent() {
        long now = System.currentTimeMillis();
        for (String id : new String[]{"ELEC001", "ELEC002", "FASH001", "SPORT001", "HOME001",
                "ELEC010", "ELEC020", "FASH010", "SPORT005", "HOME005"}) {
            Product p = product(id, 100.0);
            ProductSeeder.decorate(p, now);
            if (p.isOnSale()) {
                assertThat(p.getDiscountPrice()).isGreaterThan(0.0).isLessThan(p.getPrice());
                // 30% to 70% off, so between 30 and 70 of a 100 unit price.
                assertThat(p.getDiscountPrice()).isBetween(29.0, 70.0);
            } else {
                assertThat(p.getDiscountPrice()).isZero();
            }
        }
    }

    @Test
    @DisplayName("a slug is derived from the id when the seed data has none")
    void slugIsDerived() {
        Product p = product("ELEC001", 10.0);
        ProductSeeder.decorate(p, System.currentTimeMillis());
        assertThat(p.getSlug()).isEqualTo("elec001");
    }

    @Test
    @DisplayName("an existing createdAt is respected rather than overwritten")
    void existingCreatedAtIsKept() {
        Product p = product("ELEC001", 10.0);
        p.setCreatedAt(12345L);
        ProductSeeder.decorate(p, System.currentTimeMillis());
        assertThat(p.getCreatedAt()).isEqualTo(12345L);
    }
}
