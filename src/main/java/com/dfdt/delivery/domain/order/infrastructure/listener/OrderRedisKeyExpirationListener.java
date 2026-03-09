package com.dfdt.delivery.domain.order.infrastructure.listener;

import com.dfdt.delivery.domain.order.domain.enums.OrderRedisPrefix;
import com.dfdt.delivery.domain.order.domain.event.OrderEvent;
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
public class OrderRedisKeyExpirationListener extends KeyExpirationEventMessageListener {
    private final ApplicationEventPublisher applicationEventPublisher;

    public OrderRedisKeyExpirationListener
            (@Qualifier("redisMessageListenerContainer") RedisMessageListenerContainer listenerContainer,
             ApplicationEventPublisher applicationEventPublisher) {
        super(listenerContainer);
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Redis 만료 이벤트 발생할 때 실행되는 콜백 함수
     *
     * @param message redis key
     * @param pattern __keyevent@*__:expired
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        // 키 종류 확인
        OrderRedisPrefix prefix = OrderRedisPrefix.fromKey(expiredKey);
        if (prefix == null) return;

        // 주문 아이디 추출
        UUID orderId = prefix.parseId(expiredKey);
        log.info("Expired key: {}", expiredKey);

        // 각 상황에 맞는 이벤트 발행
        switch (prefix) {
            case ORDER_PENDING -> {
                applicationEventPublisher.publishEvent(new OrderEvent.PaymentTimeout(orderId));
            }
            case OWNER_CONFIRM -> {
                applicationEventPublisher.publishEvent(new OrderEvent.AcceptanceTimeout(orderId));
            }
        }
    }
}