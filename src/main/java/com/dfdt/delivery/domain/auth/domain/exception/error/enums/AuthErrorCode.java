package com.dfdt.delivery.domain.auth.domain.exception.error.enums;

import com.dfdt.delivery.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    // 401 UNAUTHORIZED
    LOGIN_FAILED(401, "AUTH-4011", "아이디 또는 비밀번호가 일치하지 않습니다."),
    TOKEN_VERSION_MISMATCH(401, "AUTH-4012", "권한 정보가 변경되어 재로그인이 필요합니다."),
    EXPIRED_ACCESS_TOKEN(401, "AUTH-4013", "만료된 Access Token입니다."),
    INVALID_ACCESS_TOKEN(401, "AUTH-4014", "유효하지 않거나 손상된 Access Token입니다."),
    INVALID_REFRESH_TOKEN(401, "AUTH-4015", "유효하지 않거나 만료된 Refresh Token입니다."),

    // 403 FORBIDDEN
    FORBIDDEN(403, "AUTH-4030", "접근 권한이 없습니다.");

    private final int status;
    private final String errorCode;
    private final String message;
}