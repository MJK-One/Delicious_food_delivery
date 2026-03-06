package com.dfdt.delivery.domain.payment.application.converter;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.UpdateAudit;
import com.dfdt.delivery.domain.payment.domain.entity.Payment;
import com.dfdt.delivery.domain.payment.domain.enums.PaymentStatus;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentCreateReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentDetailResDto;

import com.dfdt.delivery.domain.payment.domain.entity.PaymentStatusHistory;

public class PaymentConverter {

    public static Payment toEntity(PaymentCreateReqDto reqDto, String username) {
        return Payment.builder()
                .orderId(reqDto.getOrderId())
                .paymentMethod(reqDto.getPaymentMethod())
                .paymentStatus(PaymentStatus.READY)
                .amount(reqDto.getAmount())
                .createAudit(CreateAudit.now(username))
                .updateAudit(UpdateAudit.empty())
                .softDeleteAudit(SoftDeleteAudit.active())
                .build();
    }

    public static PaymentDetailResDto toDetailResDto(Payment payment) {
        return PaymentDetailResDto.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .status(payment.getPaymentStatus())
                .amount(payment.getAmount().longValue())
                .createdAt(payment.getCreateAudit().getCreatedAt())
                .paidAt(payment.getPaidAt())
                .failedAt(payment.getFailedAt())
                .canceledAt(payment.getCanceledAt())
                .failureReason(payment.getFailureReason())
                .hidden(payment.isHidden())
                .hiddenAt(payment.getHiddenAt())
                .build();
    }

    public static PaymentStatusHistory toStatusHistoryEntity(
            Payment payment,
            String username,
            PaymentStatus from,
            PaymentStatus to,
            String reason
    ) {
        return PaymentStatusHistory.create(
                payment.getPaymentId(),
                payment.getOrderId(),
                username,
                from,
                to,
                reason
        );
    }
}
