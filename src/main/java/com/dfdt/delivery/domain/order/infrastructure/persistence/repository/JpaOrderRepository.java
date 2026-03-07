package com.dfdt.delivery.domain.order.infrastructure.persistence.repository;

import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.payment.domain.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface JpaOrderRepository extends JpaRepository<Order, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.orderId = :orderId")
    Optional<Order> findByIdWithLock(UUID orderId);

    @Query("select p from Payment  p where  p.orderId = :orderId")
    Optional<Payment> findPaymentOrderId(UUID orderId);
}
