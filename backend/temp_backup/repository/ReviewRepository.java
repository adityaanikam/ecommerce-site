package com.ecommerce.repository;

import com.ecommerce.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    
    // Basic queries
    @Query("{'productId': ?0}")
    Page<Review> findByProductIdOrderByCreatedAtDesc(String productId, Pageable pageable);
    
    @Query("{'productId': ?0}")
    List<Review> findAllByProductIdOrderByCreatedAtDesc(String productId);
    
    @Query("{'userId': ?0}")
    Page<Review> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    @Query("{'userId': ?0}")
    List<Review> findAllByUserIdOrderByCreatedAtDesc(String userId);
    
    // Rating-based queries
    @Query("{'productId': ?0, 'rating': ?1}")
    List<Review> findByProductIdAndRating(String productId, Integer rating);
    
    @Query("{'productId': ?0, 'rating': {$gte: ?1}}")
    List<Review> findByProductIdAndRatingGreaterThanEqual(String productId, Integer minRating);
    
    @Query("{'productId': ?0, 'rating': {$lte: ?1}}")
    List<Review> findByProductIdAndRatingLessThanEqual(String productId, Integer maxRating);
    
    @Query("{'productId': ?0, 'rating': {$gte: ?1, $lte: ?2}}")
    List<Review> findByProductIdAndRatingBetween(String productId, Integer minRating, Integer maxRating);
    
    // User and product queries
    @Query("{'userId': ?0, 'productId': ?1}")
    Optional<Review> findByUserIdAndProductId(String userId, String productId);
    
    @Query("{'userId': ?0, 'productId': ?1, 'isApproved': true}")
    Optional<Review> findByUserIdAndProductIdAndIsApprovedTrue(String userId, String productId);
    
    // Approval and moderation queries
    @Query("{'isApproved': true}")
    Page<Review> findByIsApprovedTrue(Pageable pageable);
    
    @Query("{'isApproved': false}")
    Page<Review> findByIsApprovedFalse(Pageable pageable);
    
    @Query("{'moderationStatus': ?0}")
    Page<Review> findByModerationStatus(Review.ModerationStatus status, Pageable pageable);
    
    @Query("{'moderationStatus': 'PENDING'}")
    List<Review> findPendingModerationReviews();
    
    @Query("{'moderationStatus': 'FLAGGED'}")
    List<Review> findFlaggedReviews();
    
    // Verified reviews
    @Query("{'isVerified': true}")
    Page<Review> findByIsVerifiedTrue(Pageable pageable);
    
    @Query("{'productId': ?0, 'isVerified': true}")
    List<Review> findByProductIdAndIsVerifiedTrue(String productId);
    
    @Query("{'productId': ?0, 'isVerified': true}")
    Page<Review> findByProductIdAndIsVerifiedTrue(String productId, Pageable pageable);
    
    // Helpful reviews
    @Query("{'helpfulCount': {$gte: ?0}}")
    List<Review> findByHelpfulCountGreaterThanEqual(Integer minHelpful);
    
    @Query("{'productId': ?0, 'helpfulCount': {$gte: ?1}}")
    List<Review> findByProductIdAndHelpfulCountGreaterThanEqual(String productId, Integer minHelpful);
    
    // Reviews with images
    @Query("{'images': {$exists: true, $ne: []}}")
    List<Review> findReviewsWithImages();
    
    @Query("{'productId': ?0, 'images': {$exists: true, $ne: []}}")
    List<Review> findByProductIdAndImagesExists(String productId);
    
    // Date-based queries
    @Query("{'createdAt': {$gte: ?0, $lte: ?1}}")
    List<Review> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("{'productId': ?0, 'createdAt': {$gte: ?1, $lte: ?2}}")
    List<Review> findByProductIdAndCreatedAtBetween(String productId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("{'userId': ?0, 'createdAt': {$gte: ?1, $lte: ?2}}")
    List<Review> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Recent reviews
    @Query("{}")
    List<Review> findTop10ByOrderByCreatedAtDesc();
    
    @Query("{'productId': ?0}")
    List<Review> findTop5ByProductIdOrderByCreatedAtDesc(String productId);
    
    @Query("{'isApproved': true}")
    List<Review> findTop10ByIsApprovedTrueOrderByCreatedAtDesc();
    
    // Order-based reviews
    @Query("{'orderId': ?0}")
    List<Review> findByOrderId(String orderId);
    
    @Query("{'orderId': {$exists: true, $ne: null}}")
    List<Review> findReviewsWithOrderId();
    
    // Product variant reviews
    @Query("{'productId': ?0, 'variant': ?1}")
    List<Review> findByProductIdAndVariant(String productId, String variant);
    
    @Query("{'productId': ?0, 'size': ?1}")
    List<Review> findByProductIdAndSize(String productId, String size);
    
    @Query("{'productId': ?0, 'color': ?1}")
    List<Review> findByProductIdAndColor(String productId, String color);
    
    // Seller response queries
    @Query("{'sellerResponse': {$exists: true, $ne: null}}")
    List<Review> findReviewsWithSellerResponse();
    
    @Query("{'productId': ?0, 'sellerResponse': {$exists: true, $ne: null}}")
    List<Review> findByProductIdAndSellerResponseExists(String productId);
    
    // Reported reviews
    @Query("{'reportedCount': {$gt: 0}}")
    List<Review> findReportedReviews();
    
    @Query("{'reportedCount': {$gte: ?0}}")
    List<Review> findByReportedCountGreaterThanEqual(Integer minReports);
    
    // Statistics queries
    @Query(value = "{'productId': ?0}", count = true)
    long countByProductId(String productId);
    
    @Query(value = "{'productId': ?0, 'rating': ?1}", count = true)
    long countByProductIdAndRating(String productId, Integer rating);
    
    @Query(value = "{'productId': ?0, 'isApproved': true}", count = true)
    long countByProductIdAndIsApprovedTrue(String productId);
    
    @Query(value = "{'productId': ?0, 'isVerified': true}", count = true)
    long countByProductIdAndIsVerifiedTrue(String productId);
    
    @Query(value = "{'userId': ?0}", count = true)
    long countByUserId(String userId);
    
    @Query(value = "{'moderationStatus': ?0}", count = true)
    long countByModerationStatus(Review.ModerationStatus status);
    
    // Average rating queries
    @Query(value = "{'productId': ?0, 'isApproved': true}", fields = "{'rating': 1}")
    List<Review> findRatingsByProductId(String productId);
    
    @Query(value = "{'productId': ?0, 'isApproved': true, 'isVerified': true}", fields = "{'rating': 1}")
    List<Review> findVerifiedRatingsByProductId(String productId);
    
    // Review analytics
    @Query(value = "{'createdAt': {$gte: ?0, $lte: ?1}}", fields = "{'rating': 1, 'productId': 1}")
    List<Review> findReviewAnalyticsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query(value = "{'productId': ?0, 'isApproved': true}", fields = "{'rating': 1, 'helpfulCount': 1}")
    List<Review> findProductReviewAnalytics(String productId);
    
    // Top helpful reviews
    @Query("{'productId': ?0, 'isApproved': true}")
    List<Review> findTop5ByProductIdAndIsApprovedTrueOrderByHelpfulCountDesc(String productId);
    
    // Reviews by title
    @Query("{'title': {$regex: ?0, $options: 'i'}}")
    List<Review> findByTitleContainingIgnoreCase(String title);
    
    // Reviews with pros/cons
    @Query("{'pros': {$exists: true, $ne: []}}")
    List<Review> findReviewsWithPros();
    
    @Query("{'cons': {$exists: true, $ne: []}}")
    List<Review> findReviewsWithCons();
    
    // Moderated reviews
    @Query("{'moderatedBy': ?0}")
    List<Review> findByModeratedBy(String moderatorId);
    
    @Query("{'moderatedAt': {$gte: ?0, $lte: ?1}}")
    List<Review> findByModeratedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
