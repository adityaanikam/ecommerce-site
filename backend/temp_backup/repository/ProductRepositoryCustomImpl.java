package com.ecommerce.repository;

import com.ecommerce.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Repository
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Override
    public Page<Product> searchProductsWithFilters(String searchTerm, String category, String brand,
                                                 BigDecimal minPrice, BigDecimal maxPrice,
                                                 Double minRating, Integer minReviews,
                                                 String sortBy, String sortDirection, Pageable pageable) {
        
        List<Criteria> criteriaList = new ArrayList<>();
        
        // Base criteria - only active products
        criteriaList.add(Criteria.where("isActive").is(true));
        
        // Search term criteria
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            criteriaList.add(new Criteria().orOperator(
                Criteria.where("name").regex(searchTerm, "i"),
                Criteria.where("description").regex(searchTerm, "i"),
                Criteria.where("brand").regex(searchTerm, "i")
            ));
        }
        
        // Category filter
        if (category != null && !category.trim().isEmpty()) {
            criteriaList.add(Criteria.where("category").is(category));
        }
        
        // Brand filter
        if (brand != null && !brand.trim().isEmpty()) {
            criteriaList.add(Criteria.where("brand").is(brand));
        }
        
        // Price range filter
        if (minPrice != null || maxPrice != null) {
            Criteria priceCriteria = Criteria.where("price");
            if (minPrice != null && maxPrice != null) {
                priceCriteria.gte(minPrice).lte(maxPrice);
            } else if (minPrice != null) {
                priceCriteria.gte(minPrice);
            } else {
                priceCriteria.lte(maxPrice);
            }
            criteriaList.add(priceCriteria);
        }
        
        // Rating filter
        if (minRating != null) {
            criteriaList.add(Criteria.where("averageRating").gte(minRating));
        }
        
        // Reviews filter
        if (minReviews != null) {
            criteriaList.add(Criteria.where("totalReviews").gte(minReviews));
        }
        
        Criteria finalCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
        
        Query query = new Query(finalCriteria);
        
        // Apply sorting
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            query.with(Sort.by(direction, sortBy));
        }
        
        // Apply pagination
        query.with(pageable);
        
        List<Product> products = mongoTemplate.find(query, Product.class);
        long total = mongoTemplate.count(Query.query(finalCriteria), Product.class);
        
        return new PageImpl<>(products, pageable, total);
    }
    
    @Override
    public Map<String, Object> getProductAnalytics(String productId) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("_id").is(productId)),
            project()
                .and("averageRating").as("avgRating")
                .and("totalReviews").as("reviewCount")
                .and("stock").as("currentStock")
                .and("price").as("currentPrice")
                .and("discountPrice").as("discountedPrice")
                .and(ArithmeticOperators.Subtract.valueOf("price").subtract("discountPrice"))
                .as("discountAmount")
                .and(ArithmeticOperators.Divide.valueOf(
                    ArithmeticOperators.Subtract.valueOf("price").subtract("discountPrice")
                ).divideBy("price").multiplyBy(100))
                .as("discountPercentage")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "products", Map.class);
        return results.getUniqueMappedResult();
    }
    
    @Override
    public Map<String, Long> getProductCountsByCategory() {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("isActive").is(true)),
            group("category").count().as("count"),
            project("count").and("category").previousOperation()
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "products", Map.class);
        return results.getMappedResults().stream()
            .collect(java.util.stream.Collectors.toMap(
                map -> (String) map.get("category"),
                map -> (Long) map.get("count")
            ));
    }
    
    @Override
    public Map<String, Long> getProductCountsByBrand() {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("isActive").is(true)),
            group("brand").count().as("count"),
            project("count").and("brand").previousOperation()
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "products", Map.class);
        return results.getMappedResults().stream()
            .collect(java.util.stream.Collectors.toMap(
                map -> (String) map.get("brand"),
                map -> (Long) map.get("count")
            ));
    }
    
    @Override
    public Map<String, Object> getPriceRangeAnalytics() {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("isActive").is(true)),
            group()
                .min("price").as("minPrice")
                .max("price").as("maxPrice")
                .avg("price").as("avgPrice")
                .count().as("totalProducts")
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "products", Map.class);
        return results.getUniqueMappedResult();
    }
    
    @Override
    public List<Product> getTopRatedProductsByCategory(String category, int limit) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("category").is(category).and("isActive").is(true)),
            sort(Sort.Direction.DESC, "averageRating", "totalReviews"),
            limit(limit)
        );
        
        AggregationResults<Product> results = mongoTemplate.aggregate(aggregation, "products", Product.class);
        return results.getMappedResults();
    }
    
    @Override
    public List<Product> getMostReviewedProducts(int limit) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("isActive").is(true)),
            sort(Sort.Direction.DESC, "totalReviews", "averageRating"),
            limit(limit)
        );
        
        AggregationResults<Product> results = mongoTemplate.aggregate(aggregation, "products", Product.class);
        return results.getMappedResults();
    }
    
    @Override
    public List<Product> getLowStockProducts(int threshold) {
        Query query = new Query(Criteria.where("stock").lte(threshold)
            .and("isActive").is(true));
        query.with(Sort.by(Sort.Direction.ASC, "stock"));
        
        return mongoTemplate.find(query, Product.class);
    }
    
    @Override
    public List<Product> getProductsBySellerPerformance(String sellerId, int limit) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("sellerId").is(sellerId).and("isActive").is(true)),
            sort(Sort.Direction.DESC, "averageRating", "totalReviews"),
            limit(limit)
        );
        
        AggregationResults<Product> results = mongoTemplate.aggregate(aggregation, "products", Product.class);
        return results.getMappedResults();
    }
    
    @Override
    public List<String> getProductSearchSuggestions(String query, int limit) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("name").regex(query, "i").and("isActive").is(true)),
            project("name"),
            limit(limit)
        );
        
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "products", Map.class);
        return results.getMappedResults().stream()
            .map(map -> (String) map.get("name"))
            .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    public List<Product> getSimilarProducts(String productId, int limit) {
        // First get the product to find similar ones
        Product product = mongoTemplate.findById(productId, Product.class);
        if (product == null) {
            return new ArrayList<>();
        }
        
        Aggregation aggregation = newAggregation(
            match(Criteria.where("category").is(product.getCategory())
                .and("brand").is(product.getBrand())
                .and("_id").ne(productId)
                .and("isActive").is(true)),
            sort(Sort.Direction.DESC, "averageRating"),
            limit(limit)
        );
        
        AggregationResults<Product> results = mongoTemplate.aggregate(aggregation, "products", Product.class);
        return results.getMappedResults();
    }
    
    @Override
    public List<Product> getTrendingProducts(int limit) {
        // Trending based on recent reviews and high ratings
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        Aggregation aggregation = newAggregation(
            match(Criteria.where("isActive").is(true)
                .and("averageRating").gte(4.0)
                .and("totalReviews").gte(5)),
            sort(Sort.Direction.DESC, "averageRating", "totalReviews"),
            limit(limit)
        );
        
        AggregationResults<Product> results = mongoTemplate.aggregate(aggregation, "products", Product.class);
        return results.getMappedResults();
    }
    
    @Override
    public List<Product> getNewArrivals(int limit) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        
        Query query = new Query(Criteria.where("createdAt").gte(sevenDaysAgo)
            .and("isActive").is(true));
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.limit(limit);
        
        return mongoTemplate.find(query, Product.class);
    }
    
    @Override
    public List<Product> getDiscountedProducts(int limit) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("discountPrice").exists(true)
                .and("discountPrice").ne(null)
                .and("isActive").is(true)),
            sort(Sort.Direction.DESC, "discountPrice"),
            limit(limit)
        );
        
        AggregationResults<Product> results = mongoTemplate.aggregate(aggregation, "products", Product.class);
        return results.getMappedResults();
    }
    
    @Override
    public Map<String, Object> getProductPerformanceMetrics(String productId) {
        // This would typically involve multiple aggregations across products, orders, and reviews
        // For now, returning basic metrics
        return getProductAnalytics(productId);
    }
}
