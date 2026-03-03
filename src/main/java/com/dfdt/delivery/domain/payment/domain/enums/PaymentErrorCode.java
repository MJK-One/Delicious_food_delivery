package com.dfdt.delivery.domain.payment.domain.enums;

import com.dfdt.delivery.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

    // 400 Bad Request
    INVALID_PAYMENT_RESULT(
            HttpStatus.BAD_REQUEST,
            "PAY-4001",
            "결제 결과 값이 올바르지 않습니다."
    ),
    FAILURE_REASON_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "PAY-4002",
            "결제 실패 사유가 필요합니다."
    ),
    INVALID_CANCEL_REQUEST(
            HttpStatus.BAD_REQUEST,
            "PAY-4003",
            "결제 취소 요청이 올바르지 않습니다."
    ),
    INVALID_SEARCH_CONDITION(
            HttpStatus.BAD_REQUEST,
            "PAY-4004",
            "요청 파라미터가 올바르지 않습니다."
    ),
    INVALID_PAYMENT_STATUS(
            HttpStatus.BAD_REQUEST,
            "PAY-4005",
            "결제 상태 값이 올바르지 않습니다."
    ),

    // 401 Unauthorized
    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "PAY-4010",
            "인증이 필요합니다."
    ),

    // 403 Forbidden
    ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "PAY-4030",
            "해당 결제에 대한 권한이 없습니다."
    ),

    // 404 Not Found
    PAYMENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PAY-4041",
            "결제를 찾을 수 없습니다."
    ),
    PAYMENT_HISTORY_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PAY-4042",
            "결제 히스토리를 찾을 수 없습니다."
    ),

    // 409 Conflict
    ALREADY_PROCESSED(
            HttpStatus.CONFLICT,
            "PAY-4092",
            "이미 처리된 결제입니다."
    ),
    ALREADY_CANCELED(
            HttpStatus.CONFLICT,
            "PAY-4093",
            "이미 취소된 결제입니다."
    );

    private final HttpStatus status;
    private final String errorCode;
    private final String message;

    @Override
    public int getStatus() {
        return status.value();
    }
}