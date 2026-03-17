package com.notification.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

// ================================================================
// REDIS CONFIGURATION
// ================================================================

@Configuration
public class RedisListenerConfig {

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container =
                new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // Subscribe to order.* events
        container.addMessageListener(
                new MessageListenerAdapter(new OrderEventListener()),
                new PatternTopic("order.*")
        );

        // Subscribe to payment.* events
        container.addMessageListener(
                new MessageListenerAdapter(new PaymentEventListener()),
                new PatternTopic("payment.*")
        );

        return container;
    }
}