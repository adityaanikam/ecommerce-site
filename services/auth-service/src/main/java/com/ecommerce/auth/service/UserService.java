package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.AuthDtos;
import com.ecommerce.auth.model.Role;
import com.ecommerce.auth.model.UserAccount;
import com.ecommerce.auth.repository.UserAccountRepository;
import com.ecommerce.auth.security.JwtIssuer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtIssuer jwtIssuer;
    private final RefreshTokenService refreshTokens;

    @Transactional
    public AuthDtos.AuthResult register(AuthDtos.RegisterRequest request) {
        String email = normaliseEmail(request.email());

        // Checked here for a clean 409, and enforced again by uq_users_email_lower
        // for the case where two registrations race past this check.
        if (users.existsByEmailIgnoreCase(email)) {
            throw AuthException.emailTaken();
        }

        Instant now = Instant.now();
        UserAccount account = UserAccount.builder()
                .id(UUID.randomUUID())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .phoneNumber(blankToNull(request.phoneNumber()))
                .enabled(true)
                // Everyone registers as a USER. There is no self-service route to
                // ADMIN, which is granted out of band.
                .roles(Set.of(Role.USER))
                .createdAt(now)
                .updatedAt(now)
                .build();

        try {
            users.saveAndFlush(account);
        } catch (DataIntegrityViolationException e) {
            // The unique index caught a concurrent registration of the same email.
            throw AuthException.emailTaken();
        }

        log.info("Registered user {}", account.getId());
        return issueFor(account);
    }

    @Transactional
    public AuthDtos.AuthResult login(AuthDtos.LoginRequest request) {
        UserAccount account = users.findByEmailIgnoreCase(normaliseEmail(request.email()))
                .orElse(null);

        if (account == null) {
            // Hash anyway so a request for an unknown email takes about as long as
            // one for a known email. Otherwise response timing leaks which addresses
            // are registered, which is the thing the shared error message is trying
            // to hide in the first place.
            passwordEncoder.encode(request.password());
            throw AuthException.badCredentials();
        }

        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw AuthException.badCredentials();
        }

        if (!account.isEnabled()) {
            throw AuthException.accountDisabled();
        }

        log.info("User {} logged in", account.getId());
        return issueFor(account);
    }

    @Transactional
    public AuthDtos.AuthResult refresh(String refreshToken) {
        UUID userId = refreshTokens.consume(refreshToken);
        UserAccount account = users.findById(userId).orElseThrow(AuthException::invalidRefreshToken);
        if (!account.isEnabled()) {
            throw AuthException.accountDisabled();
        }
        return issueFor(account);
    }

    @Transactional
    public void logout(UUID userId) {
        refreshTokens.revokeAllForUser(userId);
    }

    @Transactional(readOnly = true)
    public AuthDtos.UserView currentUser(UUID userId) {
        return toView(users.findById(userId).orElseThrow(AuthException::userNotFound));
    }

    private AuthDtos.AuthResult issueFor(UserAccount account) {
        JwtIssuer.IssuedToken access = jwtIssuer.issue(account);
        String refresh = refreshTokens.issue(account.getId());
        return new AuthDtos.AuthResult(
                access.token(), refresh, access.expiresInSeconds(), toView(account));
    }

    public static AuthDtos.UserView toView(UserAccount account) {
        List<String> roles = account.getRoles().stream().map(Role::name).sorted().toList();
        return new AuthDtos.UserView(
                account.getId().toString(),
                account.getEmail(),
                account.getFirstName(),
                account.getLastName(),
                account.getPhoneNumber(),
                // The frontend's UserDto carries both a single role and a list. ADMIN
                // wins for the singular field, since that is the one guards read.
                roles.contains(Role.ADMIN.name()) ? Role.ADMIN.name() : Role.USER.name(),
                roles,
                account.getCreatedAt().toString(),
                account.getUpdatedAt().toString());
    }

    private static String normaliseEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
