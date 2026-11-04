package com.ecommerce.catalog.service;

import com.ecommerce.catalog.DockerAvailable;
import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the stock decrement is actually atomic, against a real MongoDB.
 *
 * The concurrency case is the reason this test exists: with a read-modify-write in
 * application code, twenty threads each taking one unit from a stock of ten would
 * oversell. With the guarded findAndModify, exactly ten of them succeed.
 */
@DataMongoTest
@Testcontainers
@DockerAvailable
@TestPropertySource(properties = "catalog.seed.enabled=false")
class StockServiceMongoTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ProductRepository productRepository;

    private StockService stockService;

    @BeforeEach
    void setUp() {
        // NoOpCacheManager rather than Redis: this test is about the database write,
        // and the eviction behaviour is covered by StockServiceTest.
        stockService = new StockService(mongoTemplate, new NoOpCacheManager());
        productRepository.deleteAll();
        productRepository.save(Product.builder().id("ELEC001").name("iPhone").stock(10).price(999.0).build());
    }

    @Test
    @DisplayName("a decrement reduces stock by exactly the quantity")
    void decrementReducesStock() {
        Product after = stockService.decrementStock("ELEC001", 3);

        assertThat(after).isNotNull();
        assertThat(after.getStock()).isEqualTo(7);
        assertThat(productRepository.findById("ELEC001")).get()
                .extracting(Product::getStock).isEqualTo(7);
    }

    @Test
    @DisplayName("stock cannot be driven below zero")
    void cannotGoNegative() {
        assertThat(stockService.decrementStock("ELEC001", 11)).isNull();
        assertThat(productRepository.findById("ELEC001")).get()
                .extracting(Product::getStock).isEqualTo(10);
    }

    @Test
    @DisplayName("an unknown product id is a no-op rather than an error")
    void unknownProductIsNoOp() {
        assertThat(stockService.decrementStock("DOES-NOT-EXIST", 1)).isNull();
    }

    @Test
    @DisplayName("twenty concurrent decrements of one unit against a stock of ten succeed exactly ten times")
    void concurrentDecrementsDoNotOversell() throws Exception {
        int threads = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Callable<Boolean>> tasks = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                tasks.add(() -> stockService.decrementStock("ELEC001", 1) != null);
            }
            List<Future<Boolean>> results = pool.invokeAll(tasks);

            long succeeded = 0;
            for (Future<Boolean> result : results) {
                if (result.get()) {
                    succeeded++;
                }
            }

            assertThat(succeeded).isEqualTo(10);
            assertThat(productRepository.findById("ELEC001")).get()
                    .extracting(Product::getStock).isEqualTo(0);
        } finally {
            pool.shutdownNow();
        }
    }
}
