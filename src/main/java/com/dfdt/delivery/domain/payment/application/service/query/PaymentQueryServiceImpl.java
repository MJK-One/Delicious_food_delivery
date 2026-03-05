package com.dfdt.delivery.domain.payment.application.service.query;

import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentHistorySearchReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentListSearchReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentDetailResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentHistoryResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentListItemResDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentQueryServiceImpl implements PaymentQueryService {

    @Override
    public PaymentDetailResDto getPayment(UUID paymentId) {
        // TODO: 단건 조회
        return null;
    }

    @Override
    public Page<PaymentListItemResDto> listPayments(PaymentListSearchReqDto reqDto, Pageable pageable) {
        // TODO: 목록 조회 + 권한 필터링 + statusAt 계산
        return null;
    }

    @Override
    public Page<PaymentHistoryResDto> listPaymentHistory(PaymentHistorySearchReqDto reqDto, Pageable pageable) {
        // TODO: 히스토리 검색
        return null;
    }
}