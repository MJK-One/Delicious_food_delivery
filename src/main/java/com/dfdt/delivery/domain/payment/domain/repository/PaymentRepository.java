package com.dfdt.delivery.domain.payment.domain.repository;

import com.dfdt.delivery.domain.payment.domain.entity.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends PaymentCustomRepository {
    Payment save(Payment payment);
    Optional<Payment> findByOrderId(UUID orderId);
    Optional<Payment> findById(UUID paymentId);
}
