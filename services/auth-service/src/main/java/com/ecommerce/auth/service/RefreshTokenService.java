package com.ecommerce.auth.service;

import com.ecommerce.auth.model.RefreshToken;
import com.ecommerce.auth.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Issues, verifies and rotates refresh tokens.
 *
 * The token itself is 32 bytes from a SecureRandom, base64url encoded. Only its
 * SHA-256 hash reaches the database, so the table is useless to anyone who reads it.
 * A plain hash is right here, unlike for a password: the token is already 256 bits of
 * uniform randomness, so there is nothing to brute force and no need for BCrypt's
 * deliberate slowness on a code path that runs on every token refresh.
 *
 * Refresh is rotating: using a refresh token revokes it and issues a new one. That
 * makes a stolen token useful at most once, and the legitimate client's next refresh
 * failing is the signal that something is wrong.
 */
@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokens;

    /**
     * Used for the one write that has to survive the exception thrown right after it.
     * See consume(): revoking a user's whole token family on a detected reuse happens
     * in its own transaction, because the enclosing one is about to be rolled back.
     */
    private final TransactionTemplate newTransaction;

    @Value("${jwt.refresh-token-ttl:P7D}")
    private Duration refreshTokenTtl;

    public RefreshTokenService(RefreshTokenRepository refreshTokens,
                               PlatformTransactionManager transactionManager) {
        this.refreshTokens = refreshTokens;
        this.newTransaction = new TransactionTemplate(transactionManager);
        this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Transactional
    public String issue(UUID userId) {
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        Instant now = Instant.now();
        refreshTokens.save(RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .tokenHash(hash(token))
                .issuedAt(now)
                .expiresAt(now.plus(refreshTokenTtl))
                .build());

        return token;
    }

    /**
     * Consume a refresh token: check it, revoke it, and return whose it was.
     *
     * @throws AuthException when the token is unknown, already used, revoked or expired
     */
    @Transactional
    public UUID consume(String token) {
        Instant now = Instant.now();
        RefreshToken stored = refreshTokens.findByTokenHash(hash(token))
                .orElseThrow(AuthException::invalidRefreshToken);

        if (!stored.isUsable(now)) {
            // Reuse of an already-revoked token is the classic sign of a stolen one.
            // Revoking the whole family forces every session for that user to log in
            // again, which is the safe response.
            if (stored.getRevokedAt() != null) {
                log.warn("Refresh token reuse detected for user {}, revoking all their tokens",
                        stored.getUserId());
                // In its own transaction on purpose. The throw below rolls this
                // method's transaction back, which would undo the revocation and
                // leave the attacker's rotated token working. Committing separately
                // is the whole point of the breach response.
                newTransaction.executeWithoutResult(
                        status -> refreshTokens.revokeAllForUser(stored.getUserId(), Instant.now()));
            }
            throw AuthException.invalidRefreshToken();
        }

        stored.setRevokedAt(now);
        refreshTokens.save(stored);
        return stored.getUserId();
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        int revoked = refreshTokens.revokeAllForUser(userId, Instant.now());
        log.info("Revoked {} refresh tokens for user {}", revoked, userId);
    }

    static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is required by the JRE spec, so this cannot happen.
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
