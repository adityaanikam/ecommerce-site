package com.ecommerce.auth;

import com.ecommerce.auth.repository.UserAccountRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The whole auth flow over real HTTP against real PostgreSQL with real Flyway
 * migrations: register, log in, use the token, refresh it, log out.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DockerAvailable
@TestPropertySource(properties = "jwt.secret=integration-test-secret-long-enough-for-hs256")
class AuthFlowIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private UserAccountRepository users;

    @BeforeEach
    void clean() {
        users.deleteAll();
    }

    private HttpEntity<String> body(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(json, headers);
    }

    private static final String REGISTRATION = """
            {"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com",
             "password":"Str0ng!Pass","phoneNumber":"+447700900123"}
            """;

    private ResponseEntity<JsonNode> register(String json) {
        return rest.postForEntity("/auth/register", body(json), JsonNode.class);
    }

    private ResponseEntity<JsonNode> login(String email, String password) {
        return rest.postForEntity("/auth/login",
                body("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)), JsonNode.class);
    }

    @Test
    @DisplayName("register then log in then read the current user with the issued token")
    void fullHappyPath() {
        ResponseEntity<JsonNode> registered = register(REGISTRATION);
        assertThat(registered.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registered.getBody().path("data").path("accessToken").asText()).isNotBlank();

        ResponseEntity<JsonNode> loggedIn = login("ada@example.com", "Str0ng!Pass");
        assertThat(loggedIn.getStatusCode()).isEqualTo(HttpStatus.OK);
        String accessToken = loggedIn.getBody().path("data").path("accessToken").asText();
        assertThat(accessToken).isNotBlank();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        ResponseEntity<JsonNode> me = rest.exchange(
                "/auth/me", HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);

        assertThat(me.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(me.getBody().path("data").path("email").asText()).isEqualTo("ada@example.com");
        assertThat(me.getBody().path("data").path("role").asText()).isEqualTo("USER");
    }

    @Test
    @DisplayName("the password is never stored in plain text")
    void passwordIsHashedInTheDatabase() {
        register(REGISTRATION);

        var stored = users.findByEmailIgnoreCase("ada@example.com").orElseThrow();
        assertThat(stored.getPasswordHash()).isNotEqualTo("Str0ng!Pass").startsWith("$2a$");
    }

    @Test
    @DisplayName("login is case insensitive on the email")
    void loginIsCaseInsensitive() {
        register(REGISTRATION);

        assertThat(login("ADA@EXAMPLE.COM", "Str0ng!Pass").getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("registering the same email twice is 409")
    void duplicateRegistrationIsConflict() {
        assertThat(register(REGISTRATION).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(register(REGISTRATION).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("a wrong password is 401")
    void wrongPasswordIs401() {
        register(REGISTRATION);

        assertThat(login("ada@example.com", "Wr0ng!Pass").getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("refresh rotates: the new token works and the old one stops working")
    void refreshRotatesTheToken() {
        String firstRefresh = register(REGISTRATION).getBody()
                .path("data").path("refreshToken").asText();

        ResponseEntity<JsonNode> refreshed = rest.postForEntity("/auth/refresh",
                body("{\"refreshToken\":\"%s\"}".formatted(firstRefresh)), JsonNode.class);

        assertThat(refreshed.getStatusCode()).isEqualTo(HttpStatus.OK);
        String secondRefresh = refreshed.getBody().path("data").path("refreshToken").asText();
        assertThat(secondRefresh).isNotBlank().isNotEqualTo(firstRefresh);

        // Replaying the consumed token must fail. This is what makes a stolen
        // refresh token useful at most once.
        ResponseEntity<JsonNode> replay = rest.postForEntity("/auth/refresh",
                body("{\"refreshToken\":\"%s\"}".formatted(firstRefresh)), JsonNode.class);
        assertThat(replay.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("replaying a consumed refresh token also kills the rotated one, as reuse means theft")
    void reuseRevokesTheWholeFamily() {
        String first = register(REGISTRATION).getBody().path("data").path("refreshToken").asText();
        String second = rest.postForEntity("/auth/refresh",
                        body("{\"refreshToken\":\"%s\"}".formatted(first)), JsonNode.class)
                .getBody().path("data").path("refreshToken").asText();

        // Attacker replays the stolen, already-consumed first token.
        rest.postForEntity("/auth/refresh",
                body("{\"refreshToken\":\"%s\"}".formatted(first)), JsonNode.class);

        // The legitimate client's current token is now dead too, forcing a re-login.
        ResponseEntity<JsonNode> afterBreach = rest.postForEntity("/auth/refresh",
                body("{\"refreshToken\":\"%s\"}".formatted(second)), JsonNode.class);
        assertThat(afterBreach.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("logout revokes the refresh token so it cannot be used again")
    void logoutRevokes() {
        JsonNode data = register(REGISTRATION).getBody().path("data");
        String accessToken = data.path("accessToken").asText();
        String refreshToken = data.path("refreshToken").asText();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        assertThat(rest.exchange("/auth/logout", HttpMethod.POST, new HttpEntity<>(headers), JsonNode.class)
                .getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(rest.postForEntity("/auth/refresh",
                body("{\"refreshToken\":\"%s\"}".formatted(refreshToken)), JsonNode.class)
                .getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("a weak password never creates an account")
    void weakPasswordCreatesNothing() {
        ResponseEntity<JsonNode> response = register("""
                {"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com","password":"weak"}
                """);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(users.existsByEmailIgnoreCase("ada@example.com")).isFalse();
    }

    @Test
    @DisplayName("an unknown refresh token is 401")
    void unknownRefreshTokenIs401() {
        assertThat(rest.postForEntity("/auth/refresh",
                body("{\"refreshToken\":\"made-up\"}"), JsonNode.class)
                .getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("the actuator readiness probe answers")
    void readinessProbeAnswers() {
        assertThat(rest.getForEntity("/actuator/health/readiness", String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }
}
