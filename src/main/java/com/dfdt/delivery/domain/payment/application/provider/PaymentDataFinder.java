package com.dfdt.delivery.domain.payment.application.provider;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.payment.domain.entity.Payment;
import com.dfdt.delivery.domain.payment.domain.enums.PaymentErrorCode;
import com.dfdt.delivery.domain.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentDataFinder {

    private final PaymentRepository paymentRepository;

    public Payment findPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }

    public Payment findPaymentWithLock(UUID paymentId) {
        return paymentRepository.findByIdWithLock(paymentId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }

    public Payment findPaymentByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }

    public Payment findPaymentByOrderIdWithLock(UUID orderId) {
        return paymentRepository.findByOrderIdWithLock(orderId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }
}
