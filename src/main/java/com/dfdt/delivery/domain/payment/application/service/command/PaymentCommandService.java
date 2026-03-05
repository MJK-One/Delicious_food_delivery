package com.dfdt.delivery.domain.payment.application.service.command;

import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentApproveReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentCreateReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentDetailResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentHiddenToggleResDto;

import java.util.UUID;

public interface PaymentCommandService {

    PaymentDetailResDto createPayment(PaymentCreateReqDto reqDto);

    PaymentDetailResDto approvePayment(UUID paymentId, PaymentApproveReqDto reqDto);

    PaymentDetailResDto cancelPayment(UUID paymentId);

    void deletePayment(UUID paymentId);

    PaymentHiddenToggleResDto toggleHidden(UUID paymentId, Boolean hidden);
}