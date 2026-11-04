package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.AuthDtos;
import com.ecommerce.auth.security.JwtIssuer;
import com.ecommerce.auth.service.AuthException;
import com.ecommerce.auth.service.UserService;
import com.ecommerce.auth.web.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP slice.
 *
 * The two things worth pinning here are the envelope shape, because the frontend
 * reads response.data.data, and that validation rejects a weak password before any
 * service code runs.
 */
@WebMvcTest(controllers = {AuthController.class, UserController.class})
@Import(GlobalExceptionHandler.class)
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtIssuer jwtIssuer;

    private static final String USER_ID = "11111111-2222-3333-4444-555555555555";

    private static AuthDtos.UserView view() {
        return new AuthDtos.UserView(USER_ID, "ada@example.com", "Ada", "Lovelace",
                null, "USER", List.of("USER"), "2026-01-01T00:00:00Z", "2026-01-01T00:00:00Z");
    }

    private static AuthDtos.AuthResult result() {
        return new AuthDtos.AuthResult("access-token", "refresh-token", 900, view());
    }

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    @Test
    @DisplayName("register returns 201 and the envelope the frontend unwraps")
    void registerReturnsEnvelope() throws Exception {
        when(userService.register(any())).thenReturn(result());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "firstName", "Ada", "lastName", "Lovelace",
                                "email", "ada@example.com", "password", "Str0ng!Pass",
                                "phoneNumber", "+447700900123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.expiresIn").value(900))
                .andExpect(jsonPath("$.data.user.email").value("ada@example.com"))
                .andExpect(jsonPath("$.data.user.role").value("USER"));
    }

    @Test
    @DisplayName("a weak password is rejected with per-field errors and never reaches the service")
    void weakPasswordIsRejected() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "firstName", "Ada", "lastName", "Lovelace",
                                "email", "ada@example.com", "password", "weak"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray());

        verify(userService, never()).register(any());
    }

    @Test
    @DisplayName("a malformed email is rejected by the carried-over EmailValidator")
    void malformedEmailIsRejected() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "firstName", "Ada", "lastName", "Lovelace",
                                "email", "not-an-email", "password", "Str0ng!Pass"))))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any());
    }

    @Test
    @DisplayName("a duplicate email surfaces as 409 with a readable message")
    void duplicateEmailIsConflict() throws Exception {
        when(userService.register(any())).thenThrow(AuthException.emailTaken());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "firstName", "Ada", "lastName", "Lovelace",
                                "email", "ada@example.com", "password", "Str0ng!Pass"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An account with that email already exists"));
    }

    @Test
    @DisplayName("login returns the envelope")
    void loginReturnsEnvelope() throws Exception {
        when(userService.login(any())).thenReturn(result());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "ada@example.com", "password", "Str0ng!Pass"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("bad credentials are 401 with one message that does not say which half was wrong")
    void badCredentialsAre401() throws Exception {
        when(userService.login(any())).thenThrow(AuthException.badCredentials());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "ada@example.com", "password", "nope"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("/auth/me reads the user id the gateway put in X-User-Id")
    void meUsesGatewayHeader() throws Exception {
        when(userService.currentUser(UUID.fromString(USER_ID))).thenReturn(view());

        mockMvc.perform(get("/auth/me").header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(USER_ID));
    }

    @Test
    @DisplayName("/auth/me without any identity is 401")
    void meWithoutIdentityIs401() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authenticated"));
    }

    @Test
    @DisplayName("/users/me is the same answer on the route the gateway sends /api/users to")
    void usersMeWorks() throws Exception {
        when(userService.currentUser(UUID.fromString(USER_ID))).thenReturn(view());

        mockMvc.perform(get("/users/me").header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("ada@example.com"));
    }

    @Test
    @DisplayName("a junk X-User-Id is 401 rather than a 500")
    void junkUserIdIs401() throws Exception {
        mockMvc.perform(get("/users/me").header("X-User-Id", "not-a-uuid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("refresh needs a token in the body")
    void refreshRequiresToken() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("logout revokes and returns a success envelope")
    void logoutWorks() throws Exception {
        mockMvc.perform(post("/auth/logout").header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(userService).logout(UUID.fromString(USER_ID));
    }
}
