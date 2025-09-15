package com.ecommerce.repository;

import com.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ProductRepositoryCustom {
    
    // Advanced search with filters and sorting
    Page<Product> searchProductsWithFilters(String searchTerm, String category, String brand, 
                                          BigDecimal minPrice, BigDecimal maxPrice, 
                                          Double minRating, Integer minReviews, 
                                          String sortBy, String sortDirection, Pageable pageable);
    
    // Product analytics
    Map<String, Object> getProductAnalytics(String productId);
    
    // Category-wise product counts
    Map<String, Long> getProductCountsByCategory();
    
    // Brand-wise product counts
    Map<String, Long> getProductCountsByBrand();
    
    // Price range analytics
    Map<String, Object> getPriceRangeAnalytics();
    
    // Top-rated products by category
    List<Product> getTopRatedProductsByCategory(String category, int limit);
    
    // Most reviewed products
    List<Product> getMostReviewedProducts(int limit);
    
    // Products with low stock
    List<Product> getLowStockProducts(int threshold);
    
    // Products by seller performance
    List<Product> getProductsBySellerPerformance(String sellerId, int limit);
    
    // Product search suggestions
    List<String> getProductSearchSuggestions(String query, int limit);
    
    // Similar products
    List<Product> getSimilarProducts(String productId, int limit);
    
    // Trending products
    List<Product> getTrendingProducts(int limit);
    
    // New arrivals
    List<Product> getNewArrivals(int limit);
    
    // Discounted products
    List<Product> getDiscountedProducts(int limit);
    
    // Product performance metrics
    Map<String, Object> getProductPerformanceMetrics(String productId);
}
