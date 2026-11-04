package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.ApiEnvelope;
import com.ecommerce.auth.dto.AuthDtos;
import com.ecommerce.auth.service.AuthException;
import com.ecommerce.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * The /users side of the same service, which is what the gateway routes
 * /api/users/** to.
 *
 * Only /users/me is implemented, because that is the only /users endpoint the
 * frontend reaches today (see docs/AUDIT.md). The rest of the frontend's user API
 * surface, addresses, wishlists, notifications and so on, has no backend and is
 * listed as not done in docs/STATUS.md rather than stubbed out here with something
 * that returns fake data.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiEnvelope<AuthDtos.UserView>> me(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) {
            throw AuthException.notAuthenticated();
        }
        try {
            return ResponseEntity.ok(ApiEnvelope.ok(userService.currentUser(UUID.fromString(userId))));
        } catch (IllegalArgumentException e) {
            throw AuthException.notAuthenticated();
        }
    }
}
