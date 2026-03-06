package com.dfdt.delivery.domain.payment.infrastructure.persistence.redis;

import com.dfdt.delivery.domain.payment.infrastructure.listener.PaymentTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class PaymentRedisConfig {

    @Bean
    public RedisMessageListenerContainer paymentRedisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            PaymentTimeoutHandler timeoutHandler) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(timeoutHandler, new PatternTopic("__keyevent@*__:expired"));

        return container;
    }
}
