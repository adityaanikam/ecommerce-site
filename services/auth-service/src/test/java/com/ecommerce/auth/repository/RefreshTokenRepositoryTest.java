package com.ecommerce.auth.repository;

import com.ecommerce.auth.DockerAvailable;
import com.ecommerce.auth.model.RefreshToken;
import com.ecommerce.auth.model.Role;
import com.ecommerce.auth.model.UserAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@DockerAvailable
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RefreshTokenRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private RefreshTokenRepository refreshTokens;

    @Autowired
    private UserAccountRepository users;

    private UUID createUser() {
        return users.saveAndFlush(UserAccount.builder()
                .id(UUID.randomUUID())
                .email("user-" + UUID.randomUUID() + "@example.com")
                .passwordHash("hash").firstName("A").lastName("B")
                .enabled(true).roles(Set.of(Role.USER))
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build()).getId();
    }

    private RefreshToken token(UUID userId, String hash, Instant expiresAt) {
        return refreshTokens.saveAndFlush(RefreshToken.builder()
                .id(UUID.randomUUID()).userId(userId).tokenHash(hash)
                .issuedAt(Instant.now()).expiresAt(expiresAt)
                .build());
    }

    @Test
    @DisplayName("a token is found by its hash")
    void foundByHash() {
        UUID userId = createUser();
        token(userId, "hash-a", Instant.now().plus(7, ChronoUnit.DAYS));

        assertThat(refreshTokens.findByTokenHash("hash-a")).isPresent();
        assertThat(refreshTokens.findByTokenHash("hash-missing")).isEmpty();
    }

    @Test
    @DisplayName("isUsable is false once revoked and once expired")
    void usability() {
        UUID userId = createUser();
        Instant now = Instant.now();

        RefreshToken live = token(userId, "hash-live", now.plus(1, ChronoUnit.DAYS));
        assertThat(live.isUsable(now)).isTrue();

        RefreshToken expired = token(userId, "hash-expired", now.minus(1, ChronoUnit.DAYS));
        assertThat(expired.isUsable(now)).isFalse();

        live.setRevokedAt(now);
        assertThat(live.isUsable(now)).isFalse();
    }

    @Test
    @DisplayName("revoking for a user stamps every one of their live tokens")
    void revokeAllForUser() {
        UUID userId = createUser();
        UUID otherUser = createUser();
        Instant future = Instant.now().plus(7, ChronoUnit.DAYS);
        token(userId, "hash-1", future);
        token(userId, "hash-2", future);
        token(otherUser, "hash-3", future);

        int revoked = refreshTokens.revokeAllForUser(userId, Instant.now());

        assertThat(revoked).isEqualTo(2);
        assertThat(refreshTokens.findByTokenHash("hash-1")).get()
                .extracting(RefreshToken::getRevokedAt).isNotNull();
        assertThat(refreshTokens.findByTokenHash("hash-3")).get()
                .extracting(RefreshToken::getRevokedAt).isNull();
    }

    @Test
    @DisplayName("expired tokens can be swept without touching live ones")
    void deleteExpired() {
        UUID userId = createUser();
        Instant now = Instant.now();
        token(userId, "hash-old", now.minus(10, ChronoUnit.DAYS));
        token(userId, "hash-new", now.plus(10, ChronoUnit.DAYS));

        int deleted = refreshTokens.deleteExpiredBefore(now);

        assertThat(deleted).isEqualTo(1);
        assertThat(refreshTokens.findByTokenHash("hash-old")).isEmpty();
        assertThat(refreshTokens.findByTokenHash("hash-new")).isPresent();
    }
}
