package com.dfdt.delivery.domain.order.infrastructure.persistence.redis;


import java.time.Duration;
import java.util.UUID;

public interface OrderRedisService {
    void setPaymentTimeout(UUID orderId);
    void setAcceptTimeout(UUID orderId);
    void cancelTimeOut(UUID orderId);
}
