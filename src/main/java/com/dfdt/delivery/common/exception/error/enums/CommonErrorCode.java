package com.dfdt.delivery.common.exception.error.enums;

import com.dfdt.delivery.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INTERNAL_SERVER_ERROR(500,"SERVER-ERROR","내부 서버 오류"),
    INVALID_INPUT_VALUE(400,"INVALID_REQUEST","@Valid 형식 에러"),
    METHOD_NOT_ALLOWED(405, "METHOD_NOT_ALLOWED", "허용되지 않는 METHOD 입니다");
    private final int status;
    private final String errorCode;
    private final String message;

}