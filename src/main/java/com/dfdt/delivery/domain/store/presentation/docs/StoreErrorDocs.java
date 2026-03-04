package com.dfdt.delivery.domain.store.presentation.docs;

import lombok.Getter;

@Getter
public final class StoreErrorDocs {

    // 400 BAD REQUEST
    public static final String INVALID_REQUEST = """
        {
          "status": 400,
          "message": "요청 형식이 올바르지 않습니다.",
          "errorCode": "STORE-4000",
          "data": null
        }
        """;

    public static final String STATUS_NOT_MODIFIED = """
        {
          "status": 400,
          "message": "변경값과 이전값이 동일합니다.",
          "errorCode": "STORE-4001",
          "data": null
        }
        """;

    public static final String NOT_MY_STORE = """
        {
          "status": 400,
          "message": "본인 소유 가게 정보만 변경할 수 있습니다.",
          "errorCode": "STORE-4002",
          "data": null
        }
        """;

    public static final String NOT_SUSPENDED = """
        {
          "status": 400,
          "message": "영업 중지된 가게가 아닙니다.",
          "errorCode": "STORE-4003",
          "data": null
        }
        """;

    public static final String ALREADY_DELETED = """
        {
          "status": 400,
          "message": "이미 삭제된 가게입니다.",
          "errorCode": "STORE-4004",
          "data": null
        }
        """;

    public static final String NOT_MODIFIED = """
        {
          "status": 400,
          "message": "삭제된 카테고리는 정보를 변경할 수 없습니다.",
          "errorCode": "STORE-4005",
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
    public static final String NOT_FOUND_STORE = """
        {
          "status": 404,
          "message": "해당 가게가 존재하지 않습니다.",
          "errorCode": "STORE-4040",
          "data": null
        }
        """;

    public static final String NOT_FOUND_STORES = """
        {
          "status": 404,
          "message": "등록된 가게가 없습니다.",
          "errorCode": "STORES-4040",
          "data": null
        }
        """;

    public static final String NOT_FOUND_REGION = """
        {
          "status": 404,
          "message": "해당 지역이 존재하지 않습니다.",
          "errorCode": "REGION-4040",
          "data": null
        }
        """;
}
