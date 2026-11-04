package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.ApiEnvelope;
import com.ecommerce.auth.dto.AuthDtos;
import com.ecommerce.auth.security.JwtIssuer;
import com.ecommerce.auth.service.AuthException;
import com.ecommerce.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Routes are served at the root; the gateway owns /api and strips it, so
 * /api/auth/login arrives here as /auth/login.
 *
 * There is no @CrossOrigin anywhere in this service. CORS is the gateway's job.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtIssuer jwtIssuer;

    @PostMapping("/register")
    public ResponseEntity<ApiEnvelope<AuthDtos.AuthResult>> register(
            @Valid @RequestBody AuthDtos.RegisterRequest request) {
        AuthDtos.AuthResult result = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiEnvelope.ok(result, "Registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiEnvelope<AuthDtos.AuthResult>> login(
            @Valid @RequestBody AuthDtos.LoginRequest request) {
        return ResponseEntity.ok(ApiEnvelope.ok(userService.login(request), "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiEnvelope<AuthDtos.AuthResult>> refresh(
            @Valid @RequestBody AuthDtos.RefreshRequest request) {
        return ResponseEntity.ok(ApiEnvelope.ok(userService.refresh(request.refreshToken())));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiEnvelope<AuthDtos.UserView>> me(HttpServletRequest request) {
        return ResponseEntity.ok(ApiEnvelope.ok(userService.currentUser(resolveUserId(request))));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiEnvelope<Void>> logout(HttpServletRequest request) {
        userService.logout(resolveUserId(request));
        return ResponseEntity.ok(ApiEnvelope.ok(null, "Logged out"));
    }

    /**
     * Who is calling.
     *
     * Behind the gateway, X-User-Id is already there and already trustworthy: the
     * gateway verified the token and strips any copy the client tried to send. The
     * Authorization fallback exists so the service is still usable when called
     * directly, which is what tests and service-to-service calls do. Both paths end
     * up at a verified signature, just verified in different places.
     */
    private UUID resolveUserId(HttpServletRequest request) {
        String headerUserId = request.getHeader("X-User-Id");
        if (headerUserId != null && !headerUserId.isBlank()) {
            return parse(headerUserId);
        }

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring("Bearer ".length()).trim();
            return jwtIssuer.parse(token)
                    .map(claims -> parse(claims.getSubject()))
                    .orElseThrow(AuthException::notAuthenticated);
        }

        throw AuthException.notAuthenticated();
    }

    private static UUID parse(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw AuthException.notAuthenticated();
        }
    }
}
