package com.dfdt.delivery.domain.payment.presentation.dto.response;

import com.dfdt.delivery.domain.payment.domain.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@AllArgsConstructor
@Builder
public class PaymentDetailResDto {

    private UUID paymentId;

    private UUID orderId;

    private PaymentStatus status;

    private Long amount;

    private String pgProvider;

    private String pgTransactionId;

    private String failureReason;

    private OffsetDateTime createdAt;

    private OffsetDateTime paidAt;

    private OffsetDateTime failedAt;

    private OffsetDateTime canceledAt;

    private Boolean hidden;

    private OffsetDateTime hiddenAt;
}