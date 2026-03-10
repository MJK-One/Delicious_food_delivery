package com.dfdt.delivery.domain.auth.presentation.Controller;

public interface AuthErrorDocs {
    String LOGIN_FAILED = "{\"status\": 401, \"errorCode\": \"AUTH-4011\", \"message\": \"아이디 또는 비밀번호가 일치하지 않습니다.\"}";
    String TOKEN_VERSION_MISMATCH = "{\"status\": 401, \"errorCode\": \"AUTH-4012\", \"message\": \"권한 정보가 변경되어 재로그인이 필요합니다.\"}";
    String EXPIRED_ACCESS_TOKEN = "{\"status\": 401, \"errorCode\": \"AUTH-4013\", \"message\": \"만료된 Access Token입니다.\"}";
    String INVALID_ACCESS_TOKEN = "{\"status\": 401, \"errorCode\": \"AUTH-4014\", \"message\": \"유효하지 않거나 손상된 Access Token입니다.\"}";
    String INVALID_REFRESH_TOKEN = "{\"status\": 401, \"errorCode\": \"AUTH-4015\", \"message\": \"유효하지 않거나 만료된 Refresh Token입니다.\"}";
    String FORBIDDEN = "{\"status\": 403, \"errorCode\": \"AUTH-4030\", \"message\": \"접근 권한이 없습니다.\"}";
    String INVALID_INPUT_VALUE = "{\"status\": 400, \"errorCode\": \"INVALID_REQUEST\", \"message\": \"@Valid 형식 에러\"}";
    }