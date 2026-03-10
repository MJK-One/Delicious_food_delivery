package com.dfdt.delivery.domain.payment.domain.event;

import java.util.UUID;

/**
 * 결제 관련 Redis TTL 만료 이벤트를 모아두는 클래스
 */
public class PaymentEvent {
    public record PaymentTimeout(UUID orderId) {
    }
}
