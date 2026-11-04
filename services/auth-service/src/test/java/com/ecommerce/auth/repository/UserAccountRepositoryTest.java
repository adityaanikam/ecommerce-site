package com.ecommerce.auth.repository;

import com.ecommerce.auth.DockerAvailable;
import com.ecommerce.auth.model.Role;
import com.ecommerce.auth.model.UserAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JPA slice against a real PostgreSQL, with Flyway building the schema.
 *
 * Running against the real database is the point: the case-insensitive unique index
 * and the role CHECK constraint are PostgreSQL behaviour that an in-memory H2 would
 * either not have or would enforce differently, and those two constraints are what
 * stop duplicate accounts and invented roles.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@DockerAvailable
class UserAccountRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserAccountRepository users;

    private UserAccount account(String email) {
        return UserAccount.builder()
                .id(UUID.randomUUID())
                .email(email)
                .passwordHash("$2a$10$abcdefghijklmnopqrstuv")
                .firstName("Ada").lastName("Lovelace")
                .enabled(true)
                .roles(Set.of(Role.USER))
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Flyway created the schema, so a user round trips")
    void userRoundTrips() {
        UserAccount saved = users.saveAndFlush(account("ada@example.com"));

        assertThat(users.findById(saved.getId())).isPresent()
                .get().extracting(UserAccount::getEmail).isEqualTo("ada@example.com");
    }

    @Test
    @DisplayName("lookup by email ignores case")
    void lookupIgnoresCase() {
        users.saveAndFlush(account("ada@example.com"));

        assertThat(users.findByEmailIgnoreCase("ADA@EXAMPLE.COM")).isPresent();
        assertThat(users.findByEmailIgnoreCase("Ada@Example.Com")).isPresent();
        assertThat(users.existsByEmailIgnoreCase("ADA@example.com")).isTrue();
        assertThat(users.existsByEmailIgnoreCase("someone@else.com")).isFalse();
    }

    @Test
    @DisplayName("the database refuses a second account with the same email in a different case")
    void duplicateEmailIsRejectedByTheDatabase() {
        users.saveAndFlush(account("ada@example.com"));

        // This is the backstop behind the application's own existsByEmailIgnoreCase
        // check, for two registrations that race past it at the same moment.
        assertThatThrownBy(() -> users.saveAndFlush(account("ADA@Example.com")))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("roles are stored in their own table and come back with the user")
    void rolesRoundTrip() {
        UserAccount admin = account("root@example.com");
        admin.setRoles(Set.of(Role.USER, Role.ADMIN));
        UUID id = users.saveAndFlush(admin).getId();

        assertThat(users.findById(id)).get()
                .extracting(UserAccount::getRoles)
                .isEqualTo(Set.of(Role.USER, Role.ADMIN));
    }
}
