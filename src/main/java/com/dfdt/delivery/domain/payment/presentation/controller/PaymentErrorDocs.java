package com.dfdt.delivery.domain.payment.presentation.controller;

public class PaymentErrorDocs {
    // --- 400 Bad Request ---
    public static final String INVALID_PAYMENT_RESULT = """
        { "status": 400, "code": "PAY-4001", "message": "결제 결과 값이 올바르지 않습니다." }""";

    public static final String FAILURE_REASON_REQUIRED = """
        { "status": 400, "code": "PAY-4002", "message": "결제 실패 사유가 필요합니다." }""";

    public static final String INVALID_CANCEL_REQUEST = """
        { "status": 400, "code": "PAY-4003", "message": "결제 취소 요청이 올바르지 않습니다." }""";

    public static final String INVALID_SEARCH_CONDITION = """
        { "status": 400, "code": "PAY-4004", "message": "요청 파라미터가 올바르지 않습니다." }""";

    public static final String INVALID_PAYMENT_STATUS = """
        { "status": 400, "code": "PAY-4005", "message": "결제 상태 값이 올바르지 않습니다." }""";

    // --- 401 Unauthorized ---
    public static final String UNAUTHORIZED = """
        { "status": 401, "code": "PAY-4010", "message": "인증이 필요합니다." }""";

    // --- 403 Forbidden ---
    public static final String ACCESS_DENIED = """
        { "status": 403, "code": "PAY-4030", "message": "해당 결제에 대한 권한이 없습니다." }""";

    // --- 404 Not Found ---
    public static final String PAYMENT_NOT_FOUND = """
        { "status": 404, "code": "PAY-4041", "message": "결제를 찾을 수 없습니다." }""";

    public static final String PAYMENT_HISTORY_NOT_FOUND = """
        { "status": 404, "code": "PAY-4042", "message": "결제 히스토리를 찾을 수 없습니다." }""";

    // --- 409 Conflict ---
    public static final String ALREADY_PROCESSED = """
        { "status": 409, "code": "PAY-4092", "message": "이미 처리된 결제입니다." }""";

    public static final String ALREADY_CANCELED = """
        { "status": 409, "code": "PAY-4093", "message": "이미 취소된 결제입니다." }""";
}
