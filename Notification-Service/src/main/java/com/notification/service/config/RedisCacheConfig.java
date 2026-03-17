package com.notification.service.config;


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
 * 🔥 NOTIFICATION SERVICE - REDIS CACHE CONFIGURATION
 *
 * Cache Names (Match @Cacheable value):
 * ✓ notification (1 hour + 0-10 min random)
 * ✓ email_sent (12 hours + 0-1 hour random)
 * ✓ sms_sent (12 hours + 0-1 hour random)
 * ✓ notification_status (30 min + 0-5 min random)
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
        // NOTIFICATION SERVICE SPECIFIC CACHES
        // ════════════════════════════════════════════════════════════════════════════

        /**
         * notification: 1 hour + 0-10 minutes random
         * Used by: @Cacheable(value = "notification", key = "#orderId")
         *
         * Why 1 hour?
         * - User checks notification within first hour
         * - After 1 hour, probably read already
         * - Can query DB for older notifications
         */
        RedisCacheConfiguration notificationCache = createCacheConfig(3600, 600);

        /**
         * email_sent: 12 hours + 0-1 hour random
         * Used by: @Cacheable(value = "email_sent", key = "#orderId")
         *
         * Why 12 hours?
         * - Track if email was sent (prevent duplicates)
         * - User might ask "Did you send email?" within 12 hours
         * - Acceptable if stale after 12 hours (user can request resend)
         * - Fire-and-forget tracking
         */
        RedisCacheConfiguration emailSentCache = createCacheConfig(43200, 3600);

        /**
         * sms_sent: 12 hours + 0-1 hour random
         * Used by: @Cacheable(value = "sms_sent", key = "#orderId")
         *
         * Why 12 hours?
         * - Track if SMS was sent (prevent duplicates)
         * - Similar to email_sent
         */
        RedisCacheConfiguration smsSentCache = createCacheConfig(43200, 3600);

        /**
         * notification_status: 30 minutes + 0-5 minutes random
         * Used by: Custom caching of notification delivery status
         *
         * Why 30 min?
         * - Status changes: SENT → DELIVERED → READ
         * - Need fresher data
         * - User checks within 30 min
         */
        RedisCacheConfiguration notificationStatusCache = createCacheConfig(1800, 300);

        // ════════════════════════════════════════════════════════════════════════════

        Map<String, RedisCacheConfiguration> configs = new HashMap<>();
        configs.put("notification", notificationCache);
        configs.put("email_sent", emailSentCache);
        configs.put("sms_sent", smsSentCache);
        configs.put("notification_status", notificationStatusCache);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configs)
                .build();
    }
}
