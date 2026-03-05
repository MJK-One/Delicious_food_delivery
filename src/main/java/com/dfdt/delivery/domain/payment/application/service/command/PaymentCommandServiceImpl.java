package com.dfdt.delivery.domain.payment.application.service.command;

import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentApproveReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentCreateReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentDetailResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentHiddenToggleResDto;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentCommandServiceImpl implements PaymentCommandService {

    @Override
    public PaymentDetailResDto createPayment(PaymentCreateReqDto reqDto) {
        // TODO: 결제 생성
        return null;
    }

    @Override
    public PaymentDetailResDto approvePayment(UUID paymentId, PaymentApproveReqDto reqDto) {
        // TODO: 결제 승인
        return null;
    }

    @Override
    public PaymentDetailResDto cancelPayment(UUID paymentId) {
        // TODO: 결제 취소
        return null;
    }

    @Override
    public void deletePayment(UUID paymentId) {
        // TODO: 결제 삭제
    }

    @Override
    public PaymentHiddenToggleResDto toggleHidden(UUID paymentId, Boolean hidden) {
        // TODO: 숨김 토글
        return null;
    }
}