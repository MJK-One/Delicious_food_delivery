package com.dfdt.delivery.domain.payment.infrastructure.listener;

import com.dfdt.delivery.domain.payment.domain.enums.PaymentRedisPrefix;
import com.dfdt.delivery.domain.payment.domain.event.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class PaymentRedisKeyExpirationListener extends KeyExpirationEventMessageListener {
    private final ApplicationEventPublisher applicationEventPublisher;

    public PaymentRedisKeyExpirationListener(
            @Qualifier("redisMessageListenerContainer") RedisMessageListenerContainer listenerContainer,
            ApplicationEventPublisher applicationEventPublisher) {
        super(listenerContainer);
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        PaymentRedisPrefix prefix = PaymentRedisPrefix.fromKey(expiredKey);
        
        if (prefix == PaymentRedisPrefix.PAYMENT_TIMEOUT) {
            UUID orderId = prefix.parseId(expiredKey);
            log.info("Payment timeout detected for orderId: {}", orderId);
            applicationEventPublisher.publishEvent(new PaymentEvent.PaymentTimeout(orderId));
        }
    }
}
