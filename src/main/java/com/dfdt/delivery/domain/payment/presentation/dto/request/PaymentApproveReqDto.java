package com.dfdt.delivery.domain.payment.presentation.dto.request;

import com.dfdt.delivery.domain.payment.domain.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentApproveReqDto {

    @NotNull(message = "결제 결과가 입력되지 않았습니다.")
    private PaymentStatus result; // PAID / FAILED

    @Size(max = 50)
    private String pgProvider;

    @Size(max = 100)
    private String pgTransactionId;

    @Size(max = 255)
    private String failureReason;
}
