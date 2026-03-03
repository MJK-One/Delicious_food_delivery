package com.dfdt.delivery.domain.payment.infrastructure.persistence.repository;

import com.dfdt.delivery.domain.payment.domain.entity.PaymentStatusHistory;
import com.dfdt.delivery.domain.payment.domain.repository.PaymentStatusHistoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaPaymentStatusHistoryRepository
        extends JpaRepository<PaymentStatusHistory, UUID>, PaymentStatusHistoryRepository {

}