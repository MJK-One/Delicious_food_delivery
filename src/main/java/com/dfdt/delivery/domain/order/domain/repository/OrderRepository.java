package com.dfdt.delivery.domain.order.domain.repository;

import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.payment.domain.entity.Payment;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID orderId);
    Optional<Order> findByIdWithLock(UUID orderId);
    Optional<Payment> findPaymentOrderId(UUID orderId);
}