package com.dfdt.delivery.common.enums;

import lombok.*;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    /** 공통 에러 **/

    /** 인증/인가 관련 **/
    // 400 BAD REQUEST
    REFRESH_TOKEN_REQUIRED(400, "AUTH-4001", "Refresh Token이 제공되지 않았습니다."),
    INVALID_AUTH_REQUEST(400, "AUTH-4002", "잘못된 요청입니다. (로그아웃 처리되었거나 토큰 정보 없음)"),

    // 401 UNAUTHORIZED
    UNAUTHORIZED(401, "AUTH-4010", "인증이 필요합니다."),
    LOGIN_FAILED(401, "AUTH-4011", "아이디 또는 비밀번호가 일치하지 않습니다."),
    TOKEN_VERSION_MISMATCH(401, "AUTH-4012", "권한 정보가 변경되어 재로그인이 필요합니다."),
    INVALID_REFRESH_TOKEN(401, "AUTH-4015", "유효하지 않거나 만료된 Refresh Token입니다."),

    // 403 FORBIDDEN
    FORBIDDEN(403, "AUTH-4030", "접근 권한이 없습니다."),
    WITHDRAWN_ACCOUNT(403, "AUTH-4031", "탈퇴 처리된 계정입니다.");

    private final int status;
    private final String code;
    private final String message;
}
