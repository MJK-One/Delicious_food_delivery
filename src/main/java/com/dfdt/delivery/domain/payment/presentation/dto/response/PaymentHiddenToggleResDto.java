package com.dfdt.delivery.domain.payment.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class PaymentHiddenToggleResDto {

    private UUID paymentId;

    private OffsetDateTime hiddenAt;

    private String hiddenBy;
}