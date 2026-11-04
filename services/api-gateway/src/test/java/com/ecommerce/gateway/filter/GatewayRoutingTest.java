package com.ecommerce.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The gateway's contract, exercised end to end through a real HTTP port with a
 * MockWebServer standing in for catalog-service and order-service.
 *
 * The assertions that matter most:
 *  - a protected route with a missing, malformed, expired or wrongly signed token is
 *    answered 401 and NOTHING is forwarded downstream;
 *  - a valid token results in X-User-Id and X-User-Roles arriving at the service;
 *  - a client cannot set X-User-Id itself and have it survive, on a protected route
 *    or a public one.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRoutingTest {

    private static final String SECRET = "test-secret-that-is-long-enough-for-hs256-abcdef";
    private static final String OTHER_SECRET = "a-completely-different-secret-also-long-enough!!";

    private static final MockWebServer DOWNSTREAM = new MockWebServer();

    static {
        try {
            DOWNSTREAM.start();
        } catch (IOException e) {
            throw new IllegalStateException("could not start the stub downstream", e);
        }
    }

    private static String targetOf(RecordedRequest request) {
        return request.getPath();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        String url = "http://localhost:" + DOWNSTREAM.getPort();
        registry.add("CATALOG_SERVICE_URL", () -> url);
        registry.add("AUTH_SERVICE_URL", () -> url);
        registry.add("ORDER_SERVICE_URL", () -> url);
        registry.add("jwt.secret", () -> SECRET);
        registry.add("gateway.cors.allowed-origins", () -> "http://localhost:3000");
    }

    @AfterAll
    static void stopDownstream() throws IOException {
        DOWNSTREAM.shutdown();
    }

    @Autowired
    private WebTestClient webTestClient;

    private static SecretKey key(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static String token(String secret, String subject, List<String> roles, Instant expiry) {
        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .claim("email", "user@example.com")
                .expiration(Date.from(expiry))
                .signWith(key(secret))
                .compact();
    }

    private static String validToken() {
        return token(SECRET, "user-42", List.of("USER"), Instant.now().plusSeconds(600));
    }

    private void enqueueOk() {
        DOWNSTREAM.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"ok\":true}"));
    }

    /** @return the request the stub actually received, or null if nothing was forwarded. */
    private RecordedRequest forwarded() throws InterruptedException {
        return DOWNSTREAM.takeRequest(2, TimeUnit.SECONDS);
    }

    // ---------------------------------------------------------------- rejection

    @Test
    @DisplayName("a protected route with no Authorization header is 401 and nothing is forwarded")
    void missingTokenIsRejected() throws Exception {
        webTestClient.get().uri("/api/orders")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error").isEqualTo("unauthorized");

        assertThat(forwarded()).as("nothing must reach order-service").isNull();
    }

    @Test
    @DisplayName("a protected route with a garbage token is 401")
    void malformedTokenIsRejected() throws Exception {
        webTestClient.get().uri("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt-at-all")
                .exchange()
                .expectStatus().isUnauthorized();

        assertThat(forwarded()).isNull();
    }

    @Test
    @DisplayName("a protected route with an expired token is 401")
    void expiredTokenIsRejected() throws Exception {
        String expired = token(SECRET, "user-42", List.of("USER"), Instant.now().minusSeconds(5));

        webTestClient.get().uri("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expired)
                .exchange()
                .expectStatus().isUnauthorized();

        assertThat(forwarded()).isNull();
    }

    @Test
    @DisplayName("a token signed with the wrong secret is 401")
    void wronglySignedTokenIsRejected() throws Exception {
        String forged = token(OTHER_SECRET, "user-42", List.of("ADMIN"), Instant.now().plusSeconds(600));

        webTestClient.get().uri("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + forged)
                .exchange()
                .expectStatus().isUnauthorized();

        assertThat(forwarded()).isNull();
    }

    @Test
    @DisplayName("an Authorization header that is not a Bearer token is 401")
    void nonBearerSchemeIsRejected() throws Exception {
        webTestClient.get().uri("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz")
                .exchange()
                .expectStatus().isUnauthorized();

        assertThat(forwarded()).isNull();
    }

    @Test
    @DisplayName("a 401 from the gateway still carries a correlation id the caller can quote")
    void rejectionCarriesCorrelationId() {
        webTestClient.get().uri("/api/orders")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().exists(CorrelationIdGatewayFilter.HEADER);
    }

    // ------------------------------------------------------------------- success

    @Test
    @DisplayName("a valid token is forwarded with the verified user id and roles, and /api is stripped")
    void validTokenForwardsIdentity() throws Exception {
        enqueueOk();

        webTestClient.get().uri("/api/orders/order-1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken())
                .exchange()
                .expectStatus().isOk();

        RecordedRequest request = forwarded();
        assertThat(request).isNotNull();
        // StripPrefix=1 removed /api, because the services serve at the root.
        assertThat(targetOf(request)).isEqualTo("/orders/order-1");
        assertThat(request.getHeader(JwtAuthenticationFilter.USER_ID_HEADER)).isEqualTo("user-42");
        assertThat(request.getHeader(JwtAuthenticationFilter.USER_ROLES_HEADER)).isEqualTo("USER");
        assertThat(request.getHeader(JwtAuthenticationFilter.USER_EMAIL_HEADER))
                .isEqualTo("user@example.com");
        assertThat(request.getHeader(CorrelationIdGatewayFilter.HEADER)).isNotBlank();
    }

    @Test
    @DisplayName("a client cannot forge X-User-Id on a protected route")
    void spoofedIdentityHeaderIsOverwritten() throws Exception {
        enqueueOk();

        webTestClient.get().uri("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken())
                .header(JwtAuthenticationFilter.USER_ID_HEADER, "administrator")
                .header(JwtAuthenticationFilter.USER_ROLES_HEADER, "ADMIN")
                .exchange()
                .expectStatus().isOk();

        RecordedRequest request = forwarded();
        assertThat(request).isNotNull();
        assertThat(request.getHeader(JwtAuthenticationFilter.USER_ID_HEADER)).isEqualTo("user-42");
        assertThat(request.getHeader(JwtAuthenticationFilter.USER_ROLES_HEADER)).isEqualTo("USER");
    }

    // -------------------------------------------------------------- public routes

    @Test
    @DisplayName("GET /api/products needs no token and is routed to catalog")
    void productReadsArePublic() throws Exception {
        enqueueOk();

        webTestClient.get().uri("/api/products?page=0&size=12")
                .exchange()
                .expectStatus().isOk();

        RecordedRequest request = forwarded();
        assertThat(request).isNotNull();
        assertThat(targetOf(request)).isEqualTo("/products?page=0&size=12");
    }

    @Test
    @DisplayName("a public route strips any client supplied identity headers too")
    void publicRouteStripsSpoofedIdentity() throws Exception {
        enqueueOk();

        webTestClient.get().uri("/api/products")
                .header(JwtAuthenticationFilter.USER_ID_HEADER, "administrator")
                .header(JwtAuthenticationFilter.USER_ROLES_HEADER, "ADMIN")
                .exchange()
                .expectStatus().isOk();

        RecordedRequest request = forwarded();
        assertThat(request).isNotNull();
        assertThat(request.getHeader(JwtAuthenticationFilter.USER_ID_HEADER)).isNull();
        assertThat(request.getHeader(JwtAuthenticationFilter.USER_ROLES_HEADER)).isNull();
    }

    @Test
    @DisplayName("POST /api/products is not public, because only reads are")
    void productWritesNeedAToken() throws Exception {
        webTestClient.post().uri("/api/products")
                .bodyValue("{}")
                .exchange()
                .expectStatus().isUnauthorized();

        assertThat(forwarded()).isNull();
    }

    @Test
    @DisplayName("POST /api/auth/login is public, since you cannot have a token before logging in")
    void authEndpointsArePublic() throws Exception {
        enqueueOk();

        webTestClient.post().uri("/api/auth/login")
                .bodyValue("{\"email\":\"a@b.c\",\"password\":\"x\"}")
                .exchange()
                .expectStatus().isOk();

        RecordedRequest request = forwarded();
        assertThat(request).isNotNull();
        assertThat(targetOf(request)).isEqualTo("/auth/login");
    }

    // ----------------------------------------------------------- correlation id

    @Test
    @DisplayName("a correlation id supplied by the caller is kept, not replaced")
    void callerCorrelationIdIsHonoured() throws Exception {
        enqueueOk();

        webTestClient.get().uri("/api/products")
                .header(CorrelationIdGatewayFilter.HEADER, "my-trace-id")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(CorrelationIdGatewayFilter.HEADER, "my-trace-id");

        RecordedRequest request = forwarded();
        assertThat(request).isNotNull();
        assertThat(request.getHeader(CorrelationIdGatewayFilter.HEADER)).isEqualTo("my-trace-id");
    }

    @Test
    @DisplayName("a request with no correlation id gets one generated and echoed back")
    void correlationIdIsGenerated() throws Exception {
        enqueueOk();

        String id = webTestClient.get().uri("/api/products")
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseHeaders()
                .getFirst(CorrelationIdGatewayFilter.HEADER);

        assertThat(id).isNotBlank();
        RecordedRequest request = forwarded();
        assertThat(request).isNotNull();
        assertThat(request.getHeader(CorrelationIdGatewayFilter.HEADER)).isEqualTo(id);
    }

    // ------------------------------------------------------------------- CORS

    @Test
    @DisplayName("an allowed origin gets CORS headers on a preflight")
    void preflightFromAllowedOrigin() {
        webTestClient.options().uri("/api/orders")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "authorization,content-type,idempotency-key")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectHeader().valueEquals("Access-Control-Allow-Origin", "http://localhost:3000");
    }

    @Test
    @DisplayName("an origin that is not on the list is refused rather than silently allowed")
    void preflightFromDisallowedOrigin() {
        webTestClient.options().uri("/api/orders")
                .header("Origin", "http://evil.example")
                .header("Access-Control-Request-Method", "POST")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("a 401 is still readable by the browser, because CORS headers are present on it")
    void rejectionCarriesCorsHeaders() {
        webTestClient.get().uri("/api/orders")
                .header("Origin", "http://localhost:3000")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().valueEquals("Access-Control-Allow-Origin", "http://localhost:3000");
    }

    // ---------------------------------------------------------------- actuator

    @Test
    @DisplayName("the actuator health probes are reachable without a token")
    void actuatorIsPublic() {
        webTestClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();

        webTestClient.get().uri("/actuator/health/readiness")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("the gateway exposes Prometheus metrics")
    void prometheusIsExposed() {
        webTestClient.get().uri("/actuator/prometheus")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("jvm_memory_used_bytes"));
    }

}
