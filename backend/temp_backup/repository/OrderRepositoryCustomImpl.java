package com.ecommerce.repository;

import com.ecommerce.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Repository
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Override
    public Map<String, Object> getOrderAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("createdAt").gte(startDate).lte(endDate)),
            group()
                .count().as("totalOrders")
                .sum("totalAmount").as("totalRevenue")
                .avg("totalAmount").as("averageOrderValue")
                .min("totalAmount").as("minOrderValue")
                .max("totalAmount").as("maxOrderValue")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "orders", Map.class);
        return results.getUniqueMappedResult();
    }
    
    @Override
    public Map<String, Object> getRevenueAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("createdAt").gte(startDate).lte(endDate)
                .and("status").in("DELIVERED", "SHIPPED")),
            group()
                .sum("totalAmount").as("totalRevenue")
                .sum("subtotal").as("totalSubtotal")
                .sum("taxAmount").as("totalTax")
                .sum("shippingAmount").as("totalShipping")
                .sum("discountAmount").as("totalDiscounts")
                .count().as("completedOrders")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "orders", Map.class);
        return results.getUniqueMappedResult();
    }
    
    @Override
    public Map<String, Object> getUserOrderAnalytics(String userId) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("userId").is(userId)),
            group()
                .count().as("totalOrders")
                .sum("totalAmount").as("totalSpent")
                .avg("totalAmount").as("averageOrderValue")
                .min("createdAt").as("firstOrderDate")
                .max("createdAt").as("lastOrderDate")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "orders", Map.class);
        return results.getUniqueMappedResult();
    }
    
    @Override
    public Map<String, Object> getProductSalesAnalytics(String productId, LocalDateTime startDate, LocalDateTime endDate) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("createdAt").gte(startDate).lte(endDate)
                .and("items.productId").is(productId)),
            unwind("items"),
            match(Criteria.where("items.productId").is(productId)),
            group()
                .sum("items.quantity").as("totalQuantitySold")
                .sum("items.subtotal").as("totalRevenue")
                .count().as("totalOrders")
                .avg("items.price").as("averagePrice")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "orders", Map.class);
        return results.getUniqueMappedResult();
    }
    
    @Override
    public Map<String, Object> getSellerPerformanceAnalytics(String sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("createdAt").gte(startDate).lte(endDate)
                .and("items.sellerId").is(sellerId)),
            unwind("items"),
            match(Criteria.where("items.sellerId").is(sellerId)),
            group()
                .sum("items.quantity").as("totalItemsSold")
                .sum("items.subtotal").as("totalRevenue")
                .count().as("totalOrders")
                .avg("items.price").as("averageItemPrice")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "orders", Map.class);
        return results.getUniqueMappedResult();
    }
    
    @Override
    public Map<String, Long> getOrderStatusDistribution() {
        Aggregation aggregation = newAggregation(
            group("status").count().as("count"),
            project("count").and("status").previousOperation()
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "orders", Map.class);
        return results.getMappedResults().stream()
            .collect(java.util.stream.Collectors.toMap(
                map -> (String) map.get("status"),
                map -> (Long) map.get("count")
            ));
    }
    
    @Override
    public Map<String, Long> getPaymentMethodAnalytics() {
        Aggregation aggregation = newAggregation(
            group("paymentInfo.paymentMethod").count().as("count"),
            project("count").and("paymentMethod").previousOperation()
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "orders", Map.class);
        return results.getMappedResults().stream()
            .collect(java.util.stream.Collectors.toMap(
                map -> (String) map.get("paymentMethod"),
                map -> (Long) map.get("count")
            ));
    }
    
    @Override
    public Map<String, Long> getOrdersByLocation() {
        Aggregation aggregation = newAggregation(
            group("shippingAddress.country").count().as("count"),
            project("count").and("country").previousOperation()
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "orders", Map.class);
        return results.getMappedResults().stream()
            .collect(java.util.stream.Collectors.toMap(
                map -> (String) map.get("country"),
                map -> (Long) map.get("count")
            ));
    }
    
    @Override
    public List<Map<String, Object>> getMonthlySalesTrends(int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        
        Aggregation aggregation = newAggregation(
            match(Criteria.where("createdAt").gte(startDate)
                .and("status").in("DELIVERED", "SHIPPED")),
            project()
                .and(DateOperators.Year.yearOf("createdAt")).as("year")
                .and(DateOperators.Month.monthOf("createdAt")).as("month")
                .and("totalAmount").as("amount"),
            group("year", "month")
                .sum("amount").as("totalRevenue")
                .count().as("totalOrders")
                .avg("amount").as("averageOrderValue"),
            sort(Sort.Direction.ASC, "year", "month")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "orders", Map.class);
        return results.getMappedResults();
    }
    
    @Override
    public List<Map<String, Object>> getTopSellingProducts(int limit, LocalDateTime startDate, LocalDateTime endDate) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("createdAt").gte(startDate).lte(endDate)
                .and("status").in("DELIVERED", "SHIPPED")),
            unwind("items"),
            group("items.productId", "items.productName")
                .sum("items.quantity").as("totalQuantitySold")
                .sum("items.subtotal").as("totalRevenue")
                .count().as("totalOrders"),
            sort(Sort.Direction.DESC, "totalQuantitySold"),
            limit(limit)
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "orders", Map.class);
        return results.getMappedResults();
    }
    
    @Override
    public Map<String, Object> getCustomerLifetimeValue(String userId) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("userId").is(userId)
                .and("status").in("DELIVERED", "SHIPPED")),
            group()
                .sum("totalAmount").as("totalLifetimeValue")
                .count().as("totalOrders")
                .avg("totalAmount").as("averageOrderValue")
                .min("createdAt").as("firstOrderDate")
                .max("createdAt").as("lastOrderDate")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "orders", Map.class);
        return results.getUniqueMappedResult();
    }
    
    @Override
    public List<Map<String, Object>> getAverageOrderValueTrends(int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        
        Aggregation aggregation = newAggregation(
            match(Criteria.where("createdAt").gte(startDate)
                .and("status").in("DELIVERED", "SHIPPED")),
            project()
                .and(DateOperators.Year.yearOf("createdAt")).as("year")
                .and(DateOperators.Month.monthOf("createdAt")).as("month")
                .and("totalAmount").as("amount"),
            group("year", "month")
                .avg("amount").as("averageOrderValue")
                .count().as("totalOrders"),
            sort(Sort.Direction.ASC, "year", "month")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "orders", Map.class);
        return results.getMappedResults();
    }
    
    @Override
    public Map<String, Object> getOrderFulfillmentMetrics() {
        Aggregation aggregation = newAggregation(
            group()
                .count().as("totalOrders")
                .sum(ConditionalOperators.when(Criteria.where("status").is("DELIVERED"))
                    .then(1).otherwise(0)).as("deliveredOrders")
                .sum(ConditionalOperators.when(Criteria.where("status").is("SHIPPED"))
                    .then(1).otherwise(0)).as("shippedOrders")
                .sum(ConditionalOperators.when(Criteria.where("status").is("CANCELLED"))
                    .then(1).otherwise(0)).as("cancelledOrders")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "orders", Map.class);
        return results.getUniqueMappedResult();
    }
    
    @Override
    public Map<String, Object> getReturnRefundAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("createdAt").gte(startDate).lte(endDate)
                .and("status").in("REFUNDED", "RETURNED")),
            group()
                .count().as("totalReturns")
                .sum("totalAmount").as("totalRefundAmount")
                .avg("totalAmount").as("averageRefundAmount")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "orders", Map.class);
        return results.getUniqueMappedResult();
    }
    
    @Override
    public List<Map<String, Object>> getPeakOrderingTimes() {
        Aggregation aggregation = newAggregation(
            project()
                .and(DateOperators.Hour.hourOf("createdAt")).as("hour")
                .and(DateOperators.DayOfWeek.dayOfWeek("createdAt")).as("dayOfWeek"),
            group("hour", "dayOfWeek").count().as("orderCount"),
            sort(Sort.Direction.DESC, "orderCount")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "orders", Map.class);
        return results.getMappedResults();
    }
    
    @Override
    public Map<String, Object> getCartAbandonmentAnalytics() {
        // This would require integration with cart data
        // For now, returning basic metrics
        return Map.of(
            "message", "Cart abandonment analytics require cart data integration",
            "timestamp", LocalDateTime.now()
        );
    }
    
    @Override
    public List<Map<String, Object>> getCustomerSegmentation() {
        Aggregation aggregation = newAggregation(
            group("userId")
                .sum("totalAmount").as("totalSpent")
                .count().as("totalOrders")
                .avg("totalAmount").as("averageOrderValue"),
            project()
                .and("totalSpent").as("totalSpent")
                .and("totalOrders").as("totalOrders")
                .and("averageOrderValue").as("averageOrderValue")
                .and(ConditionalOperators.when(Criteria.where("totalSpent").gte(1000))
                    .then("High Value").otherwise(
                        ConditionalOperators.when(Criteria.where("totalSpent").gte(500))
                            .then("Medium Value").otherwise("Low Value")
                    )).as("segment")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "orders", Map.class);
        return results.getMappedResults();
    }
    
    @Override
    public List<Map<String, Object>> getProductRecommendationData(String userId) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("userId").is(userId)
                .and("status").in("DELIVERED", "SHIPPED")),
            unwind("items"),
            group("items.productId", "items.productName", "items.category")
                .sum("items.quantity").as("totalQuantity")
                .sum("items.subtotal").as("totalSpent"),
            sort(Sort.Direction.DESC, "totalQuantity")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "orders", Map.class);
        return results.getMappedResults();
    }
    
    @Override
    public List<Map<String, Object>> getSeasonalTrends(int years) {
        LocalDateTime startDate = LocalDateTime.now().minusYears(years);
        
        Aggregation aggregation = newAggregation(
            match(Criteria.where("createdAt").gte(startDate)
                .and("status").in("DELIVERED", "SHIPPED")),
            project()
                .and(DateOperators.Year.yearOf("createdAt")).as("year")
                .and(DateOperators.Month.monthOf("createdAt")).as("month")
                .and("totalAmount").as("amount"),
            group("year", "month")
                .sum("amount").as("totalRevenue")
                .count().as("totalOrders"),
            sort(Sort.Direction.ASC, "year", "month")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "orders", Map.class);
        return results.getMappedResults();
    }
}
