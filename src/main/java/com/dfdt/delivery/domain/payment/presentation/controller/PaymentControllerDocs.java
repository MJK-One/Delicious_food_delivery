package com.dfdt.delivery.domain.payment.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentApproveReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentHistorySearchReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentListSearchReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentDetailResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentHiddenToggleResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentHistoryResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentListItemResDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "Payment (결제)", description = "결제 승인, 취소 및 내역 관리를 담당합니다.")
public interface PaymentControllerDocs {

    @Operation(summary = "API-001 결제 승인", description = "생성된 결제에 대해 최종 승인을 요청합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "결제 승인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 결제 요청", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "결제 결과 값 오류", value = PaymentErrorDocs.INVALID_PAYMENT_RESULT),
                    @ExampleObject(name = "실패 사유 누락", value = PaymentErrorDocs.FAILURE_REASON_REQUIRED)
            })),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "결제 접근 권한 없음", value = PaymentErrorDocs.ACCESS_DENIED)
            })),
            @ApiResponse(responseCode = "404", description = "찾을 수 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "결제를 찾을 수 없음", value = PaymentErrorDocs.PAYMENT_NOT_FOUND)
            })),
            @ApiResponse(responseCode = "409", description = "상태 충돌", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "이미 처리된 결제", value = PaymentErrorDocs.ALREADY_PROCESSED)
            }))
    })
    ResponseEntity<ApiResponseDto<PaymentDetailResDto>> approvePayment(
            @PathVariable UUID paymentId,
            @Valid @RequestBody PaymentApproveReqDto reqDto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails);

    @Operation(summary = "API-002 결제 취소", description = "승인된 결제를 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "결제 취소 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 취소 요청", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "취소 요청 오류", value = PaymentErrorDocs.INVALID_CANCEL_REQUEST)
            })),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "결제 접근 권한 없음", value = PaymentErrorDocs.ACCESS_DENIED)
            })),
            @ApiResponse(responseCode = "404", description = "찾을 수 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "결제를 찾을 수 없음", value = PaymentErrorDocs.PAYMENT_NOT_FOUND)
            })),
            @ApiResponse(responseCode = "409", description = "상태 충돌", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "이미 취소된 결제", value = PaymentErrorDocs.ALREADY_CANCELED)
            }))
    })
    ResponseEntity<ApiResponseDto<PaymentDetailResDto>> cancelPayment(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails);

    @Operation(summary = "API-003 결제 삭제", description = "결제를 삭제(Soft Delete)합니다. (MASTER 전용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "결제 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 부족"),
            @ApiResponse(responseCode = "404", description = "찾을 수 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "결제를 찾을 수 없음", value = PaymentErrorDocs.PAYMENT_NOT_FOUND)
            }))
    })
    ResponseEntity<ApiResponseDto<Void>> deletePayment(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails);

    @Operation(summary = "API-004 결제 내역 숨김/해제", description = "본인의 결제 내역을 목록에서 숨기거나 다시 표시합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "결제 접근 권한 없음", value = PaymentErrorDocs.ACCESS_DENIED)
            })),
            @ApiResponse(responseCode = "404", description = "찾을 수 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "결제를 찾을 수 없음", value = PaymentErrorDocs.PAYMENT_NOT_FOUND)
            }))
    })
    ResponseEntity<ApiResponseDto<PaymentHiddenToggleResDto>> toggleHidden(
            @PathVariable UUID paymentId,
            @RequestParam Boolean isHidden,
            @AuthenticationPrincipal CustomUserDetails customUserDetails);

    @Operation(summary = "API-005 결제 단건 조회", description = "결제 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "결제 접근 권한 없음", value = PaymentErrorDocs.ACCESS_DENIED)
            })),
            @ApiResponse(responseCode = "404", description = "찾을 수 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "결제를 찾을 수 없음", value = PaymentErrorDocs.PAYMENT_NOT_FOUND)
            }))
    })
    ResponseEntity<ApiResponseDto<PaymentDetailResDto>> getPayment(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails);

    @Operation(summary = "API-006 결제 목록 조회", description = "조건에 맞는 결제 목록을 페이징하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 검색 조건", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "검색 조건 오류", value = PaymentErrorDocs.INVALID_SEARCH_CONDITION)
            }))
    })
    ResponseEntity<ApiResponseDto<Page<PaymentListItemResDto>>> listPayments(
            PaymentListSearchReqDto reqDto,
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails customUserDetails);

    @Operation(summary = "API-007 결제 히스토리 검색", description = "전체 결제 변경 이력을 검색합니다. (MASTER 전용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 부족")
    })
    ResponseEntity<ApiResponseDto<Page<PaymentHistoryResDto>>> listPaymentHistory(
            PaymentHistorySearchReqDto reqDto, Pageable pageable);
}
