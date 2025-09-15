package com.ecommerce.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Repository
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Override
    public Map<String, Object> getReviewAnalytics(String productId) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("productId").is(productId).and("isApproved").is(true)),
            group()
                .count().as("totalReviews")
                .avg("rating").as("averageRating")
                .min("rating").as("minRating")
                .max("rating").as("maxRating")
                .sum("helpfulCount").as("totalHelpfulVotes")
                .sum("notHelpfulCount").as("totalNotHelpfulVotes")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "reviews", Map.class);
        return results.getUniqueMappedResult();
    }
    
    @Override
    public Map<String, Object> calculateProductRating(String productId) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("productId").is(productId).and("isApproved").is(true)),
            group()
                .avg("rating").as("averageRating")
                .count().as("totalReviews")
                .sum(ConditionalOperators.when(Criteria.where("rating").is(5))
                    .then(1).otherwise(0)).as("fiveStarReviews")
                .sum(ConditionalOperators.when(Criteria.where("rating").is(4))
                    .then(1).otherwise(0)).as("fourStarReviews")
                .sum(ConditionalOperators.when(Criteria.where("rating").is(3))
                    .then(1).otherwise(0)).as("threeStarReviews")
                .sum(ConditionalOperators.when(Criteria.where("rating").is(2))
                    .then(1).otherwise(0)).as("twoStarReviews")
                .sum(ConditionalOperators.when(Criteria.where("rating").is(1))
                    .then(1).otherwise(0)).as("oneStarReviews")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "reviews", Map.class);
        return results.getUniqueMappedResult();
    }
    
    @Override
    public Map<String, Object> getReviewSentimentAnalysis(String productId) {
        // Basic sentiment analysis based on rating distribution
        Aggregation aggregation = newAggregation(
            match(Criteria.where("productId").is(productId).and("isApproved").is(true)),
            group()
                .sum(ConditionalOperators.when(Criteria.where("rating").gte(4))
                    .then(1).otherwise(0)).as("positiveReviews")
                .sum(ConditionalOperators.when(Criteria.where("rating").is(3))
                    .then(1).otherwise(0)).as("neutralReviews")
                .sum(ConditionalOperators.when(Criteria.where("rating").lte(2))
                    .then(1).otherwise(0)).as("negativeReviews")
                .count().as("totalReviews")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "reviews", Map.class);
        return results.getUniqueMappedResult();
    }
    
    @Override
    public List<Map<String, Object>> getReviewTrends(String productId, int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        
        Aggregation aggregation = newAggregation(
            match(Criteria.where("productId").is(productId)
                .and("createdAt").gte(startDate)
                .and("isApproved").is(true)),
            project()
                .and(DateOperators.Year.yearOf("createdAt")).as("year")
                .and(DateOperators.Month.monthOf("createdAt")).as("month")
                .and("rating").as("rating"),
            group("year", "month")
                .count().as("reviewCount")
                .avg("rating").as("averageRating")
                .sum(ConditionalOperators.when(Criteria.where("rating").gte(4))
                    .then(1).otherwise(0)).as("positiveReviews"),
            sort(Sort.Direction.ASC, "year", "month")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "reviews", Map.class);
        return results.getMappedResults();
    }
    
    @Override
    public List<Map<String, Object>> getMostHelpfulReviews(String productId, int limit) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("productId").is(productId).and("isApproved").is(true)),
            project()
                .and("_id").as("reviewId")
                .and("userId").as("userId")
                .and("userName").as("userName")
                .and("rating").as("rating")
                .and("comment").as("comment")
                .and("helpfulCount").as("helpfulCount")
                .and("createdAt").as("createdAt")
                .and(ArithmeticOperators.Subtract.valueOf("helpfulCount").subtract("notHelpfulCount"))
                .as("helpfulnessScore"),
            sort(Sort.Direction.DESC, "helpfulnessScore", "helpfulCount"),
            limit(limit)
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "reviews", Map.class);
        return results.getMappedResults();
    }
    
    @Override
    public Map<String, Object> getReviewModerationAnalytics() {
        Aggregation aggregation = newAggregation(
            group("moderationStatus")
                .count().as("count"),
            group()
                .sum("count").as("totalReviews")
                .sum(ConditionalOperators.when(Criteria.where("moderationStatus").is("APPROVED"))
                    .then("count").otherwise(0)).as("approvedReviews")
                .sum(ConditionalOperators.when(Criteria.where("moderationStatus").is("PENDING"))
                    .then("count").otherwise(0)).as("pendingReviews")
                .sum(ConditionalOperators.when(Criteria.where("moderationStatus").is("REJECTED"))
                    .then("count").otherwise(0)).as("rejectedReviews")
                .sum(ConditionalOperators.when(Criteria.where("moderationStatus").is("FLAGGED"))
                    .then("count").otherwise(0)).as("flaggedReviews")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "reviews", Map.class);
        return results.getUniqueMappedResult();
    }
    
    @Override
    public Map<String, Object> getUserReviewBehavior(String userId) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("userId").is(userId)),
            group()
                .count().as("totalReviews")
                .avg("rating").as("averageRatingGiven")
                .sum("helpfulCount").as("totalHelpfulVotesReceived")
                .min("createdAt").as("firstReviewDate")
                .max("createdAt").as("lastReviewDate")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "reviews", Map.class);
        return results.getUniqueMappedResult();
    }
    
    @Override
    public List<Map<String, Object>> compareProductsByReviews(List<String> productIds) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("productId").in(productIds).and("isApproved").is(true)),
            group("productId")
                .count().as("totalReviews")
                .avg("rating").as("averageRating")
                .sum("helpfulCount").as("totalHelpfulVotes")
                .sum(ConditionalOperators.when(Criteria.where("rating").gte(4))
                    .then(1).otherwise(0)).as("positiveReviews"),
            sort(Sort.Direction.DESC, "averageRating", "totalReviews")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "reviews", Map.class);
        return results.getMappedResults();
    }
    
    @Override
    public Map<String, Object> getReviewQualityMetrics(String productId) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("productId").is(productId).and("isApproved").is(true)),
            group()
                .avg(ArithmeticOperators.Add.valueOf("helpfulCount").add("notHelpfulCount"))
                .as("averageEngagement")
                .sum(ConditionalOperators.when(Criteria.where("comment").exists(true)
                    .and("comment").ne("").and("comment").ne(null))
                    .then(1).otherwise(0)).as("reviewsWithComments")
                .sum(ConditionalOperators.when(Criteria.where("images").exists(true)
                    .and("images").ne(new String[0]))
                    .then(1).otherwise(0)).as("reviewsWithImages")
                .count().as("totalReviews")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "reviews", Map.class);
        return results.getUniqueMappedResult();
    }
    
    @Override
    public Map<String, Object> getReviewResponseTimeAnalytics() {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("sellerResponse").exists(true)
                .and("sellerResponse").ne(null)
                .and("sellerResponseDate").exists(true)),
            project()
                .and(ArithmeticOperators.Subtract.valueOf("sellerResponseDate").subtract("createdAt"))
                .as("responseTimeInDays"),
            group()
                .avg("responseTimeInDays").as("averageResponseTime")
                .min("responseTimeInDays").as("minResponseTime")
                .max("responseTimeInDays").as("maxResponseTime")
                .count().as("totalResponses")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "reviews", Map.class);
        return results.getUniqueMappedResult();
    }
    
    @Override
    public List<Map<String, Object>> getPotentialFraudulentReviews() {
        // Simple fraud detection based on multiple factors
        Aggregation aggregation = newAggregation(
            match(Criteria.where("isApproved").is(true)),
            group("userId")
                .count().as("reviewCount")
                .avg("rating").as("averageRating")
                .sum(ConditionalOperators.when(Criteria.where("isVerified").is(true))
                    .then(1).otherwise(0)).as("verifiedReviews"),
            match(Criteria.where("reviewCount").gte(10)
                .and("averageRating").gte(4.5)
                .and("verifiedReviews").lte(2)),
            sort(Sort.Direction.DESC, "reviewCount")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "reviews", Map.class);
        return results.getMappedResults();
    }
    
    @Override
    public Map<String, Object> getReviewImpactOnSales(String productId) {
        // This would require integration with order data
        // For now, returning basic review metrics
        return getReviewAnalytics(productId);
    }
    
    @Override
    public List<Map<String, Object>> getTopReviewers(int limit) {
        Aggregation aggregation = newAggregation(
            group("userId", "userName")
                .count().as("totalReviews")
                .sum("helpfulCount").as("totalHelpfulVotes")
                .avg("rating").as("averageRatingGiven"),
            project()
                .and("totalReviews").as("totalReviews")
                .and("totalHelpfulVotes").as("totalHelpfulVotes")
                .and("averageRatingGiven").as("averageRatingGiven")
                .and(ArithmeticOperators.Divide.valueOf("totalHelpfulVotes").divideBy("totalReviews"))
                .as("helpfulnessRatio"),
            sort(Sort.Direction.DESC, "helpfulnessRatio", "totalReviews"),
            limit(limit)
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "reviews", Map.class);
        return results.getMappedResults();
    }
    
    @Override
    public Map<String, Long> getReviewDistributionByRating(String productId) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("productId").is(productId).and("isApproved").is(true)),
            group("rating").count().as("count"),
            project("count").and("rating").previousOperation()
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "reviews", Map.class);
        return results.getMappedResults().stream()
            .collect(java.util.stream.Collectors.toMap(
                map -> String.valueOf(map.get("rating")),
                map -> (Long) map.get("count")
            ));
    }
    
    @Override
    public Map<String, Object> getReviewVelocity(String productId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        
        Aggregation aggregation = newAggregation(
            match(Criteria.where("productId").is(productId).and("isApproved").is(true)),
            group()
                .sum(ConditionalOperators.when(Criteria.where("createdAt").gte(sevenDaysAgo))
                    .then(1).otherwise(0)).as("reviewsLast7Days")
                .sum(ConditionalOperators.when(Criteria.where("createdAt").gte(thirtyDaysAgo))
                    .then(1).otherwise(0)).as("reviewsLast30Days")
                .count().as("totalReviews")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "reviews", Map.class);
        return results.getUniqueMappedResult();
    }
    
    @Override
    public Map<String, Object> getReviewCompletenessScore(String productId) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("productId").is(productId).and("isApproved").is(true)),
            group()
                .count().as("totalReviews")
                .sum(ConditionalOperators.when(Criteria.where("comment").exists(true)
                    .and("comment").ne("").and("comment").ne(null))
                    .then(1).otherwise(0)).as("reviewsWithComments")
                .sum(ConditionalOperators.when(Criteria.where("images").exists(true)
                    .and("images").ne(new String[0]))
                    .then(1).otherwise(0)).as("reviewsWithImages")
                .sum(ConditionalOperators.when(Criteria.where("title").exists(true)
                    .and("title").ne("").and("title").ne(null))
                    .then(1).otherwise(0)).as("reviewsWithTitles")
                .sum(ConditionalOperators.when(Criteria.where("pros").exists(true)
                    .and("pros").ne(new String[0]))
                    .then(1).otherwise(0)).as("reviewsWithPros")
                .sum(ConditionalOperators.when(Criteria.where("cons").exists(true)
                    .and("cons").ne(new String[0]))
                    .then(1).otherwise(0)).as("reviewsWithCons")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "reviews", Map.class);
        return results.getUniqueMappedResult();
    }
}
