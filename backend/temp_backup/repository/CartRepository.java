package com.ecommerce.repository;

import com.ecommerce.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<Cart, String> {
    
    // Basic queries
    @Query("{'userId': ?0}")
    Optional<Cart> findByUserId(String userId);
    
    @Query("{'userId': ?0, 'isActive': true}")
    Optional<Cart> findByUserIdAndIsActiveTrue(String userId);
    
    // Active cart queries
    @Query("{'isActive': true}")
    List<Cart> findAllActiveCarts();
    
    @Query("{'isActive': true}")
    List<Cart> findAllByIsActiveTrue();
    
    // Expired cart queries
    @Query("{'expiresAt': {$lt: ?0}, 'isActive': true}")
    List<Cart> findExpiredCarts(LocalDateTime currentTime);
    
    @Query("{'expiresAt': {$lt: ?0}}")
    List<Cart> findAllExpiredCarts(LocalDateTime currentTime);
    
    // Cart with items queries
    @Query("{'userId': ?0, 'items': {$exists: true, $ne: []}}")
    Optional<Cart> findCartWithItemsByUserId(String userId);
    
    @Query("{'userId': ?0, 'items': {$size: 0}}")
    Optional<Cart> findEmptyCartByUserId(String userId);
    
    // Product-based queries
    @Query("{'items.productId': ?0, 'isActive': true}")
    List<Cart> findCartsContainingProduct(String productId);
    
    @Query("{'userId': ?0, 'items.productId': ?1}")
    Optional<Cart> findByUserIdAndProductId(String userId, String productId);
    
    // Seller-based queries
    @Query("{'items.sellerId': ?0, 'isActive': true}")
    List<Cart> findCartsBySellerId(String sellerId);
    
    @Query("{'userId': ?0, 'items.sellerId': ?1}")
    Optional<Cart> findByUserIdAndSellerId(String userId, String sellerId);
    
    // Amount-based queries
    @Query("{'totalAmount': {$gte: ?0}, 'isActive': true}")
    List<Cart> findCartsWithAmountGreaterThanEqual(double minAmount);
    
    @Query("{'totalAmount': {$gte: ?0, $lte: ?1}, 'isActive': true}")
    List<Cart> findCartsWithAmountBetween(double minAmount, double maxAmount);
    
    // Coupon-based queries
    @Query("{'couponCode': {$exists: true, $ne: null}, 'isActive': true}")
    List<Cart> findCartsWithCoupons();
    
    @Query("{'couponCode': ?0, 'isActive': true}")
    List<Cart> findByCouponCode(String couponCode);
    
    @Query("{'userId': ?0, 'couponCode': ?1}")
    Optional<Cart> findByUserIdAndCouponCode(String userId, String couponCode);
    
    // Date-based queries
    @Query("{'updatedAt': {$gte: ?0, $lte: ?1}, 'isActive': true}")
    List<Cart> findCartsUpdatedBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("{'updatedAt': {$lt: ?0}, 'isActive': true}")
    List<Cart> findCartsNotUpdatedSince(LocalDateTime cutoffDate);
    
    // Statistics queries
    @Query(value = "{'isActive': true}", count = true)
    long countActiveCarts();
    
    @Query(value = "{'items': {$exists: true, $ne: []}, 'isActive': true}", count = true)
    long countCartsWithItems();
    
    @Query(value = "{'items': {$size: 0}, 'isActive': true}", count = true)
    long countEmptyCarts();
    
    @Query(value = "{'couponCode': {$exists: true, $ne: null}, 'isActive': true}", count = true)
    long countCartsWithCoupons();
    
    // Recent cart activity
    @Query("{'isActive': true}")
    List<Cart> findTop10ByIsActiveTrueOrderByUpdatedAtDesc();
    
    @Query("{'userId': ?0}")
    List<Cart> findTop5ByUserIdOrderByUpdatedAtDesc(String userId);
    
    // Cart cleanup queries
    @Query("{'isActive': true, 'updatedAt': {$lt: ?0}}")
    List<Cart> findInactiveCarts(LocalDateTime cutoffDate);
    
    @Query("{'expiresAt': {$lt: ?0}, 'isActive': true}")
    List<Cart> findExpiredActiveCarts(LocalDateTime currentTime);
    
    // Cart analytics
    @Query(value = "{'isActive': true}", fields = "{'totalAmount': 1, 'totalItems': 1}")
    List<Cart> findCartAnalytics();
    
    @Query(value = "{'userId': ?0, 'isActive': true}", fields = "{'totalAmount': 1, 'totalItems': 1}")
    List<Cart> findUserCartAnalytics(String userId);
    
    // Product popularity in carts
    @Query("{'items.productId': ?0, 'isActive': true}")
    List<Cart> findCartsContainingProductActive(String productId);
    
    // Category-based cart queries
    @Query("{'items.category': ?0, 'isActive': true}")
    List<Cart> findCartsByCategory(String category);
    
    @Query("{'userId': ?0, 'items.category': ?1}")
    Optional<Cart> findByUserIdAndCategory(String userId, String category);
    
    // Brand-based cart queries
    @Query("{'items.brand': ?0, 'isActive': true}")
    List<Cart> findCartsByBrand(String brand);
    
    @Query("{'userId': ?0, 'items.brand': ?1}")
    Optional<Cart> findByUserIdAndBrand(String userId, String brand);
    
    // Cart size queries
    @Query("{'totalItems': {$gte: ?0}, 'isActive': true}")
    List<Cart> findCartsWithItemCountGreaterThanEqual(int minItems);
    
    @Query("{'totalItems': {$gte: ?0, $lte: ?1}, 'isActive': true}")
    List<Cart> findCartsWithItemCountBetween(int minItems, int maxItems);
    
    // Abandoned cart queries
    @Query("{'updatedAt': {$lt: ?0}, 'isActive': true, 'items': {$exists: true, $ne: []}}")
    List<Cart> findAbandonedCarts(LocalDateTime cutoffDate);
    
    // High-value cart queries
    @Query("{'totalAmount': {$gte: ?0}, 'isActive': true}")
    List<Cart> findHighValueCarts(double threshold);
    
    // Cart with specific product variant
    @Query("{'items.productId': ?0, 'items.variant': ?1, 'isActive': true}")
    List<Cart> findCartsByProductAndVariant(String productId, String variant);
    
    // Cart with specific product size
    @Query("{'items.productId': ?0, 'items.size': ?1, 'isActive': true}")
    List<Cart> findCartsByProductAndSize(String productId, String size);
    
    // Cart with specific product color
    @Query("{'items.productId': ?0, 'items.color': ?1, 'isActive': true}")
    List<Cart> findCartsByProductAndColor(String productId, String color);
}