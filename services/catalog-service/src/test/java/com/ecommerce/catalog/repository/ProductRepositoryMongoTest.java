package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.DockerAvailable;
import com.ecommerce.catalog.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Data slice against a real MongoDB, because the queries that matter here are
 * MongoDB queries: a case insensitive regex search and a $gte/$lte price range.
 * An in-memory stand-in would not prove those behave the same way.
 */
@DataMongoTest
@Testcontainers
@DockerAvailable
@TestPropertySource(properties = "catalog.seed.enabled=false")
class ProductRepositoryMongoTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void seed() {
        productRepository.deleteAll();
        productRepository.saveAll(List.of(
                product("ELEC001", "iPhone 15 Pro", "Electronics", "Mobiles", 999.99, 50, true, false),
                product("ELEC002", "Samsung Galaxy S24", "Electronics", "Mobiles", 899.99, 45, false, true),
                product("ELEC003", "Dell XPS 13", "Electronics", "Laptops", 1299.00, 10, true, true),
                product("FASH001", "Basic White Tee", "Fashion", "T-shirts", 18.00, 120, false, false)
        ));
    }

    private static Product product(String id, String name, String category, String subcategory,
                                   double price, int stock, boolean featured, boolean onSale) {
        return Product.builder()
                .id(id).name(name).category(category).subcategory(subcategory)
                .price(price).stock(stock).isFeatured(featured).isOnSale(onSale)
                .slug(id.toLowerCase())
                .build();
    }

    @Test
    @DisplayName("findByCategory pages within one category only")
    void findByCategoryPages() {
        var page = productRepository.findByCategory("Electronics", PageRequest.of(0, 2, Sort.by("name")));
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).allMatch(p -> p.getCategory().equals("Electronics"));
    }

    @Test
    @DisplayName("findByCategoryAndSubcategory narrows to both")
    void findByCategoryAndSubcategory() {
        var page = productRepository.findByCategoryAndSubcategory(
                "Electronics", "Laptops", PageRequest.of(0, 10));
        assertThat(page.getContent()).extracting(Product::getId).containsExactly("ELEC003");
    }

    @Test
    @DisplayName("name search is a case insensitive substring match, as in the monolith")
    void nameSearchIsCaseInsensitiveSubstring() {
        var page = productRepository.findByNameContainingIgnoreCase("iphone", PageRequest.of(0, 10));
        assertThat(page.getContent()).extracting(Product::getId).containsExactly("ELEC001");

        var partial = productRepository.findByNameContainingIgnoreCase("GALAXY", PageRequest.of(0, 10));
        assertThat(partial.getContent()).extracting(Product::getId).containsExactly("ELEC002");
    }

    @Test
    @DisplayName("price range is inclusive at both ends")
    void priceRangeIsInclusive() {
        var page = productRepository.findByPriceRange(899.99, 999.99, PageRequest.of(0, 10, Sort.by("price")));
        assertThat(page.getContent()).extracting(Product::getId)
                .containsExactly("ELEC002", "ELEC001");
    }

    @Test
    @DisplayName("flag queries and the category count work off the stored booleans")
    void flagQueriesAndCount() {
        assertThat(productRepository.findByIsFeatured(true)).extracting(Product::getId)
                .containsExactlyInAnyOrder("ELEC001", "ELEC003");
        assertThat(productRepository.findByIsOnSale(true)).extracting(Product::getId)
                .containsExactlyInAnyOrder("ELEC002", "ELEC003");
        assertThat(productRepository.countByCategory("Electronics")).isEqualTo(3);
        assertThat(productRepository.countByCategory("Fashion")).isEqualTo(1);
    }

    @Test
    @DisplayName("findBySlug resolves the slug the seeder derives from the id")
    void findBySlug() {
        assertThat(productRepository.findBySlug("elec001")).isPresent()
                .get().extracting(Product::getName).isEqualTo("iPhone 15 Pro");
    }
}
