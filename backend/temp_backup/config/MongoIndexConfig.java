package com.ecommerce.config;

import com.ecommerce.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Sort;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * MongoDB Index Configuration for Performance Optimization
 * 
 * This class creates and manages database indexes to optimize query performance
 * for the e-commerce application.
 */
@Component
public class MongoIndexConfig {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void createIndexes() {
        createUserIndexes();
        createProductIndexes();
        createCategoryIndexes();
        createOrderIndexes();
        createCartIndexes();
        createReviewIndexes();
        createAuditIndexes();
    }

    /**
     * Create indexes for User collection
     */
    private void createUserIndexes() {
        IndexOperations userIndexOps = mongoTemplate.indexOps(User.class);

        // Unique email index
        userIndexOps.ensureIndex(new Index().on("email", Sort.Direction.ASC).unique());

        // Username index for login
        userIndexOps.ensureIndex(new Index().on("username", Sort.Direction.ASC).unique());

        // Phone number index
        userIndexOps.ensureIndex(new Index().on("phoneNumber", Sort.Direction.ASC).sparse());

        // Role-based queries
        userIndexOps.ensureIndex(new Index().on("roles", Sort.Direction.ASC));

        // Status and active queries
        userIndexOps.ensureIndex(new Index().on("isActive", Sort.Direction.ASC));

        // Created date for analytics
        userIndexOps.ensureIndex(new Index().on("createdAt", Sort.Direction.DESC));

        // Last login for user activity
        userIndexOps.ensureIndex(new Index().on("lastLoginAt", Sort.Direction.DESC));

        // Email verification
        userIndexOps.ensureIndex(new Index().on("emailVerified", Sort.Direction.ASC));

        // Password reset tokens (TTL index - expires after 1 hour)
        userIndexOps.ensureIndex(new Index().on("passwordResetToken", Sort.Direction.ASC)
                .expire(1, TimeUnit.HOURS).sparse());

        // Email verification tokens (TTL index - expires after 24 hours)
        userIndexOps.ensureIndex(new Index().on("emailVerificationToken", Sort.Direction.ASC)
                .expire(24, TimeUnit.HOURS).sparse());

        // Compound indexes for common queries
        userIndexOps.ensureIndex(new Index().on("isActive", Sort.Direction.ASC)
                .on("roles", Sort.Direction.ASC));

        userIndexOps.ensureIndex(new Index().on("emailVerified", Sort.Direction.ASC)
                .on("isActive", Sort.Direction.ASC));

        // Text search on user names
        userIndexOps.ensureIndex(new TextIndexDefinition.TextIndexDefinitionBuilder()
                .onField("firstName", 2.0f)
                .onField("lastName", 2.0f)
                .onField("email", 1.0f)
                .build());
    }

    /**
     * Create indexes for Product collection
     */
    private void createProductIndexes() {
        IndexOperations productIndexOps = mongoTemplate.indexOps(Product.class);

        // SKU unique index
        productIndexOps.ensureIndex(new Index().on("sku", Sort.Direction.ASC).unique());

        // Name for search
        productIndexOps.ensureIndex(new Index().on("name", Sort.Direction.ASC));

        // Category-based queries
        productIndexOps.ensureIndex(new Index().on("categoryId", Sort.Direction.ASC));

        // Brand-based queries
        productIndexOps.ensureIndex(new Index().on("brand", Sort.Direction.ASC));

        // Price range queries
        productIndexOps.ensureIndex(new Index().on("price", Sort.Direction.ASC));

        // Stock availability
        productIndexOps.ensureIndex(new Index().on("stock", Sort.Direction.ASC));

        // Product status
        productIndexOps.ensureIndex(new Index().on("status", Sort.Direction.ASC));

        // Featured products
        productIndexOps.ensureIndex(new Index().on("isFeatured", Sort.Direction.ASC));

        // Created date for new products
        productIndexOps.ensureIndex(new Index().on("createdAt", Sort.Direction.DESC));

        // Updated date for recently updated
        productIndexOps.ensureIndex(new Index().on("updatedAt", Sort.Direction.DESC));

        // Rating for sorting
        productIndexOps.ensureIndex(new Index().on("rating", Sort.Direction.DESC));

        // Review count for sorting
        productIndexOps.ensureIndex(new Index().on("reviewCount", Sort.Direction.DESC));

        // Tags for filtering
        productIndexOps.ensureIndex(new Index().on("tags", Sort.Direction.ASC));

        // Compound indexes for common product queries
        productIndexOps.ensureIndex(new Index().on("categoryId", Sort.Direction.ASC)
                .on("status", Sort.Direction.ASC)
                .on("isActive", Sort.Direction.ASC));

        productIndexOps.ensureIndex(new Index().on("categoryId", Sort.Direction.ASC)
                .on("price", Sort.Direction.ASC));

        productIndexOps.ensureIndex(new Index().on("categoryId", Sort.Direction.ASC)
                .on("rating", Sort.Direction.DESC));

        productIndexOps.ensureIndex(new Index().on("brand", Sort.Direction.ASC)
                .on("categoryId", Sort.Direction.ASC));

        productIndexOps.ensureIndex(new Index().on("isFeatured", Sort.Direction.ASC)
                .on("status", Sort.Direction.ASC)
                .on("createdAt", Sort.Direction.DESC));

        productIndexOps.ensureIndex(new Index().on("stock", Sort.Direction.ASC)
                .on("status", Sort.Direction.ASC));

        // Text search on product content
        productIndexOps.ensureIndex(new TextIndexDefinition.TextIndexDefinitionBuilder()
                .onField("name", 3.0f)
                .onField("description", 2.0f)
                .onField("brand", 2.0f)
                .onField("tags", 1.0f)
                .onField("specifications", 1.0f)
                .build());

        // Geospatial index for location-based products (commented out - not available in current Spring Data MongoDB version)
        // productIndexOps.ensureIndex(new GeoSpatialIndex("location")
        //         .typed(GeoSpatialIndexType.GEO_2DSPHERE));
    }

    /**
     * Create indexes for Category collection
     */
    private void createCategoryIndexes() {
        IndexOperations categoryIndexOps = mongoTemplate.indexOps(Category.class);

        // Name for search
        categoryIndexOps.ensureIndex(new Index().on("name", Sort.Direction.ASC));

        // Slug for URL routing
        categoryIndexOps.ensureIndex(new Index().on("slug", Sort.Direction.ASC).unique());

        // Parent category hierarchy
        categoryIndexOps.ensureIndex(new Index().on("parentId", Sort.Direction.ASC));

        // Category status
        categoryIndexOps.ensureIndex(new Index().on("isActive", Sort.Direction.ASC));

        // Display order
        categoryIndexOps.ensureIndex(new Index().on("displayOrder", Sort.Direction.ASC));

        // Created date
        categoryIndexOps.ensureIndex(new Index().on("createdAt", Sort.Direction.DESC));

        // Compound indexes for category queries
        categoryIndexOps.ensureIndex(new Index().on("parentId", Sort.Direction.ASC)
                .on("isActive", Sort.Direction.ASC)
                .on("displayOrder", Sort.Direction.ASC));

        // Text search on category names and descriptions
        categoryIndexOps.ensureIndex(new TextIndexDefinition.TextIndexDefinitionBuilder()
                .onField("name", 2.0f)
                .onField("description", 1.0f)
                .build());
    }

    /**
     * Create indexes for Order collection
     */
    private void createOrderIndexes() {
        IndexOperations orderIndexOps = mongoTemplate.indexOps(Order.class);

        // Order number unique index
        orderIndexOps.ensureIndex(new Index().on("orderNumber", Sort.Direction.ASC).unique());

        // User-based queries
        orderIndexOps.ensureIndex(new Index().on("userId", Sort.Direction.ASC));

        // Order status
        orderIndexOps.ensureIndex(new Index().on("status", Sort.Direction.ASC));

        // Payment status
        orderIndexOps.ensureIndex(new Index().on("paymentStatus", Sort.Direction.ASC));

        // Order date for analytics
        orderIndexOps.ensureIndex(new Index().on("orderDate", Sort.Direction.DESC));

        // Total amount for analytics
        orderIndexOps.ensureIndex(new Index().on("totalAmount", Sort.Direction.ASC));

        // Shipping address country
        orderIndexOps.ensureIndex(new Index().on("shippingAddress.country", Sort.Direction.ASC));

        // Shipping address state
        orderIndexOps.ensureIndex(new Index().on("shippingAddress.state", Sort.Direction.ASC));

        // Tracking number
        orderIndexOps.ensureIndex(new Index().on("trackingNumber", Sort.Direction.ASC).sparse());

        // Compound indexes for common order queries
        orderIndexOps.ensureIndex(new Index().on("userId", Sort.Direction.ASC)
                .on("orderDate", Sort.Direction.DESC));

        orderIndexOps.ensureIndex(new Index().on("status", Sort.Direction.ASC)
                .on("orderDate", Sort.Direction.DESC));

        orderIndexOps.ensureIndex(new Index().on("paymentStatus", Sort.Direction.ASC)
                .on("orderDate", Sort.Direction.DESC));

        orderIndexOps.ensureIndex(new Index().on("userId", Sort.Direction.ASC)
                .on("status", Sort.Direction.ASC));

        // Analytics indexes
        orderIndexOps.ensureIndex(new Index().on("orderDate", Sort.Direction.DESC)
                .on("totalAmount", Sort.Direction.ASC));

        // TTL index for temporary orders (expires after 30 days)
        orderIndexOps.ensureIndex(new Index().on("createdAt", Sort.Direction.ASC)
                .expire(30, TimeUnit.DAYS).sparse());
    }

    /**
     * Create indexes for Cart collection
     */
    private void createCartIndexes() {
        IndexOperations cartIndexOps = mongoTemplate.indexOps(Cart.class);

        // User-based queries
        cartIndexOps.ensureIndex(new Index().on("userId", Sort.Direction.ASC).unique());

        // Session-based queries for guest carts
        cartIndexOps.ensureIndex(new Index().on("sessionId", Sort.Direction.ASC).sparse());

        // Updated date for cart cleanup
        cartIndexOps.ensureIndex(new Index().on("updatedAt", Sort.Direction.DESC));

        // TTL index for abandoned carts (expires after 30 days)
        cartIndexOps.ensureIndex(new Index().on("updatedAt", Sort.Direction.ASC)
                .expire(30, TimeUnit.DAYS));

        // Cart items product lookup
        cartIndexOps.ensureIndex(new Index().on("items.productId", Sort.Direction.ASC));
    }

    /**
     * Create indexes for Review collection
     */
    private void createReviewIndexes() {
        IndexOperations reviewIndexOps = mongoTemplate.indexOps(Review.class);

        // Product-based queries
        reviewIndexOps.ensureIndex(new Index().on("productId", Sort.Direction.ASC));

        // User-based queries
        reviewIndexOps.ensureIndex(new Index().on("userId", Sort.Direction.ASC));

        // Rating for sorting
        reviewIndexOps.ensureIndex(new Index().on("rating", Sort.Direction.DESC));

        // Review date
        reviewIndexOps.ensureIndex(new Index().on("createdAt", Sort.Direction.DESC));

        // Review status
        reviewIndexOps.ensureIndex(new Index().on("status", Sort.Direction.ASC));

        // Verified purchase
        reviewIndexOps.ensureIndex(new Index().on("isVerified", Sort.Direction.ASC));

        // Helpful votes
        reviewIndexOps.ensureIndex(new Index().on("helpfulCount", Sort.Direction.DESC));

        // Compound indexes for review queries
        reviewIndexOps.ensureIndex(new Index().on("productId", Sort.Direction.ASC)
                .on("status", Sort.Direction.ASC)
                .on("createdAt", Sort.Direction.DESC));

        reviewIndexOps.ensureIndex(new Index().on("productId", Sort.Direction.ASC)
                .on("rating", Sort.Direction.DESC));

        reviewIndexOps.ensureIndex(new Index().on("productId", Sort.Direction.ASC)
                .on("isVerified", Sort.Direction.ASC));

        // Unique constraint: one review per user per product
        reviewIndexOps.ensureIndex(new Index().on("productId", Sort.Direction.ASC)
                .on("userId", Sort.Direction.ASC).unique());

        // Text search on review content
        reviewIndexOps.ensureIndex(new TextIndexDefinition.TextIndexDefinitionBuilder()
                .onField("title", 2.0f)
                .onField("content", 1.0f)
                .build());
    }

    /**
     * Create indexes for audit and logging collections
     */
    private void createAuditIndexes() {
        // User activity logs
        IndexOperations userActivityOps = mongoTemplate.indexOps("user_activity_logs");
        userActivityOps.ensureIndex(new Index().on("userId", Sort.Direction.ASC)
                .on("timestamp", Sort.Direction.DESC));
        userActivityOps.ensureIndex(new Index().on("action", Sort.Direction.ASC)
                .on("timestamp", Sort.Direction.DESC));
        
        // TTL index for logs (expires after 90 days)
        userActivityOps.ensureIndex(new Index().on("timestamp", Sort.Direction.ASC)
                .expire(90, TimeUnit.DAYS));

        // API access logs
        IndexOperations apiLogsOps = mongoTemplate.indexOps("api_access_logs");
        apiLogsOps.ensureIndex(new Index().on("endpoint", Sort.Direction.ASC)
                .on("timestamp", Sort.Direction.DESC));
        apiLogsOps.ensureIndex(new Index().on("userId", Sort.Direction.ASC)
                .on("timestamp", Sort.Direction.DESC));
        apiLogsOps.ensureIndex(new Index().on("responseTime", Sort.Direction.ASC));
        
        // TTL index for API logs (expires after 30 days)
        apiLogsOps.ensureIndex(new Index().on("timestamp", Sort.Direction.ASC)
                .expire(30, TimeUnit.DAYS));

        // Error logs
        IndexOperations errorLogsOps = mongoTemplate.indexOps("error_logs");
        errorLogsOps.ensureIndex(new Index().on("level", Sort.Direction.ASC)
                .on("timestamp", Sort.Direction.DESC));
        errorLogsOps.ensureIndex(new Index().on("service", Sort.Direction.ASC)
                .on("timestamp", Sort.Direction.DESC));
        
        // TTL index for error logs (expires after 60 days)
        errorLogsOps.ensureIndex(new Index().on("timestamp", Sort.Direction.ASC)
                .expire(60, TimeUnit.DAYS));
    }

    /**
     * Create performance monitoring indexes
     */
    public void createPerformanceIndexes() {
        // Query performance metrics
        IndexOperations queryMetricsOps = mongoTemplate.indexOps("query_performance_metrics");
        queryMetricsOps.ensureIndex(new Index().on("collection", Sort.Direction.ASC)
                .on("timestamp", Sort.Direction.DESC));
        queryMetricsOps.ensureIndex(new Index().on("executionTime", Sort.Direction.ASC));
        
        // TTL index for metrics (expires after 7 days)
        queryMetricsOps.ensureIndex(new Index().on("timestamp", Sort.Direction.ASC)
                .expire(7, TimeUnit.DAYS));

        // Cache hit/miss statistics
        IndexOperations cacheStatsOps = mongoTemplate.indexOps("cache_statistics");
        cacheStatsOps.ensureIndex(new Index().on("cacheName", Sort.Direction.ASC)
                .on("timestamp", Sort.Direction.DESC));
        
        // TTL index for cache stats (expires after 3 days)
        cacheStatsOps.ensureIndex(new Index().on("timestamp", Sort.Direction.ASC)
                .expire(3, TimeUnit.DAYS));
    }

    /**
     * Drop all indexes (use with caution - for development only)
     */
    public void dropAllIndexes() {
        mongoTemplate.indexOps(User.class).dropAllIndexes();
        mongoTemplate.indexOps(Product.class).dropAllIndexes();
        mongoTemplate.indexOps(Category.class).dropAllIndexes();
        mongoTemplate.indexOps(Order.class).dropAllIndexes();
        mongoTemplate.indexOps(Cart.class).dropAllIndexes();
        mongoTemplate.indexOps(Review.class).dropAllIndexes();
    }

    /**
     * Get index information for monitoring
     */
    public void printIndexInfo() {
        System.out.println("=== MongoDB Index Information ===");
        
        String[] collections = {"users", "products", "categories", "orders", "carts", "reviews"};
        
        for (String collection : collections) {
            System.out.println("\n--- " + collection.toUpperCase() + " ---");
            mongoTemplate.indexOps(collection).getIndexInfo().forEach(indexInfo -> {
                System.out.println("Index: " + indexInfo.getName() + 
                                 " | Keys: " + indexInfo.getIndexFields() + 
                                 " | Unique: " + indexInfo.isUnique() +
                                 " | Sparse: " + indexInfo.isSparse());
            });
        }
    }
}
