package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.AuthDtos;
import com.ecommerce.auth.model.Role;
import com.ecommerce.auth.model.UserAccount;
import com.ecommerce.auth.repository.UserAccountRepository;
import com.ecommerce.auth.security.JwtIssuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserAccountRepository users;

    @Mock
    private JwtIssuer jwtIssuer;

    @Mock
    private RefreshTokenService refreshTokens;

    // A real BCrypt encoder, not a mock: the hashing behaviour is the thing most
    // worth not getting wrong, and it is fast enough at the default strength.
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(users, passwordEncoder, jwtIssuer, refreshTokens);
        when(jwtIssuer.issue(any())).thenReturn(
                new JwtIssuer.IssuedToken("access-token", Instant.now().plusSeconds(900), 900));
        when(refreshTokens.issue(any())).thenReturn("refresh-token");
    }

    private static AuthDtos.RegisterRequest registration() {
        return new AuthDtos.RegisterRequest("Ada", "Lovelace", "Ada@Example.com", "Str0ng!Pass", "+447700900123");
    }

    private UserAccount existing(String email, String rawPassword, boolean enabled) {
        return UserAccount.builder()
                .id(UUID.randomUUID())
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .firstName("Ada").lastName("Lovelace")
                .enabled(enabled)
                .roles(Set.of(Role.USER))
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("registration stores a BCrypt hash, never the password")
    void registrationHashesPassword() {
        when(users.existsByEmailIgnoreCase(any())).thenReturn(false);

        userService.register(registration());

        ArgumentCaptor<UserAccount> saved = ArgumentCaptor.forClass(UserAccount.class);
        verify(users).saveAndFlush(saved.capture());

        String hash = saved.getValue().getPasswordHash();
        assertThat(hash).isNotEqualTo("Str0ng!Pass");
        assertThat(hash).startsWith("$2a$");
        assertThat(passwordEncoder.matches("Str0ng!Pass", hash)).isTrue();
    }

    @Test
    @DisplayName("registration lowercases the email so logins are case insensitive")
    void registrationNormalisesEmail() {
        when(users.existsByEmailIgnoreCase(any())).thenReturn(false);

        userService.register(registration());

        ArgumentCaptor<UserAccount> saved = ArgumentCaptor.forClass(UserAccount.class);
        verify(users).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("ada@example.com");
    }

    @Test
    @DisplayName("a new user gets USER and never ADMIN")
    void registrationGrantsUserRoleOnly() {
        when(users.existsByEmailIgnoreCase(any())).thenReturn(false);

        AuthDtos.AuthResult result = userService.register(registration());

        ArgumentCaptor<UserAccount> saved = ArgumentCaptor.forClass(UserAccount.class);
        verify(users).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getRoles()).containsExactly(Role.USER);
        assertThat(result.user().roles()).containsExactly("USER");
        assertThat(result.user().role()).isEqualTo("USER");
    }

    @Test
    @DisplayName("a duplicate email is a 409 and nothing is written")
    void duplicateEmailIsRejected() {
        when(users.existsByEmailIgnoreCase("ada@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(registration()))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(users, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("login succeeds with the right password and returns both tokens")
    void loginSucceeds() {
        when(users.findByEmailIgnoreCase("ada@example.com"))
                .thenReturn(Optional.of(existing("ada@example.com", "Str0ng!Pass", true)));

        AuthDtos.AuthResult result = userService.login(
                new AuthDtos.LoginRequest("ADA@example.com", "Str0ng!Pass"));

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.expiresIn()).isEqualTo(900);
        assertThat(result.user().email()).isEqualTo("ada@example.com");
    }

    @Test
    @DisplayName("a wrong password is 401 with the same message as an unknown email")
    void wrongPasswordIsRejected() {
        when(users.findByEmailIgnoreCase(any()))
                .thenReturn(Optional.of(existing("ada@example.com", "Str0ng!Pass", true)));

        assertThatThrownBy(() -> userService.login(
                new AuthDtos.LoginRequest("ada@example.com", "WrongPass1!")))
                .isInstanceOf(AuthException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    @DisplayName("an unknown email gives exactly the same error, so accounts cannot be enumerated")
    void unknownEmailGivesSameError() {
        when(users.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(
                new AuthDtos.LoginRequest("nobody@example.com", "Str0ng!Pass")))
                .isInstanceOf(AuthException.class)
                .hasMessage("Invalid email or password")
                .extracting(e -> ((AuthException) e).getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("a disabled account cannot log in even with the right password")
    void disabledAccountCannotLogIn() {
        when(users.findByEmailIgnoreCase(any()))
                .thenReturn(Optional.of(existing("ada@example.com", "Str0ng!Pass", false)));

        assertThatThrownBy(() -> userService.login(
                new AuthDtos.LoginRequest("ada@example.com", "Str0ng!Pass")))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("refresh consumes the old token and issues a fresh pair")
    void refreshRotates() {
        UserAccount account = existing("ada@example.com", "Str0ng!Pass", true);
        when(refreshTokens.consume("old-refresh")).thenReturn(account.getId());
        when(users.findById(account.getId())).thenReturn(Optional.of(account));

        AuthDtos.AuthResult result = userService.refresh("old-refresh");

        verify(refreshTokens).consume("old-refresh");
        verify(refreshTokens).issue(account.getId());
        assertThat(result.accessToken()).isEqualTo("access-token");
    }

    @Test
    @DisplayName("logout revokes every refresh token the user holds")
    void logoutRevokesEverything() {
        UUID userId = UUID.randomUUID();
        userService.logout(userId);
        verify(refreshTokens).revokeAllForUser(userId);
    }

    @Test
    @DisplayName("an admin's singular role field reads ADMIN")
    void adminRoleSurfacesInView() {
        UserAccount admin = UserAccount.builder()
                .id(UUID.randomUUID()).email("root@example.com").passwordHash("x")
                .firstName("Root").lastName("User").enabled(true)
                .roles(Set.of(Role.USER, Role.ADMIN))
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();

        AuthDtos.UserView view = UserService.toView(admin);

        assertThat(view.role()).isEqualTo("ADMIN");
        assertThat(view.roles()).containsExactly("ADMIN", "USER");
    }
}
