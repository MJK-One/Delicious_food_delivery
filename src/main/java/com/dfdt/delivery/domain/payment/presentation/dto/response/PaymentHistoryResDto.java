package com.dfdt.delivery.domain.payment.presentation.dto.response;

import com.dfdt.delivery.domain.payment.domain.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class PaymentHistoryResDto {

    private UUID paymentStatusHistoryId;

    private UUID paymentId;

    private UUID orderId;

    private String changedBy;

    private PaymentStatus fromStatus;

    private PaymentStatus toStatus;

    private String changeReason;

    private OffsetDateTime createdAt;
}