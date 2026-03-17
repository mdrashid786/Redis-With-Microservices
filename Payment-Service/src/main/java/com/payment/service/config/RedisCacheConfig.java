package com.payment.service.config;


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
 * 🔥 PAYMENT SERVICE - REDIS CACHE CONFIGURATION
 *
 * Cache Names (Match @Cacheable value):
 * ✓ payment (1 day + 0-1 hour random)
 * ✓ payment_status (2 hours + 0-30 min random)
 * ✓ idempotency (1 day + 0-1 hour random)
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
        // PAYMENT SERVICE SPECIFIC CACHES
        // ════════════════════════════════════════════════════════════════════════════

        /**
         * payment: 1 day + 0-1 hour random
         * Used by: @Cacheable(value = "payment", key = "#id")
         *
         * Why 1 day?
         * - Payment records rarely change
         * - User might check payment receipt within 1 day
         * - Safe to cache for long period
         * - Reference data, not critical
         */
        RedisCacheConfiguration paymentCache = createCacheConfig(86400, 3600);

        /**
         * payment_status: 2 hours + 0-30 minutes random
         * Used by: Custom caching of payment status
         *
         * Why 2 hours?
         * - Payment status changes frequently (PENDING → SUCCESS/FAILED)
         * - Need fresher data than full payment record
         * - User checks within 2 hours
         */
        RedisCacheConfiguration paymentStatusCache = createCacheConfig(7200, 1800);

        /**
         * idempotency: 1 day + 0-1 hour random
         * Used by: @Cacheable(value = "idempotency", key = "#idempotencyKey")
         *
         * Why 1 day?
         * - CRITICAL: Prevent double charging
         * - User might retry payment within 1 day
         * - Must cache idempotency check result for full day
         * - If key expires, user can be charged twice!
         */
        RedisCacheConfiguration idempotencyCache = createCacheConfig(86400, 3600);

        // ════════════════════════════════════════════════════════════════════════════

        Map<String, RedisCacheConfiguration> configs = new HashMap<>();
        configs.put("payment", paymentCache);
        configs.put("payment_status", paymentStatusCache);
        configs.put("idempotency", idempotencyCache);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configs)
                .build();
    }
}
