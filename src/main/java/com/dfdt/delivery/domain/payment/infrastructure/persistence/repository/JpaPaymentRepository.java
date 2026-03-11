package com.dfdt.delivery.domain.payment.infrastructure.persistence.repository;

import com.dfdt.delivery.domain.payment.domain.entity.Payment;
import com.dfdt.delivery.domain.payment.domain.repository.PaymentRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaPaymentRepository
        extends JpaRepository<Payment, UUID>, PaymentRepository {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.paymentId = :paymentId")
    Optional<Payment> findByIdWithLock(@Param("paymentId") UUID paymentId);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.orderId = :orderId")
    Optional<Payment> findByOrderIdWithLock(@Param("orderId") UUID orderId);
}