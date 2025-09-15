package com.ecommerce.repository;

import com.ecommerce.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    
    // Basic queries
    @Query("{'userId': ?0}")
    Page<Order> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    @Query("{'userId': ?0}")
    List<Order> findAllByUserIdOrderByCreatedAtDesc(String userId);
    
    // Order number queries
    @Query("{'orderNumber': ?0}")
    Optional<Order> findByOrderNumber(String orderNumber);
    
    // Status-based queries
    @Query("{'status': ?0}")
    Page<Order> findByStatusOrderByCreatedAtDesc(Order.OrderStatus status, Pageable pageable);
    
    @Query("{'status': ?0}")
    List<Order> findAllByStatusOrderByCreatedAtDesc(Order.OrderStatus status);
    
    @Query("{'userId': ?0, 'status': ?1}")
    List<Order> findByUserIdAndStatus(String userId, Order.OrderStatus status);
    
    @Query("{'userId': ?0, 'status': ?1}")
    Page<Order> findByUserIdAndStatus(String userId, Order.OrderStatus status, Pageable pageable);
    
    // Date range queries
    @Query("{'createdAt': {$gte: ?0, $lte: ?1}}")
    Page<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    @Query("{'createdAt': {$gte: ?0, $lte: ?1}}")
    List<Order> findAllByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("{'userId': ?0, 'createdAt': {$gte: ?1, $lte: ?2}}")
    Page<Order> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    @Query("{'status': ?0, 'createdAt': {$gte: ?1, $lte: ?2}}")
    Page<Order> findByStatusAndCreatedAtBetween(Order.OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Amount-based queries
    @Query("{'totalAmount': {$gte: ?0, $lte: ?1}}")
    Page<Order> findByTotalAmountBetween(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);
    
    @Query("{'totalAmount': {$gte: ?0}}")
    Page<Order> findByTotalAmountGreaterThanEqual(BigDecimal minAmount, Pageable pageable);
    
    @Query("{'userId': ?0, 'totalAmount': {$gte: ?1}}")
    List<Order> findByUserIdAndTotalAmountGreaterThanEqual(String userId, BigDecimal minAmount);
    
    // Payment-related queries
    @Query("{'paymentInfo.paymentMethod': ?0}")
    Page<Order> findByPaymentMethod(Order.PaymentMethod paymentMethod, Pageable pageable);
    
    @Query("{'paymentInfo.paymentStatus': ?0}")
    Page<Order> findByPaymentStatus(Order.PaymentStatus paymentStatus, Pageable pageable);
    
    @Query("{'paymentInfo.paymentMethod': ?0, 'paymentInfo.paymentStatus': ?1}")
    Page<Order> findByPaymentMethodAndPaymentStatus(Order.PaymentMethod paymentMethod, Order.PaymentStatus paymentStatus, Pageable pageable);
    
    // Shipping-related queries
    @Query("{'shippingAddress.city': ?0}")
    List<Order> findByShippingAddressCity(String city);
    
    @Query("{'shippingAddress.state': ?0}")
    List<Order> findByShippingAddressState(String state);
    
    @Query("{'shippingAddress.country': ?0}")
    List<Order> findByShippingAddressCountry(String country);
    
    // Tracking queries
    @Query("{'trackingNumber': {$exists: true, $ne: null}}")
    List<Order> findOrdersWithTracking();
    
    @Query("{'trackingNumber': ?0}")
    Optional<Order> findByTrackingNumber(String trackingNumber);
    
    // Recent orders
    @Query("{}")
    List<Order> findTop10ByOrderByCreatedAtDesc();
    
    @Query("{'userId': ?0}")
    List<Order> findTop5ByUserIdOrderByCreatedAtDesc(String userId);
    
    // Analytics queries
    @Query(value = "{}", count = true)
    long countAllOrders();
    
    @Query(value = "{'status': ?0}", count = true)
    long countByStatus(Order.OrderStatus status);
    
    @Query(value = "{'userId': ?0}", count = true)
    long countByUserId(String userId);
    
    @Query(value = "{'createdAt': {$gte: ?0, $lte: ?1}}", count = true)
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query(value = "{'userId': ?0, 'createdAt': {$gte: ?1, $lte: ?2}}", count = true)
    long countByUserIdAndCreatedAtBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Revenue analytics
    @Query(value = "{'status': {$in: ['DELIVERED', 'SHIPPED']}, 'createdAt': {$gte: ?0, $lte: ?1}}", fields = "{'totalAmount': 1}")
    List<Order> findRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query(value = "{'userId': ?0, 'status': {$in: ['DELIVERED', 'SHIPPED']}}", fields = "{'totalAmount': 1}")
    List<Order> findUserRevenue(String userId);
    
    // Product-based queries
    @Query("{'items.productId': ?0}")
    List<Order> findByProductId(String productId);
    
    @Query("{'items.productId': ?0, 'status': ?1}")
    List<Order> findByProductIdAndStatus(String productId, Order.OrderStatus status);
    
    // Seller-based queries
    @Query("{'items.sellerId': ?0}")
    List<Order> findBySellerId(String sellerId);
    
    @Query("{'items.sellerId': ?0, 'status': ?1}")
    List<Order> findBySellerIdAndStatus(String sellerId, Order.OrderStatus status);
    
    // Cancelled orders
    @Query("{'status': 'CANCELLED'}")
    List<Order> findCancelledOrders();
    
    @Query("{'status': 'CANCELLED', 'createdAt': {$gte: ?0, $lte: ?1}}")
    List<Order> findCancelledOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    // Refunded orders
    @Query("{'status': 'REFUNDED'}")
    List<Order> findRefundedOrders();
    
    @Query("{'paymentInfo.refundAmount': {$gt: 0}}")
    List<Order> findOrdersWithRefunds();
    
    // High-value orders
    @Query("{'totalAmount': {$gte: ?0}}")
    List<Order> findHighValueOrders(BigDecimal threshold);
    
    // Orders by delivery status
    @Query("{'deliveredAt': {$exists: true, $ne: null}}")
    List<Order> findDeliveredOrders();
    
    @Query("{'shippedAt': {$exists: true, $ne: null}, 'deliveredAt': {$exists: false}}")
    List<Order> findShippedButNotDeliveredOrders();
    
    // Orders with notes
    @Query("{'notes': {$exists: true, $ne: null, $ne: ''}}")
    List<Order> findOrdersWithNotes();
    
    // Bulk operations
    @Query("{'status': 'PENDING', 'createdAt': {$lt: ?0}}")
    List<Order> findOldPendingOrders(LocalDateTime cutoffDate);
    
    @Query("{'status': 'SHIPPED', 'shippedAt': {$lt: ?0}}")
    List<Order> findOverdueShippedOrders(LocalDateTime cutoffDate);
}