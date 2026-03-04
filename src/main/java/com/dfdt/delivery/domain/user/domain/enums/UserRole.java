package com.dfdt.delivery.domain.user.domain.enums;

import com.dfdt.delivery.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole implements ErrorCode {
    CUSTOMER("고객"),
    OWNER("가게 주인"),
    MASTER("전체 관리자");

    private final String message;

    @Override
    public int getStatus() {
        return 400; // Default for when it's used as an error (e.g. invalid role)
    }

    @Override
    public String getErrorCode() {
        return "USER-ROLE-" + name();
    }
}