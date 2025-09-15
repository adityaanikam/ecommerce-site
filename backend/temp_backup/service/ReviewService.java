package com.ecommerce.service;

import com.ecommerce.dto.ReviewDto;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.ValidationException;
import com.ecommerce.model.Review;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.model.Order;
import com.ecommerce.repository.ReviewRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ReviewRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final ReviewRepositoryCustom reviewRepositoryCustom;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CacheService cacheService;
    
    // Review Management
    
    public ReviewDto.ReviewResponse createReview(String userId, ReviewDto.CreateReviewRequest request) {
        log.info("Creating review for product ID: {} by user ID: {}", request.getProductId(), userId);
        
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Validate product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));
        
        // Check if user has already reviewed this product
        if (reviewRepository.existsByProductIdAndUserId(request.getProductId(), userId)) {
            throw new ValidationException("User has already reviewed this product");
        }
        
        // Check if user has purchased this product (optional validation)
        boolean hasPurchased = orderRepository.existsByUserIdAndItemsProductIdAndStatus(
                userId, request.getProductId(), Order.OrderStatus.COMPLETED);
        
        if (!hasPurchased) {
            log.warn("User {} attempting to review product {} without purchase", userId, request.getProductId());
            // You might want to make this a warning instead of an error
            // throw new ValidationException("User must purchase the product before reviewing");
        }
        
        // Validate rating
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new ValidationException("Rating must be between 1 and 5");
        }
        
        Review review = Review.builder()
                .productId(request.getProductId())
                .userId(userId)
                .rating(request.getRating())
                .comment(request.getComment())
                .images(request.getImages())
                .isModerated(false)
                .isApproved(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        Review savedReview = reviewRepository.save(review);
        
        // Update product ratings
        updateProductRatings(request.getProductId());
        
        // Clear cache
        cacheService.evictProductCache(request.getProductId());
        cacheService.evictReviewCache(savedReview.getId());
        
        log.info("Review created successfully with ID: {}", savedReview.getId());
        return mapToReviewResponse(savedReview);
    }
    
    public ReviewDto.ReviewResponse getReviewById(String reviewId) {
        log.debug("Fetching review by ID: {}", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));
        
        return mapToReviewResponse(review);
    }
    
    public Page<ReviewDto.ReviewResponse> getReviewsByProduct(String productId, Pageable pageable) {
        log.debug("Fetching reviews for product ID: {}", productId);
        
        Page<Review> reviews = reviewRepository.findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(productId, pageable);
        return reviews.map(this::mapToReviewResponse);
    }
    
    public Page<ReviewDto.ReviewResponse> getReviewsByUser(String userId, Pageable pageable) {
        log.debug("Fetching reviews by user ID: {}", userId);
        
        Page<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return reviews.map(this::mapToReviewResponse);
    }
    
    public Page<ReviewDto.ReviewResponse> getAllReviews(Pageable pageable) {
        log.debug("Fetching all reviews with pagination");
        
        Page<Review> reviews = reviewRepository.findAll(pageable);
        return reviews.map(this::mapToReviewResponse);
    }
    
    public ReviewDto.ReviewResponse updateReview(String reviewId, String userId, ReviewDto.UpdateReviewRequest request) {
        log.info("Updating review ID: {} by user ID: {}", reviewId, userId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));
        
        // Check if user owns the review
        if (!review.getUserId().equals(userId)) {
            throw new ValidationException("User can only update their own reviews");
        }
        
        // Check if review is already moderated and approved
        if (review.getIsModerated() && review.getIsApproved()) {
            throw new ValidationException("Cannot update approved review");
        }
        
        // Validate rating
        if (request.getRating() != null && (request.getRating() < 1 || request.getRating() > 5)) {
            throw new ValidationException("Rating must be between 1 and 5");
        }
        
        // Update fields
        if (request.getRating() != null) review.setRating(request.getRating());
        if (request.getComment() != null) review.setComment(request.getComment());
        if (request.getImages() != null) review.setImages(request.getImages());
        
        review.setUpdatedAt(LocalDateTime.now());
        review.setIsModerated(false); // Reset moderation status
        
        Review savedReview = reviewRepository.save(review);
        
        // Update product ratings
        updateProductRatings(review.getProductId());
        
        // Clear cache
        cacheService.evictProductCache(review.getProductId());
        cacheService.evictReviewCache(reviewId);
        
        log.info("Review updated successfully with ID: {}", reviewId);
        return mapToReviewResponse(savedReview);
    }
    
    public void deleteReview(String reviewId, String userId) {
        log.info("Deleting review ID: {} by user ID: {}", reviewId, userId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));
        
        // Check if user owns the review
        if (!review.getUserId().equals(userId)) {
            throw new ValidationException("User can only delete their own reviews");
        }
        
        String productId = review.getProductId();
        
        reviewRepository.delete(review);
        
        // Update product ratings
        updateProductRatings(productId);
        
        // Clear cache
        cacheService.evictProductCache(productId);
        cacheService.evictReviewCache(reviewId);
        
        log.info("Review deleted successfully with ID: {}", reviewId);
    }
    
    // Review Moderation
    
    public ReviewDto.ReviewResponse moderateReview(String reviewId, boolean approved, String moderatorComment) {
        log.info("Moderating review ID: {} - approved: {}", reviewId, approved);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));
        
        review.setIsModerated(true);
        review.setIsApproved(approved);
        review.setModeratorComment(moderatorComment);
        review.setUpdatedAt(LocalDateTime.now());
        
        Review savedReview = reviewRepository.save(review);
        
        // Update product ratings if approved
        if (approved) {
            updateProductRatings(review.getProductId());
        }
        
        // Clear cache
        cacheService.evictProductCache(review.getProductId());
        cacheService.evictReviewCache(reviewId);
        
        log.info("Review moderated successfully with ID: {}", reviewId);
        return mapToReviewResponse(savedReview);
    }
    
    public Page<ReviewDto.ReviewResponse> getPendingReviews(Pageable pageable) {
        log.debug("Fetching pending reviews for moderation");
        
        Page<Review> reviews = reviewRepository.findByIsModeratedFalseOrderByCreatedAtDesc(pageable);
        return reviews.map(this::mapToReviewResponse);
    }
    
    public Page<ReviewDto.ReviewResponse> getApprovedReviews(Pageable pageable) {
        log.debug("Fetching approved reviews");
        
        Page<Review> reviews = reviewRepository.findByIsModeratedTrueAndIsApprovedTrueOrderByCreatedAtDesc(pageable);
        return reviews.map(this::mapToReviewResponse);
    }
    
    public Page<ReviewDto.ReviewResponse> getRejectedReviews(Pageable pageable) {
        log.debug("Fetching rejected reviews");
        
        Page<Review> reviews = reviewRepository.findByIsModeratedTrueAndIsApprovedFalseOrderByCreatedAtDesc(pageable);
        return reviews.map(this::mapToReviewResponse);
    }
    
    // Rating Calculations
    
    public ReviewDto.ProductRatingResponse getProductRating(String productId) {
        log.debug("Getting product rating for product ID: {}", productId);
        
        // Try cache first
        ReviewDto.ProductRatingResponse cached = cacheService.getProductRatingFromCache(productId);
        if (cached != null) {
            return cached;
        }
        
        List<Review> approvedReviews = reviewRepository.findByProductIdAndIsModeratedTrueAndIsApprovedTrue(productId);
        
        if (approvedReviews.isEmpty()) {
            ReviewDto.ProductRatingResponse response = ReviewDto.ProductRatingResponse.builder()
                    .productId(productId)
                    .averageRating(0.0)
                    .totalReviews(0)
                    .ratingDistribution(Map.of(1, 0, 2, 0, 3, 0, 4, 0, 5, 0))
                    .build();
            
            cacheService.cacheProductRating(productId, response);
            return response;
        }
        
        double averageRating = approvedReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        
        Map<Integer, Long> ratingDistribution = approvedReviews.stream()
                .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));
        
        // Fill missing ratings with 0
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.putIfAbsent(i, 0L);
        }
        
        ReviewDto.ProductRatingResponse response = ReviewDto.ProductRatingResponse.builder()
                .productId(productId)
                .averageRating(Math.round(averageRating * 100.0) / 100.0) // Round to 2 decimal places
                .totalReviews(approvedReviews.size())
                .ratingDistribution(ratingDistribution)
                .build();
        
        // Cache the result
        cacheService.cacheProductRating(productId, response);
        
        return response;
    }
    
    public List<ReviewDto.ReviewResponse> getTopRatedReviews(String productId, int limit) {
        log.debug("Getting top rated reviews for product ID: {}", productId);
        
        List<Review> reviews = reviewRepository.findByProductIdAndIsModeratedTrueAndIsApprovedTrueOrderByRatingDescCreatedAtDesc(productId)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
        
        return reviews.stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }
    
    public List<ReviewDto.ReviewResponse> getRecentReviews(String productId, int limit) {
        log.debug("Getting recent reviews for product ID: {}", productId);
        
        List<Review> reviews = reviewRepository.findByProductIdAndIsModeratedTrueAndIsApprovedTrueOrderByCreatedAtDesc(productId)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
        
        return reviews.stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }
    
    // Analytics and Reporting
    
    public Map<String, Object> getReviewAnalytics(String productId) {
        log.debug("Getting review analytics for product ID: {}", productId);
        
        return reviewRepositoryCustom.getReviewAnalytics(productId);
    }
    
    public Map<String, Object> getProductRatingAnalytics(String productId) {
        log.debug("Getting product rating analytics for product ID: {}", productId);
        
        return reviewRepositoryCustom.getProductRatingAnalytics(productId);
    }
    
    public Map<String, Object> getReviewTrends(String productId) {
        log.debug("Getting review trends for product ID: {}", productId);
        
        return reviewRepositoryCustom.getReviewTrends(productId);
    }
    
    // Utility Methods
    
    private void updateProductRatings(String productId) {
        log.debug("Updating product ratings for product ID: {}", productId);
        
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return;
        }
        
        List<Review> approvedReviews = reviewRepository.findByProductIdAndIsModeratedTrueAndIsApprovedTrue(productId);
        
        if (approvedReviews.isEmpty()) {
            product.setRatings(Product.Ratings.builder()
                    .averageRating(0.0)
                    .totalReviews(0)
                    .build());
        } else {
            double averageRating = approvedReviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            
            product.setRatings(Product.Ratings.builder()
                    .averageRating(Math.round(averageRating * 100.0) / 100.0)
                    .totalReviews(approvedReviews.size())
                    .build());
        }
        
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
        
        // Clear cache
        cacheService.evictProductCache(productId);
    }
    
    private ReviewDto.ReviewResponse mapToReviewResponse(Review review) {
        return ReviewDto.ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProductId())
                .userId(review.getUserId())
                .rating(review.getRating())
                .comment(review.getComment())
                .images(review.getImages())
                .isModerated(review.getIsModerated())
                .isApproved(review.getIsApproved())
                .moderatorComment(review.getModeratorComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
    
    public boolean hasUserReviewedProduct(String userId, String productId) {
        return reviewRepository.existsByProductIdAndUserId(productId, userId);
    }
    
    public int getReviewCountByUser(String userId) {
        return reviewRepository.countByUserId(userId);
    }
    
    public int getReviewCountByProduct(String productId) {
        return reviewRepository.countByProductIdAndIsModeratedTrueAndIsApprovedTrue(productId);
    }
}
