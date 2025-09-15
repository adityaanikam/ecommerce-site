package com.ecommerce.repository;

import com.ecommerce.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderRepositoryCustom {
    
    // Order analytics
    Map<String, Object> getOrderAnalytics(LocalDateTime startDate, LocalDateTime endDate);
    
    // Revenue analytics
    Map<String, Object> getRevenueAnalytics(LocalDateTime startDate, LocalDateTime endDate);
    
    // User order analytics
    Map<String, Object> getUserOrderAnalytics(String userId);
    
    // Product sales analytics
    Map<String, Object> getProductSalesAnalytics(String productId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Seller performance analytics
    Map<String, Object> getSellerPerformanceAnalytics(String sellerId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Order status distribution
    Map<String, Long> getOrderStatusDistribution();
    
    // Payment method analytics
    Map<String, Long> getPaymentMethodAnalytics();
    
    // Geographic analytics
    Map<String, Long> getOrdersByLocation();
    
    // Monthly sales trends
    List<Map<String, Object>> getMonthlySalesTrends(int months);
    
    // Top selling products
    List<Map<String, Object>> getTopSellingProducts(int limit, LocalDateTime startDate, LocalDateTime endDate);
    
    // Customer lifetime value
    Map<String, Object> getCustomerLifetimeValue(String userId);
    
    // Average order value trends
    List<Map<String, Object>> getAverageOrderValueTrends(int months);
    
    // Order fulfillment metrics
    Map<String, Object> getOrderFulfillmentMetrics();
    
    // Return and refund analytics
    Map<String, Object> getReturnRefundAnalytics(LocalDateTime startDate, LocalDateTime endDate);
    
    // Peak ordering times
    List<Map<String, Object>> getPeakOrderingTimes();
    
    // Cart abandonment analytics
    Map<String, Object> getCartAbandonmentAnalytics();
    
    // Customer segmentation
    List<Map<String, Object>> getCustomerSegmentation();
    
    // Product recommendation data
    List<Map<String, Object>> getProductRecommendationData(String userId);
    
    // Seasonal trends
    List<Map<String, Object>> getSeasonalTrends(int years);
}
