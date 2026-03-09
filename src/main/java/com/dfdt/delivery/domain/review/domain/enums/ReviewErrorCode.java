package com.dfdt.delivery.domain.review.domain.enums;

import com.dfdt.delivery.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReviewErrorCode implements ErrorCode {

    // 400 Bad Request
    INVALID_RATING(
            HttpStatus.BAD_REQUEST,
            "REV-4001",
            "리뷰 평점 값이 올바르지 않습니다."
    ),
    INVALID_ORDER_STATUS(
            HttpStatus.BAD_REQUEST,
            "REV-4002",
            "완료된 주문에 대해서만 리뷰를 작성할 수 있습니다."
    ),
    INVALID_PAYMENT_STATUS(
            HttpStatus.BAD_REQUEST,
            "REV-4003",
            "결제가 완료된 주문에 대해서만 리뷰를 작성할 수 있습니다."
    ),
    REVIEW_ALREADY_DELETED(
            HttpStatus.BAD_REQUEST,
            "REV-4004",
            "이미 삭제된 리뷰입니다."
    ),
    IMAGE_LIMIT_EXCEEDED(
            HttpStatus.BAD_REQUEST,
            "REV-4005",
            "리뷰 이미지는 최대 5장까지 등록 가능합니다."
    ),
    INVALID_IMAGE_FORMAT(
            HttpStatus.BAD_REQUEST,
            "REV-4006",
            "지원하지 않는 이미지 형식입니다."
    ),

    // 401 Unauthorized
    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "REV-4010",
            "인증이 필요합니다."
    ),

    // 403 Forbidden
    NOT_ORDER_OWNER(
            HttpStatus.FORBIDDEN,
            "REV-4031",
            "본인의 주문에 대해서만 리뷰를 작성할 수 있습니다."
    ),
    NOT_REVIEW_WRITER(
            HttpStatus.FORBIDDEN,
            "REV-4032",
            "리뷰 작성자만 수정할 수 있습니다."
    ),
    ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "REV-4033",
            "리뷰에 대한 권한이 없습니다."
    ),
    NOT_REVIEW_DELETER(
            HttpStatus.FORBIDDEN,
            "REV-4034",
            "리뷰 삭제 권한이 없습니다."
    ),

    // 404 Not Found
    REVIEW_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "REV-4041",
            "리뷰를 찾을 수 없습니다."
    ),
    ORDER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "REV-4042",
            "주문 정보를 찾을 수 없습니다."
    ),
    STORE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "REV-4043",
            "가게 정보를 찾을 수 없습니다."
    ),

    // 409 Conflict
    ALREADY_REVIEWED(
            HttpStatus.CONFLICT,
            "REV-4091",
            "이미 해당 주문에 대한 리뷰가 존재합니다."
    );

    private final HttpStatus status;
    private final String errorCode;
    private final String message;

    @Override
    public int getStatus() {
        return status.value();
    }
}