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
public class PaymentListItemResDto {

    private UUID paymentId;

    private UUID orderId;

    private PaymentStatus status;

    /**
     * 현재 상태가 확정된 시각
     * READY  -> createdAt
     * PAID   -> paidAt
     * FAILED -> failedAt
     * CANCELED -> canceledAt
     */
    private OffsetDateTime statusAt;

    private Long amount;

    private Boolean isHidden;

}