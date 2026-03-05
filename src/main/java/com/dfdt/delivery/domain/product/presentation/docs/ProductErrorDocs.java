package com.dfdt.delivery.domain.product.presentation.docs;

import lombok.Getter;

@Getter
public final class ProductErrorDocs {

    // 400 BAD REQUEST
    public static final String INVALID_REQUEST = """
        {
          "status": 400,
          "message": "요청 형식이 올바르지 않습니다.",
          "errorCode": "PRODUCT-4000",
          "data": null
        }
        """;

    public static final String DIFFERENT_STORE_MENU = """
        {
          "status": 400,
          "message": "해당 가게의 메뉴가 아닙니다.",
          "errorCode": "PRODUCT-4001",
          "data": null
        }
        """;

    public static final String SOLD_OUT = """
        {
          "status": 400,
          "message": "해당 메뉴는 품절되었습니다.",
          "errorCode": "PRODUCT-4002",
          "data": null
        }
        """;

    public static final String NOT_DELETED = """
        {
          "status": 400,
          "message": "삭제된 메뉴가 아닙니다.",
          "errorCode": "PRODUCT-4003",
          "data": null
        }
        """;

    public static final String ALREADY_DELETED = """
        {
          "status": 400,
          "message": "이미 삭제된 메뉴입니다.",
          "errorCode": "PRODUCT-4004",
          "data": null
        }
        """;

    public static final String NOT_MODIFIED = """
        {
          "status": 400,
          "message": "삭제된 메뉴는 정보를 변경할 수 없습니다.",
          "errorCode": "PRODUCT-4005",
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
    public static final String NOT_FOUND_PRODUCT = """
        {
          "status": 404,
          "message": "해당 메뉴가 존재하지 않습니다.",
          "errorCode": "PRODUCT-4040",
          "data": null
        }
        """;

    public static final String NOT_FOUND_PRODUCTS = """
        {
          "status": 404,
          "message": "등록된 메뉴가 없습니다.",
          "errorCode": "PRODUCTS-4040",
          "data": null
        }
        """;

}
