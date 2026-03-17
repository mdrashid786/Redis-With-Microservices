package com.notification.service.config;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig extends CachingConfigurerSupport {


    // 👉 Redis down hoga
    // 👉 Exception swallow ho jayegi
    // 👉 Method normally execute hoga
    // 👉 DB se data aayega
//    @Override
//    public CacheErrorHandler errorHandler() {
//        return new CacheErrorHandler() {
//
//            @Override
//            public void handleCacheGetError(RuntimeException exception,
//                                            Cache cache,
//                                            Object key) {
//                System.out.println("Redis GET failed, fallback to DB");
//            }
//
//            @Override
//            public void handleCachePutError(RuntimeException exception,
//                                            Cache cache,
//                                            Object key,
//                                            Object value) {
//                System.out.println("Redis PUT failed");
//            }
//
//            @Override
//            public void handleCacheEvictError(RuntimeException exception,
//                                              Cache cache,
//                                              Object key) {
//                System.out.println("Redis EVICT failed");
//            }
//
//            @Override
//            public void handleCacheClearError(RuntimeException exception,
//                                              Cache cache) {
//                System.out.println("Redis CLEAR failed");
//            }
//        };
//    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key serializer
        template.setKeySerializer(new StringRedisSerializer());

        // Value serializer (JSON)
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

}
