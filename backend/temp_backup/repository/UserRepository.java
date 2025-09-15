package com.ecommerce.repository;

import com.ecommerce.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    // Basic queries
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhone(String phone);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
    
    // Active user queries
    @Query("{'email': ?0, 'isActive': true}")
    Optional<User> findByEmailAndIsActiveTrue(String email);
    
    @Query("{'phone': ?0, 'isActive': true}")
    Optional<User> findByPhoneAndIsActiveTrue(String phone);
    
    // Role-based queries
    @Query("{'roles': ?0, 'isActive': true}")
    List<User> findByRoleAndIsActiveTrue(User.Role role);
    
    @Query("{'roles': ?0, 'isActive': true}")
    Page<User> findByRoleAndIsActiveTrue(User.Role role, Pageable pageable);
    
    // Provider-based queries
    @Query("{'provider': ?0, 'isActive': true}")
    List<User> findByProviderAndIsActiveTrue(User.AuthProvider provider);
    
    @Query("{'provider': ?0, 'providerId': ?1, 'isActive': true}")
    Optional<User> findByProviderAndProviderIdAndIsActiveTrue(User.AuthProvider provider, String providerId);
    
    // Date range queries
    @Query("{'createdAt': {$gte: ?0, $lte: ?1}, 'isActive': true}")
    List<User> findByCreatedAtBetweenAndIsActiveTrue(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("{'createdAt': {$gte: ?0, $lte: ?1}, 'isActive': true}")
    Page<User> findByCreatedAtBetweenAndIsActiveTrue(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Search queries
    @Query("{'$or': [{'firstName': {$regex: ?0, $options: 'i'}}, {'lastName': {$regex: ?0, $options: 'i'}}, {'email': {$regex: ?0, $options: 'i'}}], 'isActive': true}")
    List<User> searchUsers(String searchTerm);
    
    @Query("{'$or': [{'firstName': {$regex: ?0, $options: 'i'}}, {'lastName': {$regex: ?0, $options: 'i'}}, {'email': {$regex: ?0, $options: 'i'}}], 'isActive': true}")
    Page<User> searchUsers(String searchTerm, Pageable pageable);
    
    // Address queries
    @Query("{'addresses.city': ?0, 'isActive': true}")
    List<User> findByAddressesCityAndIsActiveTrue(String city);
    
    @Query("{'addresses.state': ?0, 'isActive': true}")
    List<User> findByAddressesStateAndIsActiveTrue(String state);
    
    @Query("{'addresses.country': ?0, 'isActive': true}")
    List<User> findByAddressesCountryAndIsActiveTrue(String country);
    
    // Statistics queries
    @Query(value = "{}", count = true)
    long countByIsActiveTrue();
    
    @Query(value = "{'roles': ?0, 'isActive': true}", count = true)
    long countByRoleAndIsActiveTrue(User.Role role);
    
    @Query(value = "{'provider': ?0, 'isActive': true}", count = true)
    long countByProviderAndIsActiveTrue(User.AuthProvider provider);
    
    // Recent users
    @Query("{'isActive': true}")
    List<User> findTop10ByIsActiveTrueOrderByCreatedAtDesc();
    
    // Users with default address
    @Query("{'addresses.isDefault': true, 'isActive': true}")
    List<User> findByAddressesIsDefaultTrueAndIsActiveTrue();
}