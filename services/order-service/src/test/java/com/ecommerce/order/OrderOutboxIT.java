package com.ecommerce.order;

import com.ecommerce.order.client.CatalogClient;
import com.ecommerce.order.outbox.OutboxEventRepository;
import com.ecommerce.order.repository.IdempotencyRecordRepository;
import com.ecommerce.order.repository.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * The order flow end to end against a real PostgreSQL and a real Kafka.
 *
 * What this proves that the unit tests cannot:
 *  - the same Idempotency-Key really does produce one row in the orders table, not
 *    two, because the primary key on (user_id, idempotency_key) is doing the work;
 *  - the order and its outbox event commit together;
 *  - the scheduled publisher moves the event onto the topic and marks the row
 *    published, with the same event id the row carries.
 *
 * catalog-service is mocked here. Its real behaviour, and the circuit breaker around
 * it, are covered by CatalogClientCircuitBreakerTest.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DockerAvailable
@TestPropertySource(properties = {
        "order.outbox.topic=order-placed-it",
        "order.outbox.poll-interval-ms=300"
})
class OrderOutboxIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.0"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private OrderRepository orders;

    @Autowired
    private OutboxEventRepository outbox;

    @Autowired
    private IdempotencyRecordRepository idempotencyKeys;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * The catalog is stubbed, so this test is about order-service's own behaviour
     * rather than about HTTP between the two.
     */
    @MockBean
    private CatalogClient catalogClient;

    private static final UUID USER = UUID.fromString("11111111-2222-3333-4444-555555555555");

    private static final String BODY = """
            {
              "items": [{"productId": "ELEC001", "quantity": 2}],
              "shippingAddress": {
                "firstName": "Ada", "lastName": "Lovelace", "address": "1 Main St",
                "city": "London", "zipCode": "SW1A 1AA", "country": "GB"
              },
              "paymentMethod": {"type": "CARD"},
              "shippingMethod": "STANDARD"
            }
            """;

    private KafkaConsumer<String, String> consumer;

    @BeforeEach
    void setUp() {
        idempotencyKeys.deleteAll();
        outbox.deleteAll();
        orders.deleteAll();

        when(catalogClient.lookup(anyList())).thenReturn(Map.of(
                "ELEC001", new CatalogClient.StockView("ELEC001", "iPhone 15 Pro", 999.99, 599.99, 10)));

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-outbox-it-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(List.of("order-placed-it"));
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    private ResponseEntity<JsonNode> place(UUID idempotencyKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", USER.toString());
        if (idempotencyKey != null) {
            headers.set("Idempotency-Key", idempotencyKey.toString());
        }
        return rest.exchange("/orders", HttpMethod.POST, new HttpEntity<>(BODY, headers), JsonNode.class);
    }

    @Test
    @DisplayName("placing an order prices it from the catalog, not from the browser")
    void orderIsPricedFromCatalog() {
        ResponseEntity<JsonNode> response = place(UUID.randomUUID());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode data = response.getBody().path("data");
        // 2 x the catalog's effectivePrice of 599.99, free shipping over 50.
        assertThat(data.path("subtotal").decimalValue()).isEqualByComparingTo("1199.98");
        assertThat(data.path("shippingCost").decimalValue()).isEqualByComparingTo("0.00");
        assertThat(data.path("total").decimalValue()).isEqualByComparingTo("1199.98");
        assertThat(data.path("items").get(0).path("productName").asText()).isEqualTo("iPhone 15 Pro");
    }

    @Test
    @DisplayName("the same Idempotency-Key twice creates exactly one order")
    void sameIdempotencyKeyCreatesOneOrder() {
        UUID key = UUID.randomUUID();

        ResponseEntity<JsonNode> first = place(key);
        ResponseEntity<JsonNode> second = place(key);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String firstId = first.getBody().path("data").path("id").asText();
        String secondId = second.getBody().path("data").path("id").asText();
        assertThat(secondId).isEqualTo(firstId);

        // The assertion that matters: one row, not two.
        assertThat(orders.count()).isEqualTo(1);
        assertThat(idempotencyKeys.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("a different Idempotency-Key for the same basket does create a second order")
    void differentKeyCreatesASecondOrder() {
        place(UUID.randomUUID());
        place(UUID.randomUUID());

        assertThat(orders.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("omitting the header still works, it just offers no retry protection")
    void missingHeaderStillPlacesAnOrder() {
        assertThat(place(null).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(orders.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("the order and its outbox event are written together, and only one event per order")
    void orderAndOutboxEventCommitTogether() {
        UUID key = UUID.randomUUID();
        place(key);
        place(key);

        assertThat(orders.count()).isEqualTo(1);
        // A replayed request must not enqueue a second OrderPlaced, or stock would
        // be decremented twice for one order.
        assertThat(outbox.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("the scheduled publisher puts the event on Kafka and marks the row published")
    void outboxEventReachesKafka() {
        String orderId = place(UUID.randomUUID()).getBody().path("data").path("id").asText();

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(outbox.countByPublishedAtIsNull())
                        .as("the row should be marked published once Kafka has it")
                        .isZero());

        ConsumerRecord<String, String> record = pollFor(orderId);
        assertThat(record).as("an OrderPlaced for %s should be on the topic", orderId).isNotNull();
        assertThat(record.key()).isEqualTo(orderId);

        JsonNode payload = parse(record.value());
        assertThat(payload.path("orderId").asText()).isEqualTo(orderId);
        assertThat(payload.path("userId").asText()).isEqualTo(USER.toString());
        assertThat(payload.path("items").get(0).path("productId").asText()).isEqualTo("ELEC001");
        assertThat(payload.path("items").get(0).path("quantity").asInt()).isEqualTo(2);

        // The event id in the message is the outbox row's primary key. That identity
        // is what lets a consumer recognise a republished event as a duplicate.
        String eventId = payload.path("eventId").asText();
        assertThat(eventId).isNotBlank();
        assertThat(outbox.findById(UUID.fromString(eventId)))
                .as("the event id in the message must be the outbox row id")
                .isPresent();
    }

    @Test
    @DisplayName("a replayed request publishes one event, not two")
    void replayPublishesOneEvent() {
        UUID key = UUID.randomUUID();
        place(key);
        place(key);

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(outbox.countByPublishedAtIsNull()).isZero());

        // Counted for THIS order only. The topic is shared by every test in the
        // class and messages from earlier ones are still on it, so a raw count
        // would measure the topic's history rather than this test's behaviour.
        String orderId = orders.findAll().get(0).getId().toString();
        int messages = 0;
        long deadline = System.currentTimeMillis() + 8000;
        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, String> record : records) {
                if (orderId.equals(record.key())) {
                    messages++;
                }
            }
        }
        assertThat(messages)
                .as("a replayed request must enqueue one OrderPlaced, not two")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("an order is only visible to the user who placed it")
    void ordersAreScopedToTheirOwner() {
        String orderId = place(UUID.randomUUID()).getBody().path("data").path("id").asText();

        HttpHeaders mine = new HttpHeaders();
        mine.set("X-User-Id", USER.toString());
        assertThat(rest.exchange("/orders/" + orderId, HttpMethod.GET,
                new HttpEntity<>(mine), JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.OK);

        HttpHeaders someoneElse = new HttpHeaders();
        someoneElse.set("X-User-Id", UUID.randomUUID().toString());
        assertThat(rest.exchange("/orders/" + orderId, HttpMethod.GET,
                new HttpEntity<>(someoneElse), JsonNode.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("ordering more than the catalog has is a 409 and writes nothing")
    void insufficientStockIsRejected() {
        when(catalogClient.lookup(anyList())).thenReturn(Map.of(
                "ELEC001", new CatalogClient.StockView("ELEC001", "iPhone 15 Pro", 999.99, 599.99, 1)));

        assertThat(place(UUID.randomUUID()).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(orders.count()).isZero();
        assertThat(outbox.count()).isZero();
    }

    @Test
    @DisplayName("a product the catalog does not know is a 400 and writes nothing")
    void unknownProductIsRejected() {
        when(catalogClient.lookup(anyList())).thenReturn(Map.of());

        assertThat(place(UUID.randomUUID()).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(orders.count()).isZero();
        assertThat(outbox.count()).isZero();
    }

    /**
     * The topic outlives each test method, so a record is only interesting if it is
     * keyed by the order this test just placed.
     */
    private ConsumerRecord<String, String> pollFor(String orderId) {
        long deadline = System.currentTimeMillis() + 20000;
        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, String> record : records) {
                if (orderId.equals(record.key())) {
                    return record;
                }
            }
        }
        return null;
    }

    private JsonNode parse(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
