package com.ecommerce.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenServiceHashTest {

    @Test
    @DisplayName("the hash is stable, 64 hex characters, and is not the token itself")
    void hashIsStableSha256Hex() {
        String token = "some-refresh-token-value";

        String first = RefreshTokenService.hash(token);
        String second = RefreshTokenService.hash(token);

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSize(64).matches("[0-9a-f]{64}");
        assertThat(first).isNotEqualTo(token);
    }

    @Test
    @DisplayName("different tokens hash differently")
    void differentTokensDiffer() {
        assertThat(RefreshTokenService.hash("a")).isNotEqualTo(RefreshTokenService.hash("b"));
    }

    @Test
    @DisplayName("a known vector, so the algorithm cannot be silently swapped")
    void knownVector() {
        // SHA-256 of the empty string.
        assertThat(RefreshTokenService.hash(""))
                .isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }
}
