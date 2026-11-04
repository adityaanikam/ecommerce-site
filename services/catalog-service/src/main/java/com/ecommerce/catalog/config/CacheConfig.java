package com.ecommerce.catalog.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Redis caching for catalog reads.
 *
 * Invalidation rule, in one sentence: a product is cached under its own id and is
 * evicted the moment that product is written, and the category tree is cached under
 * one key and evicted whenever the catalog is reseeded. Nothing else is cached, so
 * there is no other way for the cache to go stale.
 *
 * TTLs are a second line of defence rather than the mechanism: even if an eviction
 * were ever missed, an entry cannot be wrong for longer than its TTL.
 */
@Configuration
public class CacheConfig implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    public static final String PRODUCTS_CACHE = "products";
    public static final String CATEGORY_TREE_CACHE = "categoryTree";

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheCustomizer() {
        GenericJackson2JsonRedisSerializer serializer = jsonSerializer();
        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return builder -> builder
                .withCacheConfiguration(PRODUCTS_CACHE, base.entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration(CATEGORY_TREE_CACHE, base.entryTtl(Duration.ofMinutes(30)));
    }

    private static GenericJackson2JsonRedisSerializer jsonSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // Type information is written into the cached JSON so a cached Product comes
        // back as a Product. The validator restricts that to our own package, so a
        // tampered cache entry cannot name an arbitrary class to instantiate.
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("com.ecommerce.catalog.")
                        .allowIfSubType("java.util.")
                        .allowIfSubType("java.lang.")
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL);
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    /**
     * A cache is an optimisation, not a dependency. If Redis is unreachable the
     * request should still be served from MongoDB rather than failing, so cache
     * errors are logged and swallowed instead of propagating.
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis cache get failed for {}::{}, falling through to MongoDB: {}",
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Redis cache put failed for {}::{}: {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                // Worth a louder log than the others: a missed eviction is the one
                // cache failure that can serve stale data until the TTL expires.
                log.error("Redis cache evict failed for {}::{}, entry may be stale until TTL: {}",
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.error("Redis cache clear failed for {}: {}", cache.getName(), exception.getMessage());
            }
        };
    }
}
