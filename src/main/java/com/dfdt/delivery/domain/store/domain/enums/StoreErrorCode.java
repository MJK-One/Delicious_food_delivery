package com.dfdt.delivery.domain.store.domain.enums;

import com.dfdt.delivery.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StoreErrorCode implements ErrorCode {

    // 400 BAD REQUEST
    INVALID_REQUEST(400, "STORE-4000", "요청 형식이 올바르지 않습니다."),
    STATUS_NOT_MODIFIED(400, "STORE-4001", "변경값과 이전값이 동일합니다."),

    // 404 NOT FOUND
    NOT_FOUND_STORE(404, "STORE-4040", "해당 가게가 존재하지 않습니다."),
    NOT_FOUND_STORES(404, "STORES-4040", "등록된 가게가 없습니다.");

    private final int status;
    private final String errorCode;
    private final String message;
}