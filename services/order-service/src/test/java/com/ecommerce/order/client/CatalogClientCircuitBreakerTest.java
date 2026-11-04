package com.ecommerce.order.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Proves the circuit breaker actually opens when catalog-service is down, and that
 * once it is open the calls fail immediately instead of each waiting out the
 * timeout.
 *
 * The stub is told to hang rather than to return an error, because that is the
 * failure that actually hurts: an error comes back fast, while a dependency that
 * accepts the connection and then never answers is what ties up every request
 * thread. The read timeout is dropped to 300ms here so the test is quick.
 *
 * This runs without a database: JPA, Flyway and the scheduled outbox publisher are
 * all switched off, because none of them is what is under test.
 */
@SpringBootTest(
        classes = {com.ecommerce.order.config.CatalogClientConfig.class, CatalogClient.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@org.springframework.boot.autoconfigure.ImportAutoConfiguration({
        io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration.class,
        io.github.resilience4j.springboot3.retry.autoconfigure.RetryAutoConfiguration.class,
        org.springframework.boot.autoconfigure.aop.AopAutoConfiguration.class
})
@TestPropertySource(properties = {
        "order.catalog.connect-timeout-ms=300",
        "order.catalog.read-timeout-ms=300",
        "resilience4j.circuitbreaker.instances.catalog.sliding-window-type=COUNT_BASED",
        "resilience4j.circuitbreaker.instances.catalog.sliding-window-size=4",
        "resilience4j.circuitbreaker.instances.catalog.minimum-number-of-calls=4",
        "resilience4j.circuitbreaker.instances.catalog.failure-rate-threshold=50",
        "resilience4j.circuitbreaker.instances.catalog.wait-duration-in-open-state=10s",
        "resilience4j.circuitbreaker.instances.catalog.automatic-transition-from-open-to-half-open-enabled=false",
        "resilience4j.retry.instances.catalog.max-attempts=1"
})
class CatalogClientCircuitBreakerTest {

    private static final MockWebServer CATALOG = new MockWebServer();

    static {
        try {
            CATALOG.start();
        } catch (IOException e) {
            throw new IllegalStateException("could not start the stub catalog", e);
        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("order.catalog.base-url", () -> "http://localhost:" + CATALOG.getPort());
    }

    @AfterAll
    static void stop() throws IOException {
        CATALOG.shutdown();
    }

    @Autowired
    private CatalogClient catalogClient;

    @Autowired
    private CircuitBreakerRegistry registry;

    private CircuitBreaker breaker() {
        return registry.circuitBreaker(CatalogClient.CIRCUIT_BREAKER);
    }

    @BeforeEach
    void resetBreaker() {
        breaker().reset();
    }

    private void enqueueHang() {
        CATALOG.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
    }

    private void enqueueOk() {
        CATALOG.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        [{"productId":"ELEC001","name":"iPhone","price":999.0,
                          "effectivePrice":999.0,"stock":10}]
                        """));
    }

    @Test
    @DisplayName("a healthy catalog answers and the breaker stays closed")
    void healthyCatalogKeepsBreakerClosed() {
        enqueueOk();

        var result = catalogClient.lookup(List.of("ELEC001"));

        assertThat(result).containsKey("ELEC001");
        assertThat(result.get("ELEC001").stock()).isEqualTo(10);
        assertThat(breaker().getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    @DisplayName("a catalog that hangs is given up on rather than waited for indefinitely")
    void hangingCatalogTimesOut() {
        enqueueHang();

        long start = System.nanoTime();
        assertThatThrownBy(() -> catalogClient.lookup(List.of("ELEC001")))
                .isInstanceOf(CatalogUnavailableException.class);
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        // The read timeout is 300ms, so this must not take anything like for ever.
        assertThat(elapsedMs).isLessThan(5000);
    }

    @Test
    @DisplayName("the breaker opens once catalog-service is down")
    void breakerOpensWhenCatalogIsDown() {
        // Four failures fills the sliding window at a 100% failure rate.
        for (int i = 0; i < 4; i++) {
            enqueueHang();
            assertThatThrownBy(() -> catalogClient.lookup(List.of("ELEC001")))
                    .isInstanceOf(CatalogUnavailableException.class);
        }

        assertThat(breaker().getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    @DisplayName("while open, calls fail immediately and the catalog is not called at all")
    void openBreakerShortCircuits() throws Exception {
        for (int i = 0; i < 4; i++) {
            enqueueHang();
            assertThatThrownBy(() -> catalogClient.lookup(List.of("ELEC001")))
                    .isInstanceOf(CatalogUnavailableException.class);
        }
        assertThat(breaker().getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // Drain what the stub received so far, so the next check is clean.
        while (CATALOG.takeRequest(50, TimeUnit.MILLISECONDS) != null) {
            // discard
        }
        int before = CATALOG.getRequestCount();

        long start = System.nanoTime();
        assertThatThrownBy(() -> catalogClient.lookup(List.of("ELEC001")))
                .isInstanceOf(CatalogUnavailableException.class);
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        // This is the point of the breaker: no network call, no timeout wait.
        assertThat(CATALOG.getRequestCount())
                .as("an open breaker must not call the failing service at all")
                .isEqualTo(before);
        assertThat(elapsedMs)
                .as("an open breaker fails fast rather than waiting out the timeout")
                .isLessThan(250);
    }

    @Test
    @DisplayName("an empty product list short circuits without calling the catalog")
    void emptyListNeedsNoCall() {
        int before = CATALOG.getRequestCount();

        assertThat(catalogClient.lookup(List.of())).isEmpty();

        assertThat(CATALOG.getRequestCount()).isEqualTo(before);
    }

    @Test
    @DisplayName("the breaker closes again once the catalog recovers")
    void breakerClosesAfterRecovery() {
        for (int i = 0; i < 4; i++) {
            enqueueHang();
            assertThatThrownBy(() -> catalogClient.lookup(List.of("ELEC001")))
                    .isInstanceOf(CatalogUnavailableException.class);
        }
        assertThat(breaker().getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // Stand in for the wait-duration elapsing, rather than sleeping it out.
        breaker().transitionToHalfOpenState();
        for (int i = 0; i < 4; i++) {
            enqueueOk();
            assertThat(catalogClient.lookup(List.of("ELEC001"))).containsKey("ELEC001");
        }

        assertThat(breaker().getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }
}
