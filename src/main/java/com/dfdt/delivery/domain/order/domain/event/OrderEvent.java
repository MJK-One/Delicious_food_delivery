package com.dfdt.delivery.domain.order.domain.event;

import java.util.UUID;

/**
 * 주문 관련 Redis TTL 만료 이벤트를 모아두는 클래스
 */
public class OrderEvent {

    public record PaymentTimeout(UUID orderId) {
    }
    public record AcceptanceTimeout(UUID orderId) {
    }
}