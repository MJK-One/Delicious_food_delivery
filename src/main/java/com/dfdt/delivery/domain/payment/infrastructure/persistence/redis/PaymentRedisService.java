package com.dfdt.delivery.domain.payment.infrastructure.persistence.redis;

import java.util.UUID;

public interface PaymentRedisService {
    void setPaymentTimeout(UUID orderId);
    void cancelPaymentTimeout(UUID orderId);
}
