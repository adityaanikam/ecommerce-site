package com.ecommerce.controller;

import com.ecommerce.dto.UserDto;
import com.ecommerce.model.User;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    
    // Profile Management
    
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<UserDto.UserResponse> getCurrentUser(Authentication authentication) {
        log.info("Get current user request received");
        String userId = authentication.getName();
        UserDto.UserResponse user = userService.getUserByEmail(userId);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<UserDto.UserResponse> updateCurrentUser(@Valid @RequestBody UserDto.UpdateUserRequest request,
                                                                Authentication authentication) {
        log.info("Update current user request received");
        String userId = authentication.getName();
        UserDto.UserResponse user = userService.getUserByEmail(userId);
        UserDto.UserResponse updatedUser = userService.updateUser(user.getId(), request);
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<UserDto.MessageResponse> deleteCurrentUser(Authentication authentication) {
        log.info("Delete current user request received");
        String userId = authentication.getName();
        UserDto.UserResponse user = userService.getUserByEmail(userId);
        userService.deleteUser(user.getId());
        return ResponseEntity.ok(UserDto.MessageResponse.builder()
                .message("Account deleted successfully")
                .build());
    }
    
    @PostMapping("/me/avatar")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<UserDto.UserResponse> uploadAvatar(@RequestParam("file") MultipartFile file,
                                                           Authentication authentication) {
        log.info("Upload avatar request received");
        String userId = authentication.getName();
        UserDto.UserResponse user = userService.getUserByEmail(userId);
        // TODO: Implement avatar upload logic
        return ResponseEntity.ok(user);
    }
    
    // Address Management
    
    @GetMapping("/me/addresses")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<List<User.Address>> getCurrentUserAddresses(Authentication authentication) {
        log.info("Get current user addresses request received");
        String userId = authentication.getName();
        UserDto.UserResponse user = userService.getUserByEmail(userId);
        return ResponseEntity.ok(user.getAddresses());
    }
    
    @PostMapping("/me/addresses")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<UserDto.UserResponse> addAddress(@Valid @RequestBody UserDto.AddressRequest request,
                                                         Authentication authentication) {
        log.info("Add address request received");
        String userId = authentication.getName();
        UserDto.UserResponse user = userService.getUserByEmail(userId);
        UserDto.UserResponse updatedUser = userService.addAddress(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedUser);
    }
    
    @PutMapping("/me/addresses/{addressId}")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<UserDto.UserResponse> updateAddress(@PathVariable String addressId,
                                                            @Valid @RequestBody UserDto.AddressRequest request,
                                                            Authentication authentication) {
        log.info("Update address request received for address ID: {}", addressId);
        String userId = authentication.getName();
        UserDto.UserResponse user = userService.getUserByEmail(userId);
        UserDto.UserResponse updatedUser = userService.updateAddress(user.getId(), addressId, request);
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/me/addresses/{addressId}")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<UserDto.UserResponse> removeAddress(@PathVariable String addressId,
                                                            Authentication authentication) {
        log.info("Remove address request received for address ID: {}", addressId);
        String userId = authentication.getName();
        UserDto.UserResponse user = userService.getUserByEmail(userId);
        UserDto.UserResponse updatedUser = userService.removeAddress(user.getId(), addressId);
        return ResponseEntity.ok(updatedUser);
    }
    
    // Order History
    
    @GetMapping("/me/orders")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<List<UserDto.OrderHistoryResponse>> getCurrentUserOrderHistory(Authentication authentication) {
        log.info("Get current user order history request received");
        String userId = authentication.getName();
        UserDto.UserResponse user = userService.getUserByEmail(userId);
        // TODO: Implement order history logic
        return ResponseEntity.ok(List.of());
    }
    
    // Admin Operations
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto.UserResponse>> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {
        log.info("Get all users request received");
        Page<UserDto.UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto.UserResponse> getUserById(@PathVariable String userId) {
        log.info("Get user by ID request received: {}", userId);
        UserDto.UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto.UserResponse> updateUser(@PathVariable String userId,
                                                         @Valid @RequestBody UserDto.UpdateUserRequest request) {
        log.info("Update user request received for user ID: {}", userId);
        UserDto.UserResponse updatedUser = userService.updateUser(userId, request);
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto.MessageResponse> deleteUser(@PathVariable String userId) {
        log.info("Delete user request received for user ID: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok(UserDto.MessageResponse.builder()
                .message("User deleted successfully")
                .build());
    }
    
    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto.UserResponse> assignRole(@PathVariable String userId,
                                                         @RequestParam User.Role role) {
        log.info("Assign role request received for user ID: {} with role: {}", userId, role);
        UserDto.UserResponse updatedUser = userService.assignRole(userId, role);
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto.UserResponse> removeRole(@PathVariable String userId,
                                                         @RequestParam User.Role role) {
        log.info("Remove role request received for user ID: {} with role: {}", userId, role);
        UserDto.UserResponse updatedUser = userService.removeRole(userId, role);
        return ResponseEntity.ok(updatedUser);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto.UserResponse>> searchUsers(@RequestParam String query) {
        log.info("Search users request received with query: {}", query);
        List<UserDto.UserResponse> users = userService.searchUsers(query);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/by-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto.UserResponse>> getUsersByRole(@RequestParam User.Role role) {
        log.info("Get users by role request received for role: {}", role);
        List<UserDto.UserResponse> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto.UserResponse>> getActiveUsers() {
        log.info("Get active users request received");
        List<UserDto.UserResponse> users = userService.getActiveUsers();
        return ResponseEntity.ok(users);
    }
}