package com.dfdt.delivery.domain.payment.presentation.dto.request;

import com.dfdt.delivery.domain.payment.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCreateReqDto {

    @NotNull(message = "주문 ID가 입력되지 않았습니다.")
    private UUID orderId;

    @NotNull(message = "결제 수단이 입력되지 않았습니다.")
    private PaymentMethod paymentMethod;

    @NotNull(message = "결제 금액이 입력되지 않았습니다.")
    private Integer amount;
}
