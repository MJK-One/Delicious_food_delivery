package com.dfdt.delivery.domain.payment.infrastructure.persistence.repository;

import com.dfdt.delivery.domain.payment.domain.entity.Payment;
import com.dfdt.delivery.domain.payment.domain.repository.PaymentRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaPaymentRepository
        extends JpaRepository<Payment, UUID>, PaymentRepository {

}