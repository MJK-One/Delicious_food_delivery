package com.dfdt.delivery.domain.user.domain.exception.error.enums;

import com.dfdt.delivery.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {

    // 400 BAD REQUEST
    INVALID_USERNAME_FORMAT(400, "USER-4001", "아이디 형식이 올바르지 않습니다."),
    INVALID_PASSWORD_FORMAT(400, "USER-4002", "비밀번호 형식이 올바르지 않습니다."),
    INVALID_ROLE_VALUE(400, "USER-4003", "유효하지 않은 권한 값입니다."),
    PASSWORD_MISMATCH(400, "USER-4004", "비밀번호가 일치하지 않습니다."),

    // 404 NOT FOUND
    USER_NOT_FOUND(404, "USER-4041", "존재하지 않는 사용자입니다."),

    // 409 CONFLICT
    DUPLICATE_USERNAME(409, "USER-4091", "이미 사용 중인 아이디입니다."),
    ALREADY_WITHDRAWN_USER(409, "USER-4092", "이미 탈퇴 처리된 사용자입니다.");

    private final int status;
    private final String errorCode;
    private final String message;
}