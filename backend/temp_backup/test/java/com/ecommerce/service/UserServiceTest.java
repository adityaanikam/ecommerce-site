package com.ecommerce.service;

import com.ecommerce.dto.UserDto;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.testdata.UserTestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDto.CreateUserRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = UserTestDataBuilder.aUser()
                .withId("user123")
                .withEmail("test@example.com")
                .build();

        createRequest = UserDto.CreateUserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .phone("+1234567890")
                .build();
    }

    @Test
    void createUser_ShouldCreateUserSuccessfully() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto.UserResponse result = userService.createUser(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> userService.createUser(createRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User with email already exists");

        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        // Given
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));

        // When
        UserDto.UserResponse result = userService.getUserById("user123");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("user123");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(userRepository).findById("user123");
    }

    @Test
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById("nonexistent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById("nonexistent");
    }

    @Test
    void updateUser_ShouldUpdateUserSuccessfully() {
        // Given
        UserDto.UpdateUserRequest updateRequest = UserDto.UpdateUserRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phone("+0987654321")
                .build();

        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto.UserResponse result = userService.updateUser("user123", updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findById("user123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldDeleteUserSuccessfully() {
        // Given
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).deleteById("user123");

        // When
        userService.deleteUser("user123");

        // Then
        verify(userRepository).findById("user123");
        verify(userRepository).deleteById("user123");
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser("nonexistent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById("nonexistent");
        verify(userRepository, never()).deleteById(anyString());
    }
}
