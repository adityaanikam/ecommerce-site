package com.ecommerce.controller;

import com.ecommerce.dto.UserDto;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.testdata.UserTestDataBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@Testcontainers
@Transactional
class UserControllerIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        userRepository.deleteAll();
        testUser = UserTestDataBuilder.aUser()
                .withEmail("test@example.com")
                .withFirstName("John")
                .withLastName("Doe")
                .build();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserProfile_ShouldReturnUserProfile_WhenUserIsAuthenticated() throws Exception {
        // Given
        User savedUser = userRepository.save(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"));
    }

    @Test
    void getUserProfile_ShouldReturnUnauthorized_WhenUserIsNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateUserProfile_ShouldUpdateProfileSuccessfully() throws Exception {
        // Given
        User savedUser = userRepository.save(testUser);
        UserDto.UpdateUserRequest updateRequest = UserDto.UpdateUserRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phone("+1234567890")
                .build();

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Jane"))
                .andExpect(jsonPath("$.data.lastName").value("Smith"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateUserProfile_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        // Given
        UserDto.UpdateUserRequest updateRequest = UserDto.UpdateUserRequest.builder()
                .firstName("") // Invalid: empty first name
                .build();

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldReturnAllUsers_WhenUserIsAdmin() throws Exception {
        // Given
        User user1 = UserTestDataBuilder.aUser()
                .withEmail("user1@example.com")
                .build();
        User user2 = UserTestDataBuilder.aUser()
                .withEmail("user2@example.com")
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        // When & Then
        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_ShouldReturnForbidden_WhenUserIsNotAdmin() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldDeleteUserSuccessfully_WhenUserIsAdmin() throws Exception {
        // Given
        User userToDelete = userRepository.save(testUser);

        // When & Then
        mockMvc.perform(delete("/api/users/{id}", userToDelete.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUser_ShouldReturnForbidden_WhenUserIsNotAdmin() throws Exception {
        // Given
        User userToDelete = userRepository.save(testUser);

        // When & Then
        mockMvc.perform(delete("/api/users/{id}", userToDelete.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
