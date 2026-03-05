package com.dfdt.delivery.domain.payment.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.payment.application.service.query.PaymentQueryService;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentHistorySearchReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentListSearchReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentDetailResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentHistoryResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentListItemResDto;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentQueryController {

    private final PaymentQueryService paymentQueryService;

    // 1. 결제 단건 조회
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponseDto<PaymentDetailResDto>> getPayment(@PathVariable UUID paymentId) {
        PaymentDetailResDto response = paymentQueryService.getPayment(paymentId);
        return ApiResponseDto.success(200, "결제 조회가 완료되었습니다.", response);
    }

    // 2. 결제 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<PaymentListItemResDto>>> listPayments(
            PaymentListSearchReqDto reqDto, Pageable pageable) {

        Page<PaymentListItemResDto> page = paymentQueryService.listPayments(reqDto, pageable);
        return ApiResponseDto.success(200, "결제 목록 조회가 완료되었습니다.", page);
    }

    // 3. 결제 히스토리 전체 검색
    @GetMapping("/history")
    public ResponseEntity<ApiResponseDto<Page<PaymentHistoryResDto>>> listPaymentHistory(
            PaymentHistorySearchReqDto reqDto, Pageable pageable) {

        Page<PaymentHistoryResDto> page = paymentQueryService.listPaymentHistory(reqDto, pageable);
        return ApiResponseDto.success(200, "결제 히스토리 검색이 완료되었습니다.", page);
    }
}