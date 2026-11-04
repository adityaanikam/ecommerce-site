package com.ecommerce.auth.security;

import com.ecommerce.auth.model.Role;
import com.ecommerce.auth.model.UserAccount;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtIssuerTest {

    private static final String SECRET = "test-secret-that-is-long-enough-for-hs256-abcdef";

    private final JwtIssuer issuer = new JwtIssuer(SECRET, Duration.ofMinutes(15), "ecommerce-auth");

    private static UserAccount user(Set<Role> roles) {
        return UserAccount.builder()
                .id(UUID.fromString("11111111-2222-3333-4444-555555555555"))
                .email("ada@example.com")
                .passwordHash("irrelevant")
                .firstName("Ada").lastName("Lovelace")
                .enabled(true).roles(roles)
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("the issued token carries the user id as subject plus email and roles")
    void tokenCarriesIdentity() {
        JwtIssuer.IssuedToken issued = issuer.issue(user(Set.of(Role.USER)));

        Claims claims = issuer.parse(issued.token()).orElseThrow();
        assertThat(claims.getSubject()).isEqualTo("11111111-2222-3333-4444-555555555555");
        assertThat(claims.get("email", String.class)).isEqualTo("ada@example.com");
        assertThat(claims.get("roles", List.class)).containsExactly("USER");
        assertThat(claims.getIssuer()).isEqualTo("ecommerce-auth");
    }

    @Test
    @DisplayName("expiry matches the configured TTL and is reported in seconds")
    void expiryMatchesTtl() {
        JwtIssuer.IssuedToken issued = issuer.issue(user(Set.of(Role.USER)));

        assertThat(issued.expiresInSeconds()).isEqualTo(900);
        assertThat(issued.expiresAt()).isBetween(
                Instant.now().plusSeconds(880), Instant.now().plusSeconds(910));
    }

    @Test
    @DisplayName("a token signed with another secret does not parse here")
    void foreignTokenIsRejected() {
        JwtIssuer other = new JwtIssuer(
                "a-completely-different-secret-also-long-enough!!", Duration.ofMinutes(15), "x");
        String foreign = other.issue(user(Set.of(Role.ADMIN))).token();

        assertThat(issuer.parse(foreign)).isEmpty();
    }

    @Test
    @DisplayName("an already expired token does not parse")
    void expiredTokenIsRejected() throws Exception {
        JwtIssuer shortLived = new JwtIssuer(SECRET, Duration.ofMillis(1), "ecommerce-auth");
        String token = shortLived.issue(user(Set.of(Role.USER))).token();

        Thread.sleep(1100);

        assertThat(issuer.parse(token)).isEmpty();
    }

    @Test
    @DisplayName("garbage does not parse and does not throw")
    void garbageIsRejected() {
        assertThat(issuer.parse("not-a-token")).isEmpty();
        assertThat(issuer.parse("")).isEmpty();
    }

    @Test
    @DisplayName("both roles end up in the token for an admin")
    void adminRolesAreCarried() {
        JwtIssuer.IssuedToken issued = issuer.issue(user(Set.of(Role.USER, Role.ADMIN)));
        Claims claims = issuer.parse(issued.token()).orElseThrow();
        assertThat(claims.get("roles", List.class)).containsExactlyInAnyOrder("USER", "ADMIN");
    }

    @Test
    @DisplayName("a missing or too-short secret stops the service starting")
    void refusesBadSecret() {
        assertThatThrownBy(() -> new JwtIssuer(null, Duration.ofMinutes(15), "x"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new JwtIssuer("short", Duration.ofMinutes(15), "x"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }
}
