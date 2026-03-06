package com.dfdt.delivery.domain.payment.application.service.command;

import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentApproveReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentCreateReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentDetailResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentHiddenToggleResDto;

import java.util.UUID;

public interface PaymentCommandService {

    PaymentDetailResDto createPayment(PaymentCreateReqDto reqDto, String username);

    PaymentDetailResDto approvePayment(UUID paymentId, PaymentApproveReqDto reqDto, String username);

    PaymentDetailResDto cancelPayment(UUID paymentId, String username);

    void deletePayment(UUID paymentId, String username);

    PaymentHiddenToggleResDto toggleHidden(UUID paymentId, Boolean hidden, String username);

    void timeoutPayment(UUID orderId);
}