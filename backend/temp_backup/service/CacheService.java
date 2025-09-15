package com.ecommerce.service;

import com.ecommerce.config.RedisCacheConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Cache Service for Redis Operations
 * 
 * This service provides high-level cache operations for the e-commerce application,
 * including caching, retrieval, invalidation, and cache statistics.
 */
@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Store data in cache with default TTL
     */
    public void put(String cacheName, String key, Object value) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
                logger.debug("Cached data for key: {} in cache: {}", key, cacheName);
            } else {
                logger.warn("Cache not found: {}", cacheName);
            }
        } catch (Exception e) {
            logger.error("Error caching data for key: {} in cache: {}", key, cacheName, e);
        }
    }

    /**
     * Store data in cache with custom TTL
     */
    public void put(String cacheName, String key, Object value, Duration ttl) {
        try {
            // Use Redis template directly for custom TTL
            redisTemplate.opsForValue().set(
                cacheName + "::" + key, 
                value, 
                ttl.toMillis(), 
                TimeUnit.MILLISECONDS
            );
            logger.debug("Cached data for key: {} in cache: {} with TTL: {}", key, cacheName, ttl);
        } catch (Exception e) {
            logger.error("Error caching data for key: {} in cache: {} with TTL: {}", key, cacheName, ttl, e);
        }
    }

    /**
     * Retrieve data from cache
     */
    public <T> T get(String cacheName, String key, Class<T> type) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(key);
                if (wrapper != null) {
                    Object value = wrapper.get();
                    if (value != null && type.isAssignableFrom(value.getClass())) {
                        logger.debug("Cache hit for key: {} in cache: {}", key, cacheName);
                        return type.cast(value);
                    }
                }
            }
            logger.debug("Cache miss for key: {} in cache: {}", key, cacheName);
            return null;
        } catch (Exception e) {
            logger.error("Error retrieving data for key: {} from cache: {}", key, cacheName, e);
            return null;
        }
    }

    /**
     * Retrieve data from cache with Redis template (for custom TTL entries)
     */
    public <T> T getWithRedis(String cacheName, String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(cacheName + "::" + key);
            if (value != null && type.isAssignableFrom(value.getClass())) {
                logger.debug("Redis cache hit for key: {} in cache: {}", key, cacheName);
                return type.cast(value);
            }
            logger.debug("Redis cache miss for key: {} in cache: {}", key, cacheName);
            return null;
        } catch (Exception e) {
            logger.error("Error retrieving data for key: {} from Redis cache: {}", key, cacheName, e);
            return null;
        }
    }

    /**
     * Check if key exists in cache
     */
    public boolean exists(String cacheName, String key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                return cache.get(key) != null;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error checking existence for key: {} in cache: {}", key, cacheName, e);
            return false;
        }
    }

    /**
     * Remove data from cache
     */
    public void evict(String cacheName, String key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
                logger.debug("Evicted data for key: {} from cache: {}", key, cacheName);
            }
        } catch (Exception e) {
            logger.error("Error evicting data for key: {} from cache: {}", key, cacheName, e);
        }
    }

    /**
     * Clear entire cache
     */
    public void clear(String cacheName) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                logger.info("Cleared cache: {}", cacheName);
            }
        } catch (Exception e) {
            logger.error("Error clearing cache: {}", cacheName, e);
        }
    }

    /**
     * Invalidate cache by pattern
     */
    public void invalidateByPattern(String cacheName, String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(cacheName + "::" + pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                logger.info("Invalidated {} keys matching pattern: {} in cache: {}", keys.size(), pattern, cacheName);
            }
        } catch (Exception e) {
            logger.error("Error invalidating pattern: {} in cache: {}", pattern, cacheName, e);
        }
    }

    /**
     * Get cache statistics
     */
    public CacheStats getCacheStats(String cacheName) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                // Get Redis info for the cache
                String info = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .info("memory");
                
                return new CacheStats(cacheName, info);
            }
            return new CacheStats(cacheName, "Cache not found");
        } catch (Exception e) {
            logger.error("Error getting cache stats for: {}", cacheName, e);
            return new CacheStats(cacheName, "Error: " + e.getMessage());
        }
    }

    /**
     * Warm up cache with data
     */
    public void warmUpCache(String cacheName, String key, Object value) {
        try {
            put(cacheName, key, value);
            logger.info("Warmed up cache: {} with key: {}", cacheName, key);
        } catch (Exception e) {
            logger.error("Error warming up cache: {} with key: {}", cacheName, key, e);
        }
    }

    /**
     * Batch operations for better performance
     */
    public void putBatch(String cacheName, List<CacheEntry> entries) {
        try {
            entries.forEach(entry -> put(cacheName, entry.getKey(), entry.getValue()));
            logger.debug("Batch cached {} entries in cache: {}", entries.size(), cacheName);
        } catch (Exception e) {
            logger.error("Error batch caching entries in cache: {}", cacheName, e);
        }
    }

    /**
     * Get multiple values at once
     */
    public List<Object> getBatch(String cacheName, List<String> keys) {
        try {
            return keys.stream()
                .map(key -> {
                    Cache cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        Cache.ValueWrapper wrapper = cache.get(key);
                        return wrapper != null ? wrapper.get() : null;
                    }
                    return null;
                })
                .toList();
        } catch (Exception e) {
            logger.error("Error batch retrieving from cache: {}", cacheName, e);
            return List.of();
        }
    }

    /**
     * Cache with conditional logic
     */
    public <T> T getOrPut(String cacheName, String key, Class<T> type, CacheLoader<T> loader) {
        T cached = get(cacheName, key, type);
        if (cached != null) {
            return cached;
        }

        T value = loader.load();
        if (value != null) {
            put(cacheName, key, value);
        }
        return value;
    }

    /**
     * Cache with conditional logic and custom TTL
     */
    public <T> T getOrPut(String cacheName, String key, Class<T> type, CacheLoader<T> loader, Duration ttl) {
        T cached = getWithRedis(cacheName, key, type);
        if (cached != null) {
            return cached;
        }

        T value = loader.load();
        if (value != null) {
            put(cacheName, key, value, ttl);
        }
        return value;
    }

    /**
     * Get all cache names
     */
    public Collection<String> getCacheNames() {
        return cacheManager.getCacheNames();
    }

    /**
     * Cache entry for batch operations
     */
    public static class CacheEntry {
        private final String key;
        private final Object value;

        public CacheEntry(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }
    }

    /**
     * Cache loader interface
     */
    @FunctionalInterface
    public interface CacheLoader<T> {
        T load();
    }

    /**
     * Cache statistics
     */
    public static class CacheStats {
        private final String cacheName;
        private final String info;

        public CacheStats(String cacheName, String info) {
            this.cacheName = cacheName;
            this.info = info;
        }

        public String getCacheName() {
            return cacheName;
        }

        public String getInfo() {
            return info;
        }
    }

    /**
     * Cache health check
     */
    public boolean isHealthy() {
        try {
            redisTemplate.opsForValue().set("health-check", "ok", Duration.ofSeconds(10));
            String result = (String) redisTemplate.opsForValue().get("health-check");
            return "ok".equals(result);
        } catch (Exception e) {
            logger.error("Cache health check failed", e);
            return false;
        }
    }

    /**
     * Get cache size
     */
    public long getCacheSize(String cacheName) {
        try {
            Set<String> keys = redisTemplate.keys(cacheName + "::*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            logger.error("Error getting cache size for: {}", cacheName, e);
            return 0;
        }
    }

    /**
     * Set cache expiration
     */
    public void expire(String cacheName, String key, Duration ttl) {
        try {
            redisTemplate.expire(cacheName + "::" + key, ttl);
            logger.debug("Set expiration for key: {} in cache: {} to: {}", key, cacheName, ttl);
        } catch (Exception e) {
            logger.error("Error setting expiration for key: {} in cache: {}", key, cacheName, e);
        }
    }
}