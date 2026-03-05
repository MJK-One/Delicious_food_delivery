package com.dfdt.delivery.domain.store.presentation.docs;

public final class StoreSuccessDocs {

    // [API-STORE-001] 가게 단일 조회 성공 응답
    public static final String STORE_GET_SUCCESS = """
    {
      "status": 200,
      "message": "{store_name} 가게가 성공적으로 조회되었습니다.",
      "data": {
        "storeId": "df63c57c-f25c-4123-9d17-9cc4914261f1",
        "ownerUsername": "홍길동",
        "regionId": 1100100,
        "name": "광화문 김밥천국",
        "addressText": "서울 종로구 광화문로 1",
        "phone": "02-1234-5678",
        "description": "가게 소개",
        "isOpen": true,
        "rating": 4.5,
        "reviewCount": 100,
        "createdAt": "2026-02-25T15:30:00"
      }
    }
    """;

    // [API-STORE-002-1] 가게 목록 조회 성공 응답
    public static final String STORES_GET_SUCCESS = """
    {
      "status": 200,
      "message": "가게 목록이 성공적으로 조회되었습니다.",
      "data": {
          "content":[
          {
                "storeId": "df63c57c-f25c-4123-9d17-9cc4914261f1",
                "ownerName": "홍길동",
                "regionId": "26d953ec-b216-42e4-b9f6-5bdc3cd3f423",
                "name": "광화문 김밥천국1",
                "addressText": "서울 종로구 광화문로 1",
                "phone": "02-1234-5678",
                "description": "24시간 운영합니다.",
                "isOpen": false,
                "status": "APPROVED",
                "rating": 0,
                "reviewCount": 0,
                "createdAt": "2026-03-03T11:42:27.036496Z"
          },
          {
                "storeId": "df63c57c-f25c-4123-9d17-9cc4914261f2",
                "ownerName": "홍길동",
                "regionId": "26d953ec-b216-42e4-b9f6-5bdc3cd3f424",
                "name": "광화문 김밥천국2",
                "addressText": "서울 종로구 광화문로 2",
                "phone": "02-1234-5678",
                "description": "24시간 운영합니다.",
                "isOpen": false,
                "status": "APPROVED",
                "rating": 0,
                "reviewCount": 0,
                "createdAt": "2026-03-03T11:42:27.036496Z"
          }
        ],
        "page": 0,
        "size": 10,
        "totalElements": 25,
        "totalPages": 3
      }
    }
    """;

    // [API-STORE-002-2] 가게 목록 조회(관리자용) 성공 응답
    public static final String ADMIN_STORES_GET_SUCCESS = """
    {
      "status": 200,
      "message": "가게 목록이 성공적으로 조회되었습니다.",
      "data": {
          "content":[
          {
                "storeId": "df63c57c-f25c-4123-9d17-9cc4914261f1",
                "ownerName": "홍길동",
                "regionId": "26d953ec-b216-42e4-b9f6-5bdc3cd3f423",
                "name": "광화문 김밥천국1",
                "addressText": "서울 종로구 광화문로 1",
                "phone": "02-1234-5678",
                "description": "24시간 운영합니다.",
                "isOpen": false,
                "status": "APPROVED",
                "rating": 0,
                "reviewCount": 0,
                "createdAt": "2026-03-03T11:42:27.036496Z"
          },
          {
                "storeId": "df63c57c-f25c-4123-9d17-9cc4914261f1",
                "ownerName": "홍길동",
                "regionId": "26d953ec-b216-42e4-b9f6-5bdc3cd3f423",
                "name": "광화문 김밥천국2",
                "addressText": "서울 종로구 광화문로 2",
                "phone": "02-1234-5678",
                "description": "24시간 운영합니다.",
                "isOpen": false,
                "status": "APPROVED",
                "rating": 0,
                "reviewCount": 0,
                "createdAt": "2026-03-03T11:42:27.036496Z"
          }
        ],
        "page": 0,
        "size": 10,
        "totalElements": 25,
        "totalPages": 3
      }
    }
    """;

    // [API-STORE-003] 가게 생성 성공 응답
    public static final String STORE_CREATE_SUCCESS = """
    {
      "status": 201,
      "message": "가게가 성공적으로 생성되었습니다.",
      "data": {
        "storeId": "9b2c4a52-8f65-4c3d-8d2e-7a5f4b8e1234",
        "status": "REQUESTED",
        "createdAt": "2026-02-25T15:30:00"
      }
    }
    """;

    // [API-STORE-004] 가게 수정 성공 응답
    public static final String STORE_UPDATE_SUCCESS = """
    {
      "status": 200,
      "message": "가게 정보가 성공적으로 수정되었습니다.",
      "data": {
        "storeId": "9b2c4a52-8f65-4c3d-8d2e-7a5f4b8e1234",
        "regionId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "name": "광화문 김밥천국 2호점",
        "phone": "02-9876-5432",
        "description": "리뉴얼 오픈했습니다.",
        "addressText": "서울 종로구 세종대로 1",
        "isOpen": false,
        "updatedAt": "2026-02-25T16:20:00",
        "categories": [
          {
              "categoryId": "UUID1",
              "name": "분식"
          },
          {
              "categoryId": "UUID2",
              "name": "한식"
          }
        ]
      }
    }
    """;

    // [API-STORE-005] 가게 삭제 성공 응답
    public static final String STORE_DELETE_SUCCESS = """
    {
      "status": 201,
      "message": "가게가 성공적으로 삭제되었습니다.",
      "data": {
      }
    }
    """;

    // [API-STORE-006] 영업 상태 변경 성공 응답
    public static final String CHANGE_STATUS_SUCCESS = """
    {
      "status": 200,
      "message": "영업 상태가 변경되었습니다.",
      "data": {
      }
    }
    """;

    // [API-STORE-007] 본인 가게 조회 성공 응답
    public static final String MY_STORES_GET_SUCCESS = """
    {
      "status": 201,
      "message": "가게가 성공적으로 조회되었습니다.",
      "data": {
          "stores": [
              {
              "storeId": "dd0a79cb-f825-4877-a0d9-47b0f07113dc",
              "name": "광화문 김밥천국1",
              "categories": [
                  {
                      "categoryId": "4d82d85c-c1cd-4fe3-ad64-0a93b6ceb933",
                      "name": "분식"
                  },
                  {
                      "categoryId": "4d82d85c-c1cd-4fe3-ad64-0a93b6ceb933",
                      "name": "한식"
                  },
                  "phone": "02-9876-5432",
                  "description": "리뉴얼 오픈했습니다.",
                  "addressText": "서울 종로구 세종대로 1",
              "rating": 4.5,
              "reviewCount": 120,
              "isOpen": true,
              "status": "APPROVED",
              "createdAt": "2026-02-25T15:30:00"
          },
              {
              "storeId": "dd0a79cb-f825-4877-a0d9-47b0f07113dc",
              "name": "광화문 김밥천국2",
              "categories": [
                {
                      "categoryId": "UUID1",
                      "name": "분식"
                  },
                  {
                      "categoryId": "UUID2",
                      "name": "한식"
                  },
              "phone": "02-9876-5432",
                  "description": "리뉴얼 오픈했습니다.",
                  "addressText": "서울 종로구 세종대로 1",
              "rating": 4.5,
              "reviewCount": 120,
              "isOpen": true,
              "status": "APPROVED",
              "createdAt": "2026-02-25T15:30:00"
          },
        ]
      }
    }
    """;

    // [API-STORE-008] 가게 복구 성공 응답
    public static final String STORE_RESTORE_SUCCESS = """
    {
      "status": 201,
      "message": "품절 처리가 성공적으로 완료되었습니다.",
      "data": {
         "storeId": "dd0a79cb-f825-4877-a0d9-47b0f07113dc",
            "ownerUsername": "홍길동"
            "regionId": 1100100
            "name": "광화문 김밥천국",
            "address": "서울 종로구 광화문로 1",
            "phone": "02-1234-5678",
            "description": "가게 소개",
            "isOpen": true,
            "updatedAt": 2026-02-25T16:20:00\s
      }
    }
    """;

    // [API-STORE-009] 가게 승인 여부 성공 응답
    public static final String REQUESTED_STATUS_SUCCESS = """
    {
      "status": 200,
      "message": "가게가 승인되었습니다.",
      "data": {
          "storeId": "dd0a79cb-f825-4877-a0d9-47b0f07113dc",
          "regionId": "26d953ec-b216-42e4-b9f6-5bdc3cd3f423",
                "ownerName": "마스터",
                "categories":[
                    {
                        "categoryId": "4d82d85c-c1cd-4fe3-ad64-0a93b6ceb933",
                        "name": "양식"
                    }
                ],
                "name": "광화문 김밥천국1",
                "addressText": "서울 종로구 광화문로 2",
                "phone": "02-1234-5678",
                "description": "24시간 운영합니다.",
                "status": "APPROVED",
                "updatedAt": "2026-03-03T20:57:10.0423387+09:00",
                "isOpen": false
      }
    }
    """;

    // [API-CATEGORY-010] 승인 대기 가게 조회 성공 응답
    public static final String REQUESTED_STORES_GET_SUCCESS = """
    {
      "status": 200,
      "message": "승인 대기 가게가 성공적으로 조회되었습니다.",
      "data": [
            "storeId": "32059350-0d40-4236-b1ca-0e7b3fa5313b",
            "regionId": "26d953ec-b216-42e4-b9f6-5bdc3cd3f423",
            "categories":[
                {
                    "categoryId": "4d82d85c-c1cd-4fe3-ad64-0a93b6ceb933",
                    "name": "양식"
                }
            ],
            "name": "광화문 김밥천국 2호점",
            "description": "리뉴얼 오픈했습니다.",
            "phone": "02-9876-5432",
            "addressText": "서울 종로구 세종대로 1",
            "status": "REQUESTED",
            "isOpen": true,
            "createdAt": "2026-03-03T04:18:01.681261Z"
      ],
      "page": 0,
      "size": 10,
      "totalElements": 25,
      "totalPages": 3
    }
    """;
}