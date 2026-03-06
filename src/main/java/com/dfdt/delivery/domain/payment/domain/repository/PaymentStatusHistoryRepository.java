package com.dfdt.delivery.domain.payment.domain.repository;

import com.dfdt.delivery.domain.payment.domain.entity.PaymentStatusHistory;

public interface PaymentStatusHistoryRepository {
    PaymentStatusHistory save(PaymentStatusHistory history);
}
