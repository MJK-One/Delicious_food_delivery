package com.dfdt.delivery.domain.payment.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.payment.application.service.command.PaymentCommandService;
import com.dfdt.delivery.domain.payment.application.service.query.PaymentQueryService;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentApproveReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentHistorySearchReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentListSearchReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentDetailResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentHiddenToggleResDto;

import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentHistoryResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentListItemResDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;

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

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<ApiResponseDto<PaymentDetailResDto>> cancelPayment(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        PaymentDetailResDto response = paymentCommandService.cancelPayment(paymentId, customUserDetails.getUsername());
        return ApiResponseDto.success(200, "결제가 취소되었습니다.", response);
    }

    @PreAuthorize("hasRole('MASTER')")
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<ApiResponseDto<Void>> deletePayment(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        paymentCommandService.deletePayment(paymentId, customUserDetails.getUsername());
        return ApiResponseDto.success(200, "결제가 삭제되었습니다.", null);
    }

    @PostMapping("/{paymentId}/hidden")
    public ResponseEntity<ApiResponseDto<PaymentHiddenToggleResDto>> toggleHidden(
            @PathVariable UUID paymentId,
            @RequestParam Boolean isHidden,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        PaymentHiddenToggleResDto response = paymentCommandService.toggleHidden(paymentId, isHidden, customUserDetails.getUsername());
        String msg = isHidden ? "결제 내역이 숨김 처리되었습니다." : "결제 내역 숨김이 해제되었습니다.";
        return ApiResponseDto.success(200, msg, response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponseDto<PaymentDetailResDto>> getPayment(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        PaymentDetailResDto response = paymentQueryService.getPayment(
                paymentId,
                customUserDetails.getUsername(),
                customUserDetails.getRole()
        );
        return ApiResponseDto.success(200, "결제 조회가 완료되었습니다.", response);
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<PaymentListItemResDto>>> listPayments(
            PaymentListSearchReqDto reqDto,
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Page<PaymentListItemResDto> page = paymentQueryService.listPayments(
                reqDto,
                pageable,
                customUserDetails.getUsername(),
                customUserDetails.getRole()
        );
        return ApiResponseDto.success(200, "결제 목록 조회가 완료되었습니다.", page);
    }

    @PreAuthorize("hasRole('MASTER')")
    @GetMapping("/history")
    public ResponseEntity<ApiResponseDto<Page<PaymentHistoryResDto>>> listPaymentHistory(
            PaymentHistorySearchReqDto reqDto, Pageable pageable) {

        Page<PaymentHistoryResDto> page = paymentQueryService.listPaymentHistory(reqDto, pageable);
        return ApiResponseDto.success(200, "결제 히스토리 검색이 완료되었습니다.", page);
    }
}
