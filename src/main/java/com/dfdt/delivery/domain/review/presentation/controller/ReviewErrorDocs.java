package com.dfdt.delivery.domain.review.presentation.controller;

public class ReviewErrorDocs {
    // --- 400 Bad Request ---
    public static final String INVALID_RATING = """
        { "status": 400, "code": "REV-4001", "message": "리뷰 평점 값이 올바르지 않습니다." }""";

    public static final String INVALID_ORDER_STATUS = """
        { "status": 400, "code": "REV-4002", "message": "완료된 주문에 대해서만 리뷰를 작성할 수 있습니다." }""";

    public static final String REVIEW_ALREADY_DELETED = """
        { "status": 400, "code": "REV-4004", "message": "이미 삭제된 리뷰입니다." }""";

    public static final String IMAGE_LIMIT_EXCEEDED = """
        { "status": 400, "code": "REV-4005", "message": "리뷰 이미지는 최대 5장까지 등록 가능합니다." }""";

    // --- 401 Unauthorized ---
    public static final String UNAUTHORIZED = """
        { "status": 401, "code": "REV-4010", "message": "인증이 필요합니다." }""";

    // --- 403 Forbidden ---
    public static final String NOT_ORDER_OWNER = """
        { "status": 403, "code": "REV-4031", "message": "본인의 주문에 대해서만 리뷰를 작성할 수 있습니다." }""";

    public static final String NOT_REVIEW_WRITER = """
        { "status": 403, "code": "REV-4032", "message": "리뷰 작성자만 수정할 수 있습니다." }""";

    public static final String NOT_REVIEW_DELETER = """
        { "status": 403, "code": "REV-4034", "message": "리뷰 삭제 권한이 없습니다." }""";

    // --- 404 Not Found ---
    public static final String REVIEW_NOT_FOUND = """
        { "status": 404, "code": "REV-4041", "message": "리뷰를 찾을 수 없습니다." }""";

    public static final String ORDER_NOT_FOUND = """
        { "status": 404, "code": "REV-4042", "message": "주문 정보를 찾을 수 없습니다." }""";

    public static final String STORE_NOT_FOUND = """
        { "status": 404, "code": "REV-4043", "message": "가게 정보를 찾을 수 없습니다." }""";

    // --- 409 Conflict ---
    public static final String ALREADY_REVIEWED = """
        { "status": 409, "code": "REV-4091", "message": "이미 해당 주문에 대한 리뷰가 존재합니다." }""";
}
