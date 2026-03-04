package com.dfdt.delivery.domain.category.presentation.docs;

public final class CategorySuccessDocs {

    // [API-CATEGORY-001] 카테고리 단일 조회 성공 응답
    public static final String CATEGORY_GET_SUCCESS = """
    {
      "status": 200,
      "message": "카테고리가 성공적으로 조회되었습니다.",
      "data": {
        "categoryId": "7741ca92-f4f1-465f-9194-cb3c08eae613",
        "name": "양식",
        "description": "양식 설명",
        "displayOrder": 1,
        "isActive": true
      }
    }
    """;

    // [API-CATEGORY-002-1] 카테고리 목록 조회 성공 응답
    public static final String CATEGORIES_GET_SUCCESS = """
    {
      "status": 200,
      "message": "카테고리 목록이 성공적으로 조회되었습니다.",
      "data": [
        {
          "categoryId": "a1b2c3d4-1111-2222-3333-444455556666",
          "name": "한식",
          "description": "한식 설명",
          "sortOrder": 0,
          "isActive": true
        },
        {
          "categoryId": "b2c3d4e5-2222-3333-4444-555566667777",
          "name": "디저트",
          "description": "한식 설명",
          "sortOrder": 1,
          "isActive": true
        }
      ]
    }
    """;

    // [API-CATEGORY-002-2] 카테고리 목록 조회(관리자용) 성공 응답
    public static final String ADMIN_CATEGORIES_GET_SUCCESS = """
    {
      "status": 200,
      "message": "카테고리 목록이 성공적으로 조회되었습니다.",
      "data": {
        "content": [
            {
                "categoryId": "5a98d633-712b-4fe9-a539-dc6f2e412caa",
                "name": "한식",
                "description": "한식 설명 수정",
                "sortOrder": 0,
                "isActive": true,
                "createdAt": "2026-03-03T09:15:31.194694Z",
                "updatedAt": "2026-03-03T09:15:31.194694Z",
                "deletedAt": "2026-03-03T09:15:31.194694Z"
            }
        ],
        "page": 0,
        "size": 10,
        "totalElements": 7,
        "totalPages": 1
      }
    }
    """;

    // [API-CATEGORY-003] 카테고리 생성 성공 응답
    public static final String CATEGORY_CREATE_SUCCESS = """
    {
      "name": "한식",
      "description": "한국 음식",
      "isActive": true
    }
    """;

    // [API-CATEGORY-004] 카테고리 수정 성공 응답
    public static final String CATEGORY_UPDATE_SUCCESS = """
    {
      "status": 200,
      "message": "카테고리가 성공적으로 수정되었습니다.",
      "data": {
        "categoryId": "9b2c4a52-8f65-4c3d-8d2e-7a5f4b8e1234",
          "name": "한식",
          "description": "한국 음식",
          "sortOrder": "01",
          "isActive": true
      }
    }
    """;

    // [API-CATEGORY-005] 카테고리 삭제 성공 응답
    public static final String CATEGORY_DELETE_SUCCESS = """
    {
      "status": 200,
      "message": "카테고리가 성공적으로 삭제되었습니다.",
      "data": null
    }
    """;

    // [API-CATEGORY-006] 카테고리 복구 성공 응답
    public static final String CATEGORY_RESTORE_SUCCESS = """
    {
      "status": 200,
      "message": "카테고리가 성공적으로 복구되었습니다.",
      "data": null
    }
    """;
}