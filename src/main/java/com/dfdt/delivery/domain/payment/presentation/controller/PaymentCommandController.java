package com.dfdt.delivery.domain.payment.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.payment.application.service.command.PaymentCommandService;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentApproveReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentCreateReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentDetailResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentHiddenToggleResDto;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentCommandController {

    private final PaymentCommandService paymentCommandService;

    // 1. 결제 생성
    @PostMapping
    public ResponseEntity<ApiResponseDto<PaymentDetailResDto>> createPayment(
            @RequestBody PaymentCreateReqDto reqDto) {

        PaymentDetailResDto response = paymentCommandService.createPayment(reqDto);
        return ApiResponseDto.success(201, "결제가 생성되었습니다.", response);
    }

    // 2. 결제 승인
    @PostMapping("/{paymentId}/approve")
    public ResponseEntity<ApiResponseDto<PaymentDetailResDto>> approvePayment(
            @PathVariable UUID paymentId,
            @RequestBody PaymentApproveReqDto reqDto) {

        PaymentDetailResDto response = paymentCommandService.approvePayment(paymentId, reqDto);
        return ApiResponseDto.success(200, "결제가 승인되었습니다.", response);
    }

    // 3. 결제 취소
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<ApiResponseDto<PaymentDetailResDto>> cancelPayment(
            @PathVariable UUID paymentId) {

        PaymentDetailResDto response = paymentCommandService.cancelPayment(paymentId);
        return ApiResponseDto.success(200, "결제가 취소되었습니다.", response);
    }

    // 4. 결제 삭제
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<ApiResponseDto<Void>> deletePayment(@PathVariable UUID paymentId) {
        paymentCommandService.deletePayment(paymentId);
        return ApiResponseDto.success(200, "결제가 삭제되었습니다.", null);
    }

    // 5. 결제 숨김 토글
    @PostMapping("/{paymentId}/hidden")
    public ResponseEntity<ApiResponseDto<PaymentHiddenToggleResDto>> toggleHidden(
            @PathVariable UUID paymentId,
            @RequestParam Boolean hidden) {

        PaymentHiddenToggleResDto response = paymentCommandService.toggleHidden(paymentId, hidden);
        String msg = hidden ? "결제 내역이 숨김 처리되었습니다." : "결제 내역 숨김이 해제되었습니다.";
        return ApiResponseDto.success(200, msg, response);
    }
}