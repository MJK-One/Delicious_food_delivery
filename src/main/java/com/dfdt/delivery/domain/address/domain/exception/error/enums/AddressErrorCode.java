package com.dfdt.delivery.domain.address.domain.exception.error.enums;

import com.dfdt.delivery.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AddressErrorCode implements ErrorCode {
    ADDRESS_NOT_FOUND(404, "ADDRESS-001", "배송지를 찾을 수 없습니다."),
    ADDRESS_ACCESS_DENIED(403, "ADDRESS-002", "배송지에 대한 권한이 없습니다.");

    private final int status;
    private final String errorCode;
    private final String message;
}