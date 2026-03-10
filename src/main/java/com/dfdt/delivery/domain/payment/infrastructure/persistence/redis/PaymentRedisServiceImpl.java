package com.dfdt.delivery.domain.payment.infrastructure.persistence.redis;

import com.dfdt.delivery.domain.payment.domain.enums.PaymentRedisPrefix;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentRedisServiceImpl implements PaymentRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void setPaymentTimeout(UUID orderId) {
        String redisKey = PaymentRedisPrefix.PAYMENT_TIMEOUT.generateKey(orderId);
        redisTemplate.opsForValue().set(
                redisKey,
                "waiting",
                PaymentRedisPrefix.PAYMENT_TIMEOUT.getTtl()
        );
    }

    @Override
    public void cancelPaymentTimeout(UUID orderId) {
        String redisKey = PaymentRedisPrefix.PAYMENT_TIMEOUT.generateKey(orderId);
        redisTemplate.delete(redisKey);
    }
}
