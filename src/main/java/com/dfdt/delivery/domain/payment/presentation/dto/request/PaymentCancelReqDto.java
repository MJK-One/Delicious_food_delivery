package com.dfdt.delivery.domain.payment.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentCancelReqDto {

    @NotBlank(message = "취소 사유가 입력되지 않았습니다.")
    @Size(max = 255)
    private String reason;
}