package com.ecommerce.catalog.service;

import com.ecommerce.catalog.DockerAvailable;
import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the Redis cache is real, against a real Redis.
 *
 * This test exists because of a bug that unit tests could not have caught: the cached
 * read used to be a second method that the public one called, and a call from inside
 * the same bean skips the Spring proxy, so @Cacheable never fired and Redis stayed
 * empty while every assertion about behaviour still passed. The only way to catch
 * that is to look in Redis.
 *
 * It also pins the invalidation rule: a stock decrement must remove that product's
 * key, or the catalog would keep serving a stock level it no longer has.
 */
@SpringBootTest
@Testcontainers
@DockerAvailable
@TestPropertySource(properties = {
        "catalog.seed.enabled=false",
        "spring.cache.type=redis",
        "spring.kafka.listener.auto-startup=false"
})
class ProductCacheTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @Container
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine")).withExposedPorts(6379);

    @Autowired
    private ProductService productService;

    @Autowired
    private StockService stockService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        productRepository.save(Product.builder()
                .id("ELEC001").name("iPhone 15 Pro").price(999.99).stock(10).category("Electronics").build());
        var cache = cacheManager.getCache("products");
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    @DisplayName("reading a product by id writes it into Redis under products::{id}")
    void readPopulatesRedis() {
        assertThat(redisTemplate.hasKey("products::ELEC001")).isFalse();

        Optional<Product> product = productService.findById("ELEC001");

        assertThat(product).isPresent();
        assertThat(redisTemplate.hasKey("products::ELEC001"))
                .as("the @Cacheable read must actually reach Redis, not just return the right value")
                .isTrue();
    }

    @Test
    @DisplayName("a second read is served from the cache even after the database row changes underneath")
    void secondReadIsServedFromCache() {
        assertThat(productService.findById("ELEC001")).get().extracting(Product::getStock).isEqualTo(10);

        // Change the document behind the service's back, bypassing StockService so
        // nothing evicts. A cached second read must still show the old value, which
        // is what proves the first read was cached rather than re-queried.
        Product direct = productRepository.findById("ELEC001").orElseThrow();
        direct.setStock(3);
        productRepository.save(direct);

        assertThat(productService.findById("ELEC001")).get().extracting(Product::getStock).isEqualTo(10);
    }

    @Test
    @DisplayName("a stock decrement evicts that product, so the next read sees the new stock")
    void decrementEvictsSoNextReadIsFresh() {
        assertThat(productService.findById("ELEC001")).get().extracting(Product::getStock).isEqualTo(10);
        assertThat(redisTemplate.hasKey("products::ELEC001")).isTrue();

        stockService.decrementStock("ELEC001", 4);

        assertThat(redisTemplate.hasKey("products::ELEC001"))
                .as("the write must invalidate the cached copy")
                .isFalse();
        assertThat(productService.findById("ELEC001")).get().extracting(Product::getStock).isEqualTo(6);
    }

    @Test
    @DisplayName("a miss is not cached, so an unknown id does not fill Redis with empties")
    void missIsNotCached() {
        assertThat(productService.findById("NOPE")).isEmpty();
        assertThat(redisTemplate.hasKey("products::NOPE")).isFalse();
    }

    @Test
    @DisplayName("the category tree is cached under its own single key")
    void categoryTreeIsCached() {
        productService.getAllCategories();
        assertThat(redisTemplate.hasKey("categoryTree::all")).isTrue();
    }
}
