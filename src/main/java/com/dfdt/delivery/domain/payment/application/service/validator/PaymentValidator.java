package com.dfdt.delivery.domain.payment.application.service.validator;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.payment.domain.entity.Payment;
import com.dfdt.delivery.domain.payment.domain.enums.PaymentErrorCode;
import com.dfdt.delivery.domain.payment.domain.enums.PaymentStatus;
import com.dfdt.delivery.domain.order.domain.enums.OrderStatus;
import org.springframework.stereotype.Component;

@Component
public class PaymentValidator {

    /**
     * 결제 승인 가능 여부 검증 (상태가 READY여야 함)
     */
    public void validateApproveCondition(Payment payment) {
        if (payment.getPaymentStatus() != PaymentStatus.READY) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }
    }

    /**
     * 결제 취소 가능 여부 검증 (이미 취소되었거나 실패한 상태가 아니어야 함)
     */
    public void validateCancelStatus(Payment payment) {
        if (payment.getPaymentStatus() == PaymentStatus.CANCELED || payment.getPaymentStatus() == PaymentStatus.FAILED) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }
    }

    /**
     * 주문 상태 기반 취소 가능 여부 검증 (PENDING 또는 PAID 상태에서만 취소 가능)
     */
    public void validateOrderCancelable(Order order) {
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }
    }

    /**
     * 결제 내역 접근 권한 검증 (주문 소유자 본인 확인)
     */
    public void validateOwnership(Order order, String username) {
        if (!order.getUser().getUsername().equals(username)) {
            throw new BusinessException(PaymentErrorCode.ACCESS_DENIED);
        }
    }
}
