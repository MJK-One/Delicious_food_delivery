package com.dfdt.delivery.domain.category.presentation.docs;

import lombok.Getter;

@Getter
public final class CategoryErrorDocs {

    // 400 BAD REQUEST
    public static final String INVALID_REQUEST = """
        {
          "status": 400,
          "message": "요청 형식이 올바르지 않습니다.",
          "errorCode": "CATEGORY-4000",
          "data": null
        }
        """;

    public static final String ALREADY_EXIST = """
        {
          "status": 400,
          "message": "이미 존재하는 카테고리명입니다.",
          "errorCode": "CATEGORY-4001",
          "data": null
        }
        """;

    public static final String CATEGORY_BE_USED = """
        {
          "status": 400,
          "message": "해당 카테고리를 사용하는 가게가 존재합니다.",
          "errorCode": "CATEGORY-4002",
          "data": null
        }
        """;

    public static final String NOT_DELETED = """
        {
          "status": 400,
          "message": "삭제된 카테고리가 아닙니다.",
          "errorCode": "CATEGORY-4003",
          "data": null
        }
        """;

    public static final String ALREADY_DELETED = """
        {
          "status": 400,
          "message": "이미 삭제된 카테고리입니다.",
          "errorCode": "CATEGORY-4004",
          "data": null
        }
        """;

    public static final String NOT_MODIFIED = """
        {
          "status": 400,
          "message": "삭제된 카테고리는 정보를 변경할 수 없습니다.",
          "errorCode": "CATEGORY-4005",
          "data": null
        }
        """;

    // 403 FORBIDDEN
    public static final String FORBIDDEN = """
        {
          "status": 403,
          "message": "접근 권한이 없습니다.",
          "errorCode": "AUTH-4030",
          "data": null
        }
        """;

    // 404 NOT FOUND
    public static final String NOT_FOUND_CATEGORY = """
        {
          "status": 404,
          "message": "해당 카테고리가 존재하지 않습니다.",
          "errorCode": "CATEGORY-4040",
          "data": null
        }
        """;

    public static final String NOT_FOUND_CATEGORIES = """
        {
          "status": 404,
          "message": "등록된 카테고리가 없습니다.",
          "errorCode": "CATEGORIES-4040",
          "data": null
        }
        """;
}
