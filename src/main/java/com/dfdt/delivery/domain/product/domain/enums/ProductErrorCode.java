package com.dfdt.delivery.domain.product.domain.enums;

import com.dfdt.delivery.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    // 400 BAD REQUEST
    INVALID_REQUEST(400, "PRODUCT-4000", "요청 형식이 올바르지 않습니다."),
    DIFFERENT_STORE_MENU(400, "PRODUCT-4001", "해당 가게의 메뉴가 아닙니다."),

    // 404 NOT FOUND
    NOT_FOUND_PRODUCT(404, "PRODUCT-4040", "해당 메뉴가 존재하지 않습니다."),
    NOT_FOUND_PRODUCTS(404, "PRODUCTS-4040", "등록된 메뉴가 없습니다.");


    private final int status;
    private final String errorCode;
    private final String message;
}
