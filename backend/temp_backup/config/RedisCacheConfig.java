package com.ecommerce.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration for Performance Optimization
 * 
 * This configuration sets up Redis caching with optimized serialization,
 * TTL policies, and cache eviction strategies for the e-commerce application.
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    @Value("${spring.redis.timeout:2000}")
    private int redisTimeout;

    /**
     * Redis Connection Factory
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setPassword(redisPassword);
        config.setDatabase(redisDatabase);

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.setTimeout(Duration.ofMillis(redisTimeout));
        factory.setValidateConnection(true);
        
        return factory;
    }

    /**
     * Jackson Object Mapper for JSON serialization
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * Redis Template with optimized serialization
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Jackson serializer for values
        Jackson2JsonRedisSerializer<Object> jacksonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        jacksonSerializer.setObjectMapper(objectMapper());
        template.setValueSerializer(jacksonSerializer);
        template.setHashValueSerializer(jacksonSerializer);

        template.setDefaultSerializer(jacksonSerializer);
        template.afterPropertiesSet();
        
        return template;
    }

    /**
     * Cache Manager with custom TTL policies
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class)))
                .disableCachingNullValues();

        // Custom cache configurations with different TTL policies
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // User-related caches
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(60)));
        cacheConfigurations.put("user-profiles", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("user-sessions", defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put("user-preferences", defaultConfig.entryTtl(Duration.ofHours(12)));

        // Product-related caches
        cacheConfigurations.put("products", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("product-details", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("product-categories", defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigurations.put("product-featured", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("product-search", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("product-recommendations", defaultConfig.entryTtl(Duration.ofMinutes(20)));

        // Category caches
        cacheConfigurations.put("categories", defaultConfig.entryTtl(Duration.ofHours(12)));
        cacheConfigurations.put("category-tree", defaultConfig.entryTtl(Duration.ofHours(6)));

        // Cart caches
        cacheConfigurations.put("carts", defaultConfig.entryTtl(Duration.ofMinutes(60)));
        cacheConfigurations.put("cart-items", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Order caches
        cacheConfigurations.put("orders", defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigurations.put("order-history", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("order-tracking", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // Review caches
        cacheConfigurations.put("reviews", defaultConfig.entryTtl(Duration.ofMinutes(45)));
        cacheConfigurations.put("review-stats", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Analytics caches
        cacheConfigurations.put("analytics", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("dashboard-stats", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("sales-reports", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // Search caches
        cacheConfigurations.put("search-suggestions", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("search-filters", defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigurations.put("popular-searches", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Inventory caches
        cacheConfigurations.put("inventory", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("stock-levels", defaultConfig.entryTtl(Duration.ofMinutes(2)));

        // Coupon caches
        cacheConfigurations.put("coupons", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("coupon-validation", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // Notification caches
        cacheConfigurations.put("notifications", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("email-templates", defaultConfig.entryTtl(Duration.ofHours(24)));

        // Rate limiting caches
        cacheConfigurations.put("rate-limits", defaultConfig.entryTtl(Duration.ofMinutes(1)));

        // Session caches
        cacheConfigurations.put("sessions", defaultConfig.entryTtl(Duration.ofHours(24)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * Cache configuration for specific use cases
     */
    public static class CacheNames {
        // User caches
        public static final String USERS = "users";
        public static final String USER_PROFILES = "user-profiles";
        public static final String USER_SESSIONS = "user-sessions";
        public static final String USER_PREFERENCES = "user-preferences";

        // Product caches
        public static final String PRODUCTS = "products";
        public static final String PRODUCT_DETAILS = "product-details";
        public static final String PRODUCT_CATEGORIES = "product-categories";
        public static final String PRODUCT_FEATURED = "product-featured";
        public static final String PRODUCT_SEARCH = "product-search";
        public static final String PRODUCT_RECOMMENDATIONS = "product-recommendations";

        // Category caches
        public static final String CATEGORIES = "categories";
        public static final String CATEGORY_TREE = "category-tree";

        // Cart caches
        public static final String CARTS = "carts";
        public static final String CART_ITEMS = "cart-items";

        // Order caches
        public static final String ORDERS = "orders";
        public static final String ORDER_HISTORY = "order-history";
        public static final String ORDER_TRACKING = "order-tracking";

        // Review caches
        public static final String REVIEWS = "reviews";
        public static final String REVIEW_STATS = "review-stats";

        // Analytics caches
        public static final String ANALYTICS = "analytics";
        public static final String DASHBOARD_STATS = "dashboard-stats";
        public static final String SALES_REPORTS = "sales-reports";

        // Search caches
        public static final String SEARCH_SUGGESTIONS = "search-suggestions";
        public static final String SEARCH_FILTERS = "search-filters";
        public static final String POPULAR_SEARCHES = "popular-searches";

        // Inventory caches
        public static final String INVENTORY = "inventory";
        public static final String STOCK_LEVELS = "stock-levels";

        // Coupon caches
        public static final String COUPONS = "coupons";
        public static final String COUPON_VALIDATION = "coupon-validation";

        // Notification caches
        public static final String NOTIFICATIONS = "notifications";
        public static final String EMAIL_TEMPLATES = "email-templates";

        // Rate limiting caches
        public static final String RATE_LIMITS = "rate-limits";

        // Session caches
        public static final String SESSIONS = "sessions";
    }

    /**
     * Cache TTL configuration
     */
    public static class CacheTTL {
        public static final Duration SHORT = Duration.ofMinutes(5);
        public static final Duration MEDIUM = Duration.ofMinutes(30);
        public static final Duration LONG = Duration.ofHours(2);
        public static final Duration VERY_LONG = Duration.ofHours(12);
        public static final Duration SESSION = Duration.ofHours(24);
    }

    /**
     * Cache eviction policies
     */
    public static class EvictionPolicy {
        public static final String LRU = "LRU"; // Least Recently Used
        public static final String LFU = "LFU"; // Least Frequently Used
        public static final String TTL = "TTL"; // Time To Live
        public static final String RANDOM = "RANDOM";
    }
}
