package com.dfdt.delivery.domain.category.domain.enums;

import com.dfdt.delivery.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CategoryErrorCode implements ErrorCode {

    // 400 BAD REQUEST
    INVALID_REQUEST(400, "CATEGORY-4000", "요청 형식이 올바르지 않습니다."),
    ALREADY_EXIST(400, "CATEGORY-4001", "이미 존재하는 카테고리명입니다."),
    CATEGORY_BE_USED(400, "CATEGORY-4002", "해당 카테고리를 사용하는 가게가 존재합니다."),

    // 404 NOT FOUND
    NOT_FOUND_CATEGORY(404, "CATEGORY-4040", "해당 카테고리가 존재하지 않습니다."),
    NOT_FOUND_CATEGORIES(404, "CATEGORIES-4040", "등록된 카테고리가 없습니다.");


    private final int status;
    private final String errorCode;
    private final String message;
}
