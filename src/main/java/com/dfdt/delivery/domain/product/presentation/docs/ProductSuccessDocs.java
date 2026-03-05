package com.dfdt.delivery.domain.product.presentation.docs;

public final class ProductSuccessDocs {

    // [API-PRODUCT-001] 메뉴 단일 조회 성공 응답
    public static final String PRODUCT_GET_SUCCESS = """
    {
      "status": 201,
      "message": "메뉴가 성공적으로 조회되었습니다.",
      "data": {
          "productId": "2659fcb9-ae1b-4510-9686-407114e69b0a",
          "name": "참치 김밥",
          "description": "고소한 참치와 신선한 채소",
          "isAiDescription": false,
          "price": 4000,
          "displayOrder": 2,
          "createdAt": "2026-02-25T12:00:00",
          "isHidden": true
      }
    }
    """;

    // [API-PRODUCT-002-1] 메뉴 목록 조회 성공 응답
    public static final String PRODUCTS_GET_SUCCESS = """
    {
      "status": 200,
      "message": "메뉴 목록이 성공적으로 조회되었습니다.",
      "data": {
        "content": [
         {
           "productId": "2659fcb9-ae1b-4510-9686-407114e69b0a",
           "name": "참치 김밥",
           "description": "고소한 참치와 신선한 채소",
           "isAiDescription": false,
           "price": 4000,
           "displayOrder": 1,
           "createdAt": "2026-02-25T12:00:00",
           "isHidden": true
         },
         {
           "productId": "2659fcb9-ae1b-4510-9686-407114e69b02",
           "name": "라면",
           "description": "얼큰한 국물",
           "isAiDescription": false,
           "price": 3500,
           "displayOrder": 2,
           "createdAt": "2026-02-25T12:00:00",
           "isHidden": false
         }
        ],
        "page": 0,
        "size": 10,
        "totalElements": 25,
        "totalPages": 3
      }
    }
    """;

    // [API-PRODUCT-002-2] 메뉴 목록 조회(관리자용) 성공 응답
    public static final String ADMIN_PRODUCTS_GET_SUCCESS = """
    {
      "status": 200,
      "message": "메뉴 목록이 성공적으로 조회되었습니다.",
      "data": {
        "content": [
          {
            "productId": "2659fcb9-ae1b-4510-9686-407114e69b0a",
            "name": "참치 김밥",
            "description": "고소한 참치와 신선한 채소",
            "isAiDescription": false,
            "price": 4000,
            "displayOrder": 1,
            "createdAt": "2026-02-25T12:00:00",
            "updatedAt": "2026-02-25T12:00:00",
            "deletedAt": "2026-02-25T12:00:00",
            "isHidden": true
          },
          {
            "productId": "2659fcb9-ae1b-4510-9686-407114e69b01",
            "name": "라면",
            "description": "얼큰한 국물",
            "isAiDescription": false,
            "price": 3500,
            "displayOrder": 2,
            "createdAt": "2026-02-25T12:00:00",
            "updatedAt": "2026-02-25T12:00:00",
            "deletedAt": "2026-02-25T12:00:00",
            "isHidden": true
          }
        ],
        "page": 0,
        "size": 10,
        "totalElements": 25,
        "totalPages": 3
      }
    }
    """;

    // [API-PRODUCT-003] 메뉴 생성 성공 응답
    public static final String PRODUCT_CREATE_SUCCESS = """
    {
      "status": 201,
      "message": "메뉴가 성공적으로 생성되었습니다.",
      "data": {
          "productId": "2659fcb9-ae1b-4510-9686-407114e69b0a",
          "name": "아메리카노",
          "price": 4500,
          "displayOrder": 1,
          "isHidden": false,
          "isAiDescription": false,
          "createdAt": "2026-02-25T12:00:00"
      }
    }
    """;

    // [API-PRODUCT-004] 메뉴 수정 성공 응답
    public static final String PRODUCT_UPDATE_SUCCESS = """
    {
       "status": 200,
       "message": "메뉴가 성공적으로 수정되었습니다.",
       "data": {
            "name": "아이스 아메리카노",
          "description": "시원하고 깔끔한 커피",
          "price": 5000,
          "displayOrder": 2,
          "isHidden": false,
          "isAiDescription": false,
          "updatedAt": 2026-02-25T16:20:00
       }
     }
    """;

    // [API-PRODUCT-005] 메뉴 삭제 성공 응답
    public static final String PRODUCT_DELETE_SUCCESS = """
    {
      "status": 201,
      "message": "메뉴가 성공적으로 삭제되었습니다.",
      "data": {
      }
    }
    """;

    // [API-PRODUCT-006] 품절 처리 성공 응답
    public static final String SOLD_OUT_SUCCESS = """
    {
      "status": 201,
      "message": "메뉴가 품절 처리되었습니다.",
      "data": {
      }
    }
    """;

    // [API-PRODUCT-007] 메뉴 복구 성공 응답
    public static final String PRODUCT_RESTORE_SUCCESS = """
    {
      "status": 200,
      "message": "메뉴 복구가 성공적으로 완료되었습니다.",
      "data": {
      }
    }
    """;
}