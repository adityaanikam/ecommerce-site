package com.ecommerce.service;

import com.ecommerce.dto.UserDto;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.ValidationException;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CacheService cacheService;
    
    // User Registration and Profile Management
    
    public UserDto.UserResponse createUser(UserDto.CreateUserRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());
        
        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already exists: " + request.getEmail());
        }
        
        // Validate phone uniqueness if provided
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new ValidationException("Phone number already exists: " + request.getPhone());
        }
        
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .roles(Set.of(User.Role.USER))
                .provider(User.AuthProvider.LOCAL)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        
        // Clear cache
        cacheService.evictUserCache(savedUser.getId());
        
        return mapToUserResponse(savedUser);
    }
    
    public UserDto.UserResponse getUserById(String id) {
        log.debug("Fetching user by ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        return mapToUserResponse(user);
    }
    
    public UserDto.UserResponse getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        
        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        return mapToUserResponse(user);
    }
    
    public Page<UserDto.UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination");
        
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::mapToUserResponse);
    }
    
    public UserDto.UserResponse updateUser(String id, UserDto.UpdateUserRequest request) {
        log.info("Updating user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        // Validate email uniqueness if changed
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already exists: " + request.getEmail());
        }
        
        // Validate phone uniqueness if changed
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone()) 
            && userRepository.existsByPhone(request.getPhone())) {
            throw new ValidationException("Phone number already exists: " + request.getPhone());
        }
        
        // Update fields
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setImageUrl(request.getImageUrl());
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", savedUser.getId());
        
        // Clear cache
        cacheService.evictUserCache(savedUser.getId());
        
        return mapToUserResponse(savedUser);
    }
    
    public void deleteUser(String id) {
        log.info("Deleting user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        // Soft delete
        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Clear cache
        cacheService.evictUserCache(id);
        
        log.info("User deleted successfully with ID: {}", id);
    }
    
    // Address Management
    
    public UserDto.UserResponse addAddress(String userId, UserDto.AddressRequest addressRequest) {
        log.info("Adding address for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        User.Address address = User.Address.builder()
                .street(addressRequest.getStreet())
                .city(addressRequest.getCity())
                .state(addressRequest.getState())
                .zipCode(addressRequest.getZipCode())
                .country(addressRequest.getCountry())
                .isDefault(addressRequest.getIsDefault())
                .build();
        
        // If this is set as default, unset other default addresses
        if (address.getIsDefault()) {
            user.getAddresses().forEach(addr -> addr.setIsDefault(false));
        }
        
        user.getAddresses().add(address);
        User savedUser = userRepository.save(user);
        
        // Clear cache
        cacheService.evictUserCache(userId);
        
        log.info("Address added successfully for user ID: {}", userId);
        return mapToUserResponse(savedUser);
    }
    
    public UserDto.UserResponse updateAddress(String userId, String addressId, UserDto.AddressRequest addressRequest) {
        log.info("Updating address {} for user ID: {}", addressId, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        User.Address address = user.getAddresses().stream()
                .filter(addr -> addr.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));
        
        // Update address fields
        address.setStreet(addressRequest.getStreet());
        address.setCity(addressRequest.getCity());
        address.setState(addressRequest.getState());
        address.setZipCode(addressRequest.getZipCode());
        address.setCountry(addressRequest.getCountry());
        
        // If this is set as default, unset other default addresses
        if (addressRequest.getIsDefault()) {
            user.getAddresses().forEach(addr -> {
                if (!addr.getId().equals(addressId)) {
                    addr.setIsDefault(false);
                }
            });
        }
        address.setIsDefault(addressRequest.getIsDefault());
        
        User savedUser = userRepository.save(user);
        
        // Clear cache
        cacheService.evictUserCache(userId);
        
        log.info("Address updated successfully for user ID: {}", userId);
        return mapToUserResponse(savedUser);
    }
    
    public UserDto.UserResponse removeAddress(String userId, String addressId) {
        log.info("Removing address {} for user ID: {}", addressId, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        boolean removed = user.getAddresses().removeIf(addr -> addr.getId().equals(addressId));
        
        if (!removed) {
            throw new ResourceNotFoundException("Address not found with ID: " + addressId);
        }
        
        User savedUser = userRepository.save(user);
        
        // Clear cache
        cacheService.evictUserCache(userId);
        
        log.info("Address removed successfully for user ID: {}", userId);
        return mapToUserResponse(savedUser);
    }
    
    // Role Management
    
    public UserDto.UserResponse assignRole(String userId, User.Role role) {
        log.info("Assigning role {} to user ID: {}", role, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        user.getRoles().add(role);
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        
        // Clear cache
        cacheService.evictUserCache(userId);
        
        log.info("Role {} assigned successfully to user ID: {}", role, userId);
        return mapToUserResponse(savedUser);
    }
    
    public UserDto.UserResponse removeRole(String userId, User.Role role) {
        log.info("Removing role {} from user ID: {}", role, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Prevent removing the last role
        if (user.getRoles().size() <= 1) {
            throw new ValidationException("Cannot remove the last role from user");
        }
        
        user.getRoles().remove(role);
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        
        // Clear cache
        cacheService.evictUserCache(userId);
        
        log.info("Role {} removed successfully from user ID: {}", role, userId);
        return mapToUserResponse(savedUser);
    }
    
    // Search and Filter Operations
    
    public List<UserDto.UserResponse> searchUsers(String query) {
        log.debug("Searching users with query: {}", query);
        
        List<User> users = userRepository.findByEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                query, query, query);
        
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }
    
    public List<UserDto.UserResponse> getUsersByRole(User.Role role) {
        log.debug("Fetching users by role: {}", role);
        
        List<User> users = userRepository.findByRolesContaining(role);
        
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }
    
    public List<UserDto.UserResponse> getActiveUsers() {
        log.debug("Fetching all active users");
        
        List<User> users = userRepository.findByIsActiveTrue();
        
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }
    
    // Password Management
    
    public void changePassword(String userId, String currentPassword, String newPassword) {
        log.info("Changing password for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        
        // Clear cache
        cacheService.evictUserCache(userId);
        
        log.info("Password changed successfully for user ID: {}", userId);
    }
    
    public void resetPassword(String email, String newPassword) {
        log.info("Resetting password for email: {}", email);
        
        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        
        // Clear cache
        cacheService.evictUserCache(user.getId());
        
        log.info("Password reset successfully for email: {}", email);
    }
    
    // Utility Methods
    
    private UserDto.UserResponse mapToUserResponse(User user) {
        return UserDto.UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .imageUrl(user.getImageUrl())
                .roles(user.getRoles())
                .addresses(user.getAddresses())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
}

    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }
}