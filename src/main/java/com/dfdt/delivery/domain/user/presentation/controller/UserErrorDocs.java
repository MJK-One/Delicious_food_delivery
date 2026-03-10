package com.dfdt.delivery.domain.user.presentation.controller;

public interface UserErrorDocs {
    String INVALID_USERNAME_FORMAT = "{\"status\": 400, \"errorCode\": \"USER-4001\", \"message\": \"아이디 형식이 올바르지 않습니다.\"}";
    String INVALID_PASSWORD_FORMAT = "{\"status\": 400, \"errorCode\": \"USER-4002\", \"message\": \"비밀번호 형식이 올바르지 않습니다.\"}";
    String INVALID_ROLE_VALUE = "{\"status\": 400, \"errorCode\": \"USER-4003\", \"message\": \"유효하지 않은 권한 값입니다.\"}";
    String USER_NOT_FOUND = "{\"status\": 404, \"errorCode\": \"USER-4041\", \"message\": \"존재하지 않는 사용자입니다.\"}";
    String DUPLICATE_USERNAME = "{\"status\": 409, \"errorCode\": \"USER-4091\", \"message\": \"이미 사용 중인 아이디입니다.\"}";
}