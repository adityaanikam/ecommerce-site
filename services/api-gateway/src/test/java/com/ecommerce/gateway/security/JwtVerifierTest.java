package com.ecommerce.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtVerifierTest {

    private static final String SECRET = "test-secret-that-is-long-enough-for-hs256-abcdef";
    private static final String OTHER_SECRET = "a-completely-different-secret-also-long-enough!!";

    private final JwtVerifier verifier = new JwtVerifier(SECRET);

    private static SecretKey key(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static String token(String secret, String subject, List<String> roles, Instant expiry) {
        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .claim("email", "a@example.com")
                .issuedAt(Date.from(Instant.now().minusSeconds(60)))
                .expiration(Date.from(expiry))
                .signWith(key(secret))
                .compact();
    }

    @Test
    @DisplayName("a correctly signed, unexpired token yields the subject and roles")
    void validTokenIsAccepted() {
        String jwt = token(SECRET, "user-1", List.of("USER", "ADMIN"), Instant.now().plusSeconds(600));

        var user = verifier.verify(jwt);

        assertThat(user).isPresent();
        assertThat(user.get().userId()).isEqualTo("user-1");
        assertThat(user.get().roles()).containsExactly("USER", "ADMIN");
        assertThat(user.get().email()).isEqualTo("a@example.com");
    }

    @Test
    @DisplayName("an expired token is rejected")
    void expiredTokenIsRejected() {
        String jwt = token(SECRET, "user-1", List.of("USER"), Instant.now().minusSeconds(10));
        assertThat(verifier.verify(jwt)).isEmpty();
    }

    @Test
    @DisplayName("a token signed with a different secret is rejected")
    void wrongSignatureIsRejected() {
        String jwt = token(OTHER_SECRET, "user-1", List.of("USER"), Instant.now().plusSeconds(600));
        assertThat(verifier.verify(jwt)).isEmpty();
    }

    @Test
    @DisplayName("garbage, empty and null tokens are rejected without throwing")
    void malformedTokensAreRejected() {
        assertThat(verifier.verify("not-a-jwt")).isEmpty();
        assertThat(verifier.verify("a.b.c")).isEmpty();
        assertThat(verifier.verify("")).isEmpty();
        assertThat(verifier.verify("   ")).isEmpty();
        assertThat(verifier.verify(null)).isEmpty();
    }

    @Test
    @DisplayName("an unsigned token is rejected, so alg:none cannot be used to forge identity")
    void unsignedTokenIsRejected() {
        String unsigned = Jwts.builder().subject("user-1").claim("roles", List.of("ADMIN")).compact();
        assertThat(verifier.verify(unsigned)).isEmpty();
    }

    @Test
    @DisplayName("a token with no subject is rejected, since there would be no user id to forward")
    void tokenWithoutSubjectIsRejected() {
        String jwt = Jwts.builder()
                .claim("roles", List.of("USER"))
                .expiration(Date.from(Instant.now().plusSeconds(600)))
                .signWith(key(SECRET))
                .compact();
        assertThat(verifier.verify(jwt)).isEmpty();
    }

    @Test
    @DisplayName("roles survive as a comma separated string as well as a JSON array")
    void rolesAcceptBothShapes() {
        String jwt = Jwts.builder()
                .subject("user-1")
                .claim("roles", "USER, ADMIN")
                .expiration(Date.from(Instant.now().plusSeconds(600)))
                .signWith(key(SECRET))
                .compact();
        assertThat(verifier.verify(jwt)).get()
                .extracting(JwtVerifier.AuthenticatedUser::roles)
                .isEqualTo(List.of("USER", "ADMIN"));
    }

    @Test
    @DisplayName("a missing roles claim gives an empty role list rather than an error")
    void missingRolesIsEmptyList() {
        String jwt = Jwts.builder()
                .subject("user-1")
                .expiration(Date.from(Instant.now().plusSeconds(600)))
                .signWith(key(SECRET))
                .compact();
        assertThat(verifier.verify(jwt)).get()
                .extracting(JwtVerifier.AuthenticatedUser::roles)
                .isEqualTo(List.of());
    }

    @Test
    @DisplayName("the verifier refuses to be built without a usable secret")
    void refusesWeakOrMissingSecret() {
        assertThatThrownBy(() -> new JwtVerifier(null)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new JwtVerifier("")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new JwtVerifier("too-short"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    @Test
    @DisplayName("Map claims also deserialise, which is what a real auth-service token looks like")
    void mapClaimsWork() {
        String jwt = Jwts.builder()
                .claims(Map.of("sub", "user-9", "roles", List.of("USER"), "email", "b@example.com"))
                .expiration(Date.from(Instant.now().plusSeconds(600)))
                .signWith(key(SECRET))
                .compact();
        assertThat(verifier.verify(jwt)).get()
                .extracting(JwtVerifier.AuthenticatedUser::userId).isEqualTo("user-9");
    }
}
