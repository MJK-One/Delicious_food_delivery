package com.dfdt.delivery.domain.region.domain.enums;

import com.dfdt.delivery.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RegionErrorCode implements ErrorCode {

    // 404 NOT FOUND
    NOT_FOUND_REGION(404, "REGION-4040", "해당 Region이 존재하지 않습니다.");

    private final int status;
    private final String errorCode;
    private final String message;
}