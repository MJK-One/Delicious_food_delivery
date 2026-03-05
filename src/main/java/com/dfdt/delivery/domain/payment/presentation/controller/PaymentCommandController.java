package com.dfdt.delivery.domain.payment.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.payment.application.service.command.PaymentCommandService;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentApproveReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentDetailResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentHiddenToggleResDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentCommandController {

    private final PaymentCommandService paymentCommandService;

    // 2. 결제 승인
    @PostMapping("/{paymentId}/approve")
    public ResponseEntity<ApiResponseDto<PaymentDetailResDto>> approvePayment(
            @PathVariable UUID paymentId,
            @Valid @RequestBody PaymentApproveReqDto reqDto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        PaymentDetailResDto response = paymentCommandService.approvePayment(
                paymentId, reqDto, customUserDetails.getUsername()
        );
        return ApiResponseDto.success(200, "결제가 승인되었습니다.", response);
    }

    // 3. 결제 취소
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<ApiResponseDto<PaymentDetailResDto>> cancelPayment(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        PaymentDetailResDto response = paymentCommandService.cancelPayment(paymentId, customUserDetails.getUsername());
        return ApiResponseDto.success(200, "결제가 취소되었습니다.", response);
    }

    // 4. 결제 삭제
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<ApiResponseDto<Void>> deletePayment(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        paymentCommandService.deletePayment(paymentId, customUserDetails.getUsername());
        return ApiResponseDto.success(200, "결제가 삭제되었습니다.", null);
    }

    // 5. 결제 숨김 토글
    @PostMapping("/{paymentId}/hidden")
    public ResponseEntity<ApiResponseDto<PaymentHiddenToggleResDto>> toggleHidden(
            @PathVariable UUID paymentId,
            @RequestParam Boolean isHidden,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        PaymentHiddenToggleResDto response = paymentCommandService.toggleHidden(paymentId, isHidden, customUserDetails.getUsername());
        String msg = isHidden ? "결제 내역이 숨김 처리되었습니다." : "결제 내역 숨김이 해제되었습니다.";
        return ApiResponseDto.success(200, msg, response);
    }
}
