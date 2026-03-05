package com.dfdt.delivery.domain.order.domain.repository;

import com.dfdt.delivery.domain.order.domain.repository.OrderCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.time.Duration;
import java.util.UUID;

@Repository // 스프링이 이 클래스를 '실제 일꾼'으로 인식하게 합니다.
public class OrderCacheManagerImpl implements OrderCacheManager {

    private final RedisTemplate<String, Object> redisTemplate;

    public OrderCacheManagerImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void setPaymentTimeout(UUID orderId, Duration timeout) {
        // 우선 에러를 없애기 위해 비워두거나 아래처럼 간단히 작성하세요.
        String key = "order:timeout:" + orderId;
        redisTemplate.opsForValue().set(key, "PENDING", timeout);
    }
}