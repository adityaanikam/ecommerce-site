package com.ecommerce.auth.service;

import org.springframework.http.HttpStatus;

/**
 * Carries the status the caller should see along with a message that is safe to show
 * them. Everything else becomes a 500 with no detail leaked.
 */
public class AuthException extends RuntimeException {

    private final HttpStatus status;

    public AuthException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static AuthException emailTaken() {
        return new AuthException(HttpStatus.CONFLICT, "An account with that email already exists");
    }

    /**
     * One message for "no such user" and for "wrong password", on purpose. Telling
     * the two apart hands an attacker a way to enumerate which emails are registered.
     */
    public static AuthException badCredentials() {
        return new AuthException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    public static AuthException accountDisabled() {
        return new AuthException(HttpStatus.FORBIDDEN, "This account is disabled");
    }

    public static AuthException invalidRefreshToken() {
        return new AuthException(HttpStatus.UNAUTHORIZED, "The refresh token is invalid or has expired");
    }

    public static AuthException notAuthenticated() {
        return new AuthException(HttpStatus.UNAUTHORIZED, "Not authenticated");
    }

    public static AuthException userNotFound() {
        return new AuthException(HttpStatus.NOT_FOUND, "User not found");
    }
}
