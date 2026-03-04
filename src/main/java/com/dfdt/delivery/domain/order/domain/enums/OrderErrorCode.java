package com.dfdt.delivery.domain.order.domain.enums;

import com.dfdt.delivery.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

//400 -> HttpClientErrorException.BadRequest
//401 -> HttpClientErrorException.Unauthorized
//403 -> HttpClientErrorException.Forbidden
//404 -> HttpClientErrorException.NotFound


@Getter
@AllArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    // 400 Bad Request
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "ORDER-4000", "주문 수량은 1개 이상이어야 합니다."),
    SHOP_MISMATCH(HttpStatus.BAD_REQUEST, "ORDER-4001", "해당 가게의 상품이 아닙니다."),
    ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "ORDER-4002", "이미 처리된 주문건입니다."),
    PAYMENT_REQUIRED(HttpStatus.BAD_REQUEST, "ORDER-4003", "사용자가 결제를 하지 않았습니다."),
    PRICE_CHANGED(HttpStatus.BAD_REQUEST, "ORDER-4004", "상품의 가격이 변동되었습니다. 다시 확인해 주세요."),
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "ORDER-4005", "상품이 없거나 재고가 부족합니다."),

    // 401 Unauthorized / 403 Forbidden
    ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "ORDER-4010", "해당 주문에 대해 접근할 수 있는 권한이 없습니다."),
    PRODUCT_NOT_FOR_SALE(HttpStatus.FORBIDDEN, "ORDER-4031", "현재 판매 중이 아닌 상품이 포함되어 있습니다."),

    // 404 Not Found
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER-4040", "주문을 찾을 수 없습니다.");


    private final HttpStatus status;

    public int getStatus() {
        return this.status.value();
    }

    private final String errorCode;
    private final String message;

}