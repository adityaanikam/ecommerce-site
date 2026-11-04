package com.ecommerce.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.jsonwebtoken.security.Keys;

/**
 * Verifies the HMAC signature and the expiry of an access token, and pulls out the
 * two things the downstream services need: who the caller is and what roles they
 * hold.
 *
 * The gateway only verifies; auth-service is the only thing that issues. They share
 * one secret, supplied by JWT_SECRET, so there is nothing to hardcode here. If the
 * variable is missing the application refuses to start rather than falling back to a
 * built-in default, which is how the monolith's application-docker.yml shipped a real
 * looking secret in Git.
 */
@Component
public class JwtVerifier {

    private static final Logger log = LoggerFactory.getLogger(JwtVerifier.class);

    private final SecretKey key;

    public JwtVerifier(@Value("${jwt.secret}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("jwt.secret (JWT_SECRET) must be set");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // HS256 needs at least 256 bits of key material. Failing here beats
            // failing on the first request with a confusing library error.
            throw new IllegalStateException(
                    "jwt.secret must be at least 32 bytes for HS256, got " + keyBytes.length);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * @return the verified principal, or empty when the token is absent, malformed,
     *         expired, or signed with the wrong key. The caller cannot tell the
     *         difference, and neither can an attacker.
     */
    public Optional<AuthenticatedUser> verify(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            Claims claims = jws.getPayload();
            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                log.debug("Rejecting token with no subject");
                return Optional.empty();
            }

            // The roles claim is a JSON array when auth-service issued the token, but
            // tolerate a comma separated string so a hand-made token still works.
            Object rawRoles = claims.get("roles");
            List<String> roles;
            if (rawRoles instanceof List<?> list) {
                roles = list.stream().map(String::valueOf).toList();
            } else if (rawRoles instanceof String s && !s.isBlank()) {
                roles = Arrays.stream(s.split(",")).map(String::trim).filter(r -> !r.isEmpty()).toList();
            } else {
                roles = List.of();
            }

            String email = claims.get("email", String.class);
            return Optional.of(new AuthenticatedUser(subject, email, roles));
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Rejecting token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public record AuthenticatedUser(String userId, String email, List<String> roles) {
    }
}
