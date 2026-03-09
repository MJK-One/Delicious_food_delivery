package com.dfdt.delivery.domain.ai.domain.enums;

import com.dfdt.delivery.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AiErrorCode implements ErrorCode {

    // 400 BAD REQUEST - 입력값 검증
    INVALID_INPUT(400, "AI-4001", "요청 형식이 올바르지 않습니다."),
    INPUT_PROMPT_TOO_LONG(400, "AI-4002", "inputPrompt는 최대 300자까지 입력 가능합니다."),
    INVALID_TONE(400, "AI-4003", "tone은 FRIENDLY, SALESY, INFORMATIVE 중 하나여야 합니다."),
    KEYWORDS_TOO_MANY(400, "AI-4004", "keywords는 최대 10개까지 입력 가능합니다."),
    KEYWORD_ITEM_TOO_LONG(400, "AI-4005", "keyword 항목은 최대 30자까지 입력 가능합니다."),
    PRODUCT_NAME_TOO_LONG(400, "AI-4006", "productName은 최대 120자까지 입력 가능합니다."),
    PRODUCT_IDENTIFIER_REQUIRED(400, "AI-4007", "productId 또는 productName 중 하나는 필수입니다."),
    INVALID_PAGE(400, "AI-4008", "page는 0 이상이어야 합니다."),
    INVALID_PAGE_SIZE(400, "AI-4009", "size는 10, 30, 50 중 하나여야 합니다."),
    INVALID_DATETIME_FORMAT(400, "AI-4010", "날짜/시간 형식이 올바르지 않습니다. (ISO 8601)"),
    INVALID_DATE_RANGE(400, "AI-4011", "fromDateTime은 toDateTime보다 이전이어야 합니다."),
    DATE_RANGE_EXCEEDED(400, "AI-4012", "조회 기간은 최대 90일까지 가능합니다."),
    INVALID_SORT_FIELD(400, "AI-4013", "허용되지 않는 정렬 기준입니다."),
    INVALID_UUID_FORMAT(400, "AI-4014", "UUID 형식이 올바르지 않습니다."),
    PROMPT_BLANK(400, "AI-4015", "prompt는 공백만으로 구성될 수 없습니다."),
    OVERRIDE_PROMPT_TOO_LONG(400, "AI-4016", "overrideInputPrompt는 최대 300자까지 입력 가능합니다."),
    INCLUDE_TEXT_REQUIRED(400, "AI-4017", "includeText가 true인 경우 text는 필수입니다."),
    IMAGE_TEXT_TOO_LONG(400, "AI-4018", "이미지 내 텍스트는 최대 50자까지 입력 가능합니다."),
    IMAGE_PROMPT_TOO_LONG(400, "AI-4019", "이미지 생성 prompt는 최대 500자까지 입력 가능합니다."),
    INVALID_TIMEOUT_MS(400, "AI-4020", "timeoutMs는 100 이상 10000 이하여야 합니다."),
    INVALID_REQUEST_TYPE(400, "AI-4021", "허용되지 않는 requestType입니다."),

    // 401 UNAUTHORIZED
    UNAUTHORIZED(401, "AI-4101", "인증이 필요합니다."),

    // 403 FORBIDDEN
    FORBIDDEN(403, "AI-4301", "접근 권한이 없습니다."),
    STORE_ACCESS_DENIED(403, "AI-4302", "해당 가게에 대한 접근 권한이 없습니다."),
    PRODUCT_ACCESS_DENIED(403, "AI-4303", "해당 상품에 대한 접근 권한이 없습니다."),

    // 404 NOT FOUND
    AI_LOG_NOT_FOUND(404, "AI-4401", "AI 로그가 존재하지 않습니다."),
    STORE_NOT_FOUND(404, "AI-4402", "가게가 존재하지 않습니다."),
    PRODUCT_NOT_FOUND(404, "AI-4403", "상품이 존재하지 않습니다."),

    // 409 CONFLICT - 상태 충돌
    ALREADY_APPLIED(409, "AI-4091", "이미 적용된 AI 로그입니다."),
    STORE_PRODUCT_MISMATCH(409, "AI-4092", "storeId와 productId가 일치하지 않습니다."),
    PRODUCT_ID_REQUIRED_FOR_APPLY(409, "AI-4093", "productId가 없는 미리보기 로그는 적용할 수 없습니다."),
    RETRY_NOT_SUPPORTED_TYPE(409, "AI-4094", "해당 requestType은 재실행을 지원하지 않습니다."),

    // 429 TOO MANY REQUESTS
    RATE_LIMIT_EXCEEDED(429, "AI-4291", "요청 횟수 한도를 초과했습니다. 잠시 후 다시 시도해 주세요."),

    // 500 INTERNAL SERVER ERROR
    INTERNAL_SERVER_ERROR(500, "AI-5001", "서버 내부 오류가 발생했습니다."),

    // 502 BAD GATEWAY - 외부 AI 호출 실패
    EXTERNAL_AI_CALL_FAILED(502, "AI-5021", "외부 AI API 호출에 실패했습니다."),
    EXTERNAL_AI_EMPTY_RESPONSE(502, "AI-5023", "외부 AI가 빈 응답을 반환했습니다."),

    // 504 GATEWAY TIMEOUT
    EXTERNAL_AI_TIMEOUT(504, "AI-5022", "외부 AI API 응답 시간이 초과되었습니다.");

    private final int status;
    private final String errorCode;
    private final String message;
}