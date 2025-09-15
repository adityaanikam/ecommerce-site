package com.ecommerce.repository;

import com.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    
    // Basic queries
    @Query("{'isActive': true}")
    Page<Product> findByIsActiveTrue(Pageable pageable);
    
    @Query("{'isActive': true}")
    List<Product> findAllByIsActiveTrue();
    
    // Category-based queries
    @Query("{'category': ?0, 'isActive': true}")
    Page<Product> findByCategoryAndIsActiveTrue(String category, Pageable pageable);
    
    @Query("{'category': ?0, 'isActive': true}")
    List<Product> findAllByCategoryAndIsActiveTrue(String category);
    
    @Query("{'subcategory': ?0, 'isActive': true}")
    Page<Product> findBySubcategoryAndIsActiveTrue(String subcategory, Pageable pageable);
    
    @Query("{'category': ?0, 'subcategory': ?1, 'isActive': true}")
    Page<Product> findByCategoryAndSubcategoryAndIsActiveTrue(String category, String subcategory, Pageable pageable);
    
    // Brand-based queries
    @Query("{'brand': ?0, 'isActive': true}")
    Page<Product> findByBrandAndIsActiveTrue(String brand, Pageable pageable);
    
    @Query("{'brand': ?0, 'isActive': true}")
    List<Product> findAllByBrandAndIsActiveTrue(String brand);
    
    // Seller-based queries
    @Query("{'sellerId': ?0, 'isActive': true}")
    Page<Product> findBySellerIdAndIsActiveTrue(String sellerId, Pageable pageable);
    
    @Query("{'sellerId': ?0, 'isActive': true}")
    List<Product> findAllBySellerIdAndIsActiveTrue(String sellerId);
    
    // Price range queries
    @Query("{'price': {$gte: ?0, $lte: ?1}, 'isActive': true}")
    Page<Product> findByPriceBetweenAndIsActiveTrue(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    @Query("{'price': {$gte: ?0}, 'isActive': true}")
    Page<Product> findByPriceGreaterThanEqualAndIsActiveTrue(BigDecimal minPrice, Pageable pageable);
    
    @Query("{'price': {$lte: ?0}, 'isActive': true}")
    Page<Product> findByPriceLessThanEqualAndIsActiveTrue(BigDecimal maxPrice, Pageable pageable);
    
    // Discount queries
    @Query("{'discountPrice': {$exists: true, $ne: null}, 'isActive': true}")
    Page<Product> findByDiscountPriceExistsAndIsActiveTrue(Pageable pageable);
    
    @Query("{'discountPrice': {$gte: ?0, $lte: ?1}, 'isActive': true}")
    Page<Product> findByDiscountPriceBetweenAndIsActiveTrue(BigDecimal minDiscount, BigDecimal maxDiscount, Pageable pageable);
    
    // Stock queries
    @Query("{'stock': {$gt: 0}, 'isActive': true}")
    Page<Product> findByStockGreaterThanAndIsActiveTrue(int stock, Pageable pageable);
    
    @Query("{'stock': 0, 'isActive': true}")
    Page<Product> findByStockEqualsAndIsActiveTrue(int stock, Pageable pageable);
    
    // Rating queries
    @Query("{'averageRating': {$gte: ?0}, 'isActive': true}")
    Page<Product> findByAverageRatingGreaterThanEqualAndIsActiveTrue(double minRating, Pageable pageable);
    
    @Query("{'averageRating': {$gte: ?0, $lte: ?1}, 'isActive': true}")
    Page<Product> findByAverageRatingBetweenAndIsActiveTrue(double minRating, double maxRating, Pageable pageable);
    
    // Review count queries
    @Query("{'totalReviews': {$gte: ?0}, 'isActive': true}")
    Page<Product> findByTotalReviewsGreaterThanEqualAndIsActiveTrue(int minReviews, Pageable pageable);
    
    // Text search queries
    @Query("{'$text': {$search: ?0}, 'isActive': true}")
    Page<Product> findByTextSearchAndIsActiveTrue(String searchText, Pageable pageable);
    
    @Query("{'name': {$regex: ?0, $options: 'i'}, 'isActive': true}")
    Page<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);
    
    @Query("{'description': {$regex: ?0, $options: 'i'}, 'isActive': true}")
    Page<Product> findByDescriptionContainingIgnoreCaseAndIsActiveTrue(String description, Pageable pageable);
    
    // Complex search queries
    @Query("{'$or': [{'name': {$regex: ?0, $options: 'i'}}, {'description': {$regex: ?0, $options: 'i'}}, {'brand': {$regex: ?0, $options: 'i'}}], 'isActive': true}")
    Page<Product> searchProducts(String searchTerm, Pageable pageable);
    
    // Combined filter queries
    @Query("{'category': ?0, 'price': {$gte: ?1, $lte: ?2}, 'averageRating': {$gte: ?3}, 'isActive': true}")
    Page<Product> findByCategoryAndPriceBetweenAndAverageRatingGreaterThanEqualAndIsActiveTrue(
            String category, BigDecimal minPrice, BigDecimal maxPrice, double minRating, Pageable pageable);
    
    @Query("{'brand': ?0, 'price': {$gte: ?1, $lte: ?2}, 'isActive': true}")
    Page<Product> findByBrandAndPriceBetweenAndIsActiveTrue(
            String brand, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    // Featured and popular products
    @Query("{'isActive': true}")
    List<Product> findTop10ByIsActiveTrueOrderByAverageRatingDesc();
    
    @Query("{'isActive': true}")
    List<Product> findTop10ByIsActiveTrueOrderByTotalReviewsDesc();
    
    @Query("{'isActive': true}")
    List<Product> findTop10ByIsActiveTrueOrderByCreatedAtDesc();
    
    // Statistics queries
    @Query(value = "{'isActive': true}", count = true)
    long countByIsActiveTrue();
    
    @Query(value = "{'category': ?0, 'isActive': true}", count = true)
    long countByCategoryAndIsActiveTrue(String category);
    
    @Query(value = "{'brand': ?0, 'isActive': true}", count = true)
    long countByBrandAndIsActiveTrue(String brand);
    
    @Query(value = "{'sellerId': ?0, 'isActive': true}", count = true)
    long countBySellerIdAndIsActiveTrue(String sellerId);
    
    // Price statistics
    @Query(value = "{'isActive': true}", fields = "{'price': 1}")
    List<Product> findPricesByIsActiveTrue();
    
    // Low stock products
    @Query("{'stock': {$lte: ?0, $gt: 0}, 'isActive': true}")
    List<Product> findByStockLessThanEqualAndStockGreaterThanAndIsActiveTrue(int maxStock);
    
    // Out of stock products
    @Query("{'stock': 0, 'isActive': true}")
    List<Product> findByStockEqualsAndIsActiveTrue();
}