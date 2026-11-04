package com.ecommerce.catalog.messaging;

import com.ecommerce.catalog.DockerAvailable;
import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.repository.ProductRepository;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * End to end over real Kafka and real MongoDB: publish the same OrderPlaced twice and
 * prove stock moved once.
 *
 * This is the integration counterpart to OrderPlacedConsumerTest. The unit test proves
 * the branch with a mocked repository; this one proves the primary key collision
 * actually happens in MongoDB and that the Kafka deserialiser produces the event this
 * service expects.
 *
 * Redis is not started here. The context needs a CacheManager, so the test profile
 * switches spring.cache.type to none, which is enough because what is under test is
 * the consumer and the database write.
 */
@SpringBootTest
@Testcontainers
@DockerAvailable
@TestPropertySource(properties = {
        "catalog.seed.enabled=false",
        "spring.cache.type=none",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "catalog.kafka.order-placed-topic=order-placed-it"
})
class OrderPlacedKafkaIT {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @Container
    // Wired with @DynamicPropertySource rather than @ServiceConnection: Spring Boot
    // 3.2 only knows the older org.testcontainers.containers.KafkaContainer, and this
    // is the newer KRaft one, which is the same single-node topology
    // docker-compose.yml runs. Setting the one property by hand is cheaper than
    // pinning the whole project to an older Kafka container that still needs
    // ZooKeeper.
    static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.0"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        processedEventRepository.deleteAll();
        productRepository.save(Product.builder().id("ELEC001").name("iPhone").stock(10).price(999.0).build());

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        ProducerFactory<String, String> factory = new DefaultKafkaProducerFactory<>(props);
        kafkaTemplate = new KafkaTemplate<>(factory);
    }

    private static String orderPlacedJson(String eventId, String orderId, String productId, int quantity) {
        return """
                {"eventId":"%s","orderId":"%s","items":[{"productId":"%s","quantity":%d}]}
                """.formatted(eventId, orderId, productId, quantity);
    }

    @Test
    @DisplayName("one OrderPlaced decrements stock once")
    void singleEventDecrementsOnce() {
        kafkaTemplate.send("order-placed-it", "order-1",
                orderPlacedJson("evt-single", "order-1", "ELEC001", 4));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(productRepository.findById("ELEC001")).get()
                        .extracting(Product::getStock).isEqualTo(6));
    }

    @Test
    @DisplayName("the same OrderPlaced delivered twice decrements stock only once")
    void duplicateEventDecrementsOnlyOnce() {
        String json = orderPlacedJson("evt-duplicate", "order-2", "ELEC001", 3);

        kafkaTemplate.send("order-placed-it", "order-2", json);
        kafkaTemplate.send("order-placed-it", "order-2", json);
        kafkaTemplate.flush();

        // Wait for the first to land.
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(processedEventRepository.findById("evt-duplicate")).isPresent());

        // Then give the second one time to be consumed and ignored, and assert the
        // stock did not move again. A sleep would be flaky; asserting the value holds
        // steady for a while is what actually proves the duplicate was dropped.
        await().during(Duration.ofSeconds(5)).atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(productRepository.findById("ELEC001")).get()
                        .extracting(Product::getStock).isEqualTo(7));

        assertThat(processedEventRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("two different events for the same product both apply")
    void distinctEventsBothApply() {
        kafkaTemplate.send("order-placed-it", "order-3",
                orderPlacedJson("evt-a", "order-3", "ELEC001", 2));
        kafkaTemplate.send("order-placed-it", "order-4",
                orderPlacedJson("evt-b", "order-4", "ELEC001", 3));
        kafkaTemplate.flush();

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(productRepository.findById("ELEC001")).get()
                        .extracting(Product::getStock).isEqualTo(5));
    }
}
