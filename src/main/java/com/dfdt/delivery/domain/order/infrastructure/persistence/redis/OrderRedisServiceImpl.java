package com.dfdt.delivery.domain.order.infrastructure.persistence.redis;

import com.dfdt.delivery.domain.order.domain.enums.OrderRedisPrefix;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderRedisServiceImpl implements OrderRedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    // TTL 설정
    @Override
    public void setPaymentTimeout(UUID orderId) {
        String redisKey = OrderRedisPrefix.ORDER_PENDING.generateKey(orderId);
        redisTemplate.opsForValue().set(
                redisKey,
                "waiting",
                OrderRedisPrefix.ORDER_PENDING.getTtl()
        );
    }
    @Override
    public void setAcceptTimeout(UUID orderId) {
        String redisKey = OrderRedisPrefix.OWNER_CONFIRM.generateKey(orderId);
        redisTemplate.opsForValue().set(
                redisKey,
                "waiting",
                OrderRedisPrefix.OWNER_CONFIRM.getTtl()
        );
    }
    @Override
    // 키 삭제하기
    public void cancelTimeOut(UUID orderId) {
        Arrays.stream(OrderRedisPrefix.values())
                .forEach(prefix -> removeKey(prefix, orderId));
    }
    private void removeKey(OrderRedisPrefix prefix, Object id) {
        String key = prefix.generateKey(id);
        redisTemplate.delete(key);
    }
}
