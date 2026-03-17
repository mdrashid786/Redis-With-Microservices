package com.product.service.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 🔥 PRODUCT SERVICE - REDIS CACHE CONFIGURATION
 *
 * Cache Names (Match @Cacheable value):
 * ✓ product_by_id (30 min + 0-5 min random)
 * ✓ product_list (5 min + 0-90 sec random)
 * ✓ product_stock (2 min + 0-30 sec random)
 * ✓ product_not_found (10 sec + 0-5 sec random)
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    private static final Random RANDOM = new Random();

    /**
     * Helper to create cache config with randomized TTL
     */
    private static RedisCacheConfiguration createCacheConfig(long baseSeconds, int randomSeconds) {
        long totalSeconds = baseSeconds + RANDOM.nextInt(randomSeconds + 1);
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(totalSeconds))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()
                        )
                );
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // Default config (fallback)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()
                        )
                );

        // ════════════════════════════════════════════════════════════════════════════
        // PRODUCT SERVICE SPECIFIC CACHES
        // ════════════════════════════════════════════════════════════════════════════

        /**
         * product_by_id: 30 minutes + 0-5 minutes random
         * Used by: @Cacheable(value = "product_by_id", key = "#id")
         *
         * Why 30 min?
         * - Product details rarely change
         * - User might view same product multiple times in 30 min
         * - Acceptable if slightly stale
         */
        RedisCacheConfiguration productByIdCache = createCacheConfig(1800, 300);

        /**
         * product_list: 5 minutes + 0-90 seconds random
         * Used by: @Cacheable(value = "product_list", key = "'all'")
         *
         * Why 5 min?
         * - Catalog changes frequently (products added/removed)
         * - Need fresher data than individual product
         */
        RedisCacheConfiguration productListCache = createCacheConfig(300, 90);

        /**
         * product_stock: 2 minutes + 0-30 seconds random
         * Used by: @Cacheable(value = "product_stock", key = "#id")
         *
         * Why 2 min? (SHORTEST TTL in this service!)
         * - Stock is MOST VOLATILE
         * - Changes on EVERY order
         * - Stale stock = overbooking risk!
         * - Must be very fresh
         */
        RedisCacheConfiguration productStockCache = createCacheConfig(120, 30);

        /**
         * product_not_found: 10 seconds + 0-5 seconds random
         * Used by: Custom logic checking non-existent products
         *
         * Why 10 sec?
         * - DOS protection (attacker probes non-existent IDs)
         * - Short TTL: When product is created, soon available in search
         * - Don't want to cache misses for long
         */
        RedisCacheConfiguration productNotFoundCache = createCacheConfig(10, 5);

        // ════════════════════════════════════════════════════════════════════════════
        // REGISTER CACHES
        // ════════════════════════════════════════════════════════════════════════════

        Map<String, RedisCacheConfiguration> configs = new HashMap<>();
        configs.put("product_by_id", productByIdCache);
        configs.put("product_list", productListCache);
        configs.put("product_stock", productStockCache);
        configs.put("product_not_found", productNotFoundCache);

        // ════════════════════════════════════════════════════════════════════════════

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configs)
                .build();
    }
}
