package com.dfdt.delivery.domain.order.presentation.controller;

public class OrderErrorDocs {
    // --- 400 Bad Request ---
    public static final String INVALID_QUANTITY = """
        { "status": 400, "code": "ORDER-4000", "message": "주문 수량은 1개 이상이어야 합니다." }""";

    public static final String SHOP_MISMATCH = """
        { "status": 400, "code": "ORDER-4001", "message": "해당 가게의 상품이 아닙니다." }""";

    public static final String ALREADY_PROCESSED = """
        { "status": 400, "code": "ORDER-4002", "message": "이미 처리된 주문건입니다." }""";

    public static final String PAYMENT_REQUIRED = """
        { "status": 400, "code": "ORDER-4003", "message": "사용자가 결제를 하지 않았습니다." }""";

    public static final String PRICE_CHANGED = """
        { "status": 400, "code": "ORDER-4004", "message": "상품의 가격이 변동되었습니다. 다시 확인해 주세요." }""";

    public static final String OUT_OF_STOCK = """
        { "status": 400, "code": "ORDER-4005", "message": "재고가 부족합니다." }""";

    // --- 401 Unauthorized / 403 Forbidden ---
    public static final String ACCESS_DENIED = """
        { "status": 401, "code": "ORDER-4010", "message": "해당 주문에 대해 접근할 수 있는 권한이 없습니다." }""";

    public static final String PRODUCT_NOT_FOR_SALE = """
        { "status": 403, "code": "ORDER-4031", "message": "현재 판매 중이 아닌 상품이 포함되어 있습니다." }""";

    // --- 404 Not Found ---
    public static final String ORDER_NOT_FOUND = """
        { "status": 404, "code": "ORDER-4040", "message": "주문을 찾을 수 없습니다." }""";
}