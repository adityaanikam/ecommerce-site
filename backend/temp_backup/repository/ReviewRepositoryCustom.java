package com.ecommerce.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ReviewRepositoryCustom {
    
    // Review analytics
    Map<String, Object> getReviewAnalytics(String productId);
    
    // Rating calculations
    Map<String, Object> calculateProductRating(String productId);
    
    // Review sentiment analysis
    Map<String, Object> getReviewSentimentAnalysis(String productId);
    
    // Review trends
    List<Map<String, Object>> getReviewTrends(String productId, int months);
    
    // Most helpful reviews
    List<Map<String, Object>> getMostHelpfulReviews(String productId, int limit);
    
    // Review moderation analytics
    Map<String, Object> getReviewModerationAnalytics();
    
    // User review behavior
    Map<String, Object> getUserReviewBehavior(String userId);
    
    // Product comparison by reviews
    List<Map<String, Object>> compareProductsByReviews(List<String> productIds);
    
    // Review quality metrics
    Map<String, Object> getReviewQualityMetrics(String productId);
    
    // Review response time analytics
    Map<String, Object> getReviewResponseTimeAnalytics();
    
    // Review fraud detection data
    List<Map<String, Object>> getPotentialFraudulentReviews();
    
    // Review impact on sales
    Map<String, Object> getReviewImpactOnSales(String productId);
    
    // Top reviewers
    List<Map<String, Object>> getTopReviewers(int limit);
    
    // Review distribution by rating
    Map<String, Long> getReviewDistributionByRating(String productId);
    
    // Review velocity
    Map<String, Object> getReviewVelocity(String productId);
    
    // Review completeness score
    Map<String, Object> getReviewCompletenessScore(String productId);
}
