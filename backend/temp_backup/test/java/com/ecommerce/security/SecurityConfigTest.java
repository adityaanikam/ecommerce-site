package com.ecommerce.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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
class SecurityConfigTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void publicEndpoints_ShouldBeAccessibleWithoutAuthentication() throws Exception {
        // Test public endpoints
        mockMvc.perform(get("/api/auth/health"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register"))
                .andExpect(status().isBadRequest()); // Bad request due to missing body, but not unauthorized

        mockMvc.perform(post("/api/auth/login"))
                .andExpect(status().isBadRequest()); // Bad request due to missing body, but not unauthorized
    }

    @Test
    void protectedEndpoints_ShouldRequireAuthentication() throws Exception {
        // Test protected endpoints without authentication
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userEndpoints_ShouldBeAccessibleWithUserRole() throws Exception {
        // Test user endpoints with USER role
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminEndpoints_ShouldBeForbiddenForUserRole() throws Exception {
        // Test admin endpoints with USER role
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/users/123"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/analytics"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEndpoints_ShouldBeAccessibleWithAdminRole() throws Exception {
        // Test admin endpoints with ADMIN role
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/analytics"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void sellerEndpoints_ShouldBeAccessibleWithSellerRole() throws Exception {
        // Test seller endpoints with SELLER role
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/products"))
                .andExpect(status().isBadRequest()); // Bad request due to missing body, but not forbidden
    }

    @Test
    void corsHeaders_ShouldBePresent() throws Exception {
        mockMvc.perform(options("/api/users/profile")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }

    @Test
    void securityHeaders_ShouldBePresent() throws Exception {
        mockMvc.perform(get("/api/auth/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-XSS-Protection", "1; mode=block"));
    }
}
