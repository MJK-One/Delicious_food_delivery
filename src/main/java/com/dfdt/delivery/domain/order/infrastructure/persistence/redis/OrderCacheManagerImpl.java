package com.dfdt.delivery.domain.order.infrastructure.persistence.redis;

import com.dfdt.delivery.domain.order.domain.repository.OrderCacheManager;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.UUID;

// 사용X
@Repository
public class OrderCacheManagerImpl implements OrderCacheManager {

    @Override
    public void setPaymentTimeout(UUID orderId, Duration timeout) {

    }
}