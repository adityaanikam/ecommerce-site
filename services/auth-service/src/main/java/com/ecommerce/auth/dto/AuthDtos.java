package com.ecommerce.auth.dto;

import com.ecommerce.auth.validation.ValidEmail;
import com.ecommerce.auth.validation.ValidPassword;
import com.ecommerce.auth.validation.ValidPhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request and response shapes.
 *
 * These are deliberately local to this service and small enough to duplicate rather
 * than share. order-service has its own copy of what it needs; neither imports the
 * other's classes, so neither can be broken by the other's release.
 *
 * The field names match what the frontend already sends and reads, which is why
 * registration takes firstName/lastName/phoneNumber and the response carries
 * accessToken, refreshToken, expiresIn and user.
 */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "First name is required")
            @Size(max = 100, message = "First name must be at most 100 characters")
            String firstName,

            @NotBlank(message = "Last name is required")
            @Size(max = 100, message = "Last name must be at most 100 characters")
            String lastName,

            @NotBlank(message = "Email is required")
            @ValidEmail
            String email,

            // The monolith's PasswordValidator, finally used: at least 8 characters
            // with an uppercase, a lowercase, a digit and a special character.
            @ValidPassword
            String password,

            // allowEmpty defaults to true on this constraint, so an omitted phone
            // number is fine but a malformed one is rejected.
            @ValidPhoneNumber(countryCode = "INTERNATIONAL")
            String phoneNumber
    ) {
    }

    public record LoginRequest(
            @NotBlank(message = "Email is required") String email,
            @NotBlank(message = "Password is required") String password
    ) {
    }

    public record RefreshRequest(
            @NotBlank(message = "A refresh token is required") String refreshToken
    ) {
    }

    public record UserView(
            String id,
            String email,
            String firstName,
            String lastName,
            String phoneNumber,
            String role,
            List<String> roles,
            String createdAt,
            String updatedAt
    ) {
    }

    /**
     * expiresIn is in seconds, matching the frontend's AuthResponse type.
     */
    public record AuthResult(
            String accessToken,
            String refreshToken,
            long expiresIn,
            UserView user
    ) {
    }
}
