package com.ecommerce.auth.security;

import com.ecommerce.auth.model.Role;
import com.ecommerce.auth.model.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * The only thing in the system that mints access tokens.
 *
 * It signs with the same HS256 secret the gateway verifies with, supplied by
 * JWT_SECRET. Nothing is hardcoded and the service refuses to start without it; the
 * monolith shipped a real looking default in application-docker.yml.
 *
 * Access tokens are short lived on purpose. A revoked user keeps working until their
 * access token expires, because the gateway verifies the signature and does not call
 * back to this service on every request. Fifteen minutes is the window that choice
 * costs, and the refresh token, which IS checked against the database on every use,
 * is what makes a revocation actually land.
 */
@Component
public class JwtIssuer {

    private final SecretKey key;
    private final Duration accessTokenTtl;
    private final String issuer;

    public JwtIssuer(@Value("${jwt.secret}") String secret,
                     @Value("${jwt.access-token-ttl:PT15M}") Duration accessTokenTtl,
                     @Value("${jwt.issuer:ecommerce-auth}") String issuer) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("jwt.secret (JWT_SECRET) must be set");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "jwt.secret must be at least 32 bytes for HS256, got " + keyBytes.length);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenTtl = accessTokenTtl;
        this.issuer = issuer;
    }

    public IssuedToken issue(UserAccount user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(accessTokenTtl);

        List<String> roles = user.getRoles().stream().map(Role::name).toList();

        String token = Jwts.builder()
                .subject(user.getId().toString())
                .issuer(issuer)
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();

        return new IssuedToken(token, expiry, accessTokenTtl.toSeconds());
    }

    /**
     * Used by this service's own /auth/me, which has to accept the same token the
     * gateway issued access with when the request did not come through the gateway
     * (service-to-service calls, and tests).
     */
    public Optional<Claims> parse(String token) {
        try {
            return Optional.of(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public record IssuedToken(String token, Instant expiresAt, long expiresInSeconds) {
    }
}
