package com.ecommerce.integration;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.testdata.UserTestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers
class UserRepositoryIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testUser = UserTestDataBuilder.aUser()
                .withEmail("test@example.com")
                .withFirstName("John")
                .withLastName("Doe")
                .build();
    }

    @Test
    void save_ShouldSaveUserSuccessfully() {
        // When
        User savedUser = userRepository.save(testUser);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getFirstName()).isEqualTo("John");
        assertThat(savedUser.getLastName()).isEqualTo("Doe");
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenUserExists() {
        // Given
        userRepository.save(testUser);

        // When
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findByRolesContaining_ShouldReturnUsersWithRole() {
        // Given
        User adminUser = UserTestDataBuilder.aUser()
                .withEmail("admin@example.com")
                .asAdmin()
                .build();
        User regularUser = UserTestDataBuilder.aUser()
                .withEmail("user@example.com")
                .build();

        userRepository.save(adminUser);
        userRepository.save(regularUser);

        // When
        List<User> adminUsers = userRepository.findByRolesContaining(User.Role.ADMIN);

        // Then
        assertThat(adminUsers).hasSize(1);
        assertThat(adminUsers.get(0).getEmail()).isEqualTo("admin@example.com");
    }

    @Test
    void findByIsActiveTrue_ShouldReturnOnlyActiveUsers() {
        // Given
        User activeUser = UserTestDataBuilder.aUser()
                .withEmail("active@example.com")
                .build();
        User inactiveUser = UserTestDataBuilder.aUser()
                .withEmail("inactive@example.com")
                .inactive()
                .build();

        userRepository.save(activeUser);
        userRepository.save(inactiveUser);

        // When
        List<User> activeUsers = userRepository.findByIsActiveTrue();

        // Then
        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getEmail()).isEqualTo("active@example.com");
    }

    @Test
    void countByIsActiveTrue_ShouldReturnCorrectCount() {
        // Given
        User activeUser1 = UserTestDataBuilder.aUser()
                .withEmail("active1@example.com")
                .build();
        User activeUser2 = UserTestDataBuilder.aUser()
                .withEmail("active2@example.com")
                .build();
        User inactiveUser = UserTestDataBuilder.aUser()
                .withEmail("inactive@example.com")
                .inactive()
                .build();

        userRepository.save(activeUser1);
        userRepository.save(activeUser2);
        userRepository.save(inactiveUser);

        // When
        long activeCount = userRepository.countByIsActiveTrue();

        // Then
        assertThat(activeCount).isEqualTo(2);
    }
}
