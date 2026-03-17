package com.order.service.config;


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
 * 🔥 ORDER SERVICE - REDIS CACHE CONFIGURATION
 *
 * Cache Names (Match @Cacheable value):
 * ✓ order (1 hour + 0-10 min random)
 * ✓ user_orders (1 hour + 0-10 min random)
 * ✓ order_status (30 min + 0-5 min random)
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    private static final Random RANDOM = new Random();

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

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()
                        )
                );

        // ════════════════════════════════════════════════════════════════════════════
        // ORDER SERVICE SPECIFIC CACHES
        // ════════════════════════════════════════════════════════════════════════════

        /**
         * order: 1 hour + 0-10 minutes random
         * Used by: @Cacheable(value = "order", key = "#id")
         *
         * Why 1 hour?
         * - User checks order details within first hour
         * - Order status doesn't change frequently
         * - After 1 hour, user probably doesn't check again
         */
        RedisCacheConfiguration orderCache = createCacheConfig(3600, 600);

        /**
         * user_orders: 1 hour + 0-10 minutes random
         * Used by: @Cacheable(value = "user_orders", key = "#userId")
         *
         * Why 1 hour?
         * - User checks their order history frequently in first hour
         * - Acceptable if slightly stale (old orders, new orders take time to show)
         */
        RedisCacheConfiguration userOrdersCache = createCacheConfig(3600, 600);

        /**
         * order_status: 30 minutes + 0-5 minutes random
         * Used by: Custom caching of order status
         *
         * Why 30 min?
         * - Order status updates frequently (pending → confirmed → shipped)
         * - Need fresher data than full order
         * - When status changes, invalidate cache
         */
        RedisCacheConfiguration orderStatusCache = createCacheConfig(1800, 300);

        RedisCacheConfiguration userOrderCache = createCacheConfig(3600, 600);

        // ════════════════════════════════════════════════════════════════════════════

        Map<String, RedisCacheConfiguration> configs = new HashMap<>();
        configs.put("order", orderCache);
        configs.put("user_orders", userOrdersCache);
        configs.put("order_status", orderStatusCache);
        configs.put("order_by_id", userOrderCache);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configs)
                .build();
    }
}