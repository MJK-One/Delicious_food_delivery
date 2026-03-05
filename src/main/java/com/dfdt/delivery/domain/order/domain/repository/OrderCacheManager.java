package com.dfdt.delivery.domain.order.domain.repository;


import java.time.Duration;
import java.util.UUID;

public interface OrderCacheManager {
    void setPaymentTimeout(UUID orderId, Duration timeout);
}
