package com.dfdt.delivery.common.response;


import com.dfdt.delivery.common.enums.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.ResponseEntity;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto<T> {

    private int status;       // 상태 코드 (예: 200, 400)
    private String message;   // 반환 메시지
    private String errorCode; // 에러 코드 (성공 시 null)
    private T data;           // 반환 데이터

    // 성공 시
    public static <T> ResponseEntity<ApiResponseDto<T>> success(int status, String message, T data) {
        return ResponseEntity
                .status(status)
                .body(new ApiResponseDto<>(status, message, null, data));
    }

    // 실패 시
    public static <T> ResponseEntity<ApiResponseDto<T>> fail(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ApiResponseDto<>(
                        errorCode.getStatus(),
                        errorCode.getMessage(),
                        errorCode.getCode(),
                        null
                ));
    }
}