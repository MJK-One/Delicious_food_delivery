package com.dfdt.delivery.common.exception;

import com.dfdt.delivery.common.exception.error.enums.CommonErrorCode;
import com.dfdt.delivery.common.response.ErrorResponseDto;
import com.dfdt.delivery.domain.auth.domain.exception.error.enums.AuthErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
@Slf4j(topic = "예외")
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponseDto> handleCustomException(BusinessException e) {
        log.warn("SERVICE_ERROR - Code: {}, Message: {}", e.getErrorCode().getErrorCode(),e.getErrorCode().getMessage());
        return ErrorResponseDto.fail(e.getErrorCode());
    }

    // @Valid 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidExceptionException(MethodArgumentNotValidException e) {
        log.warn("VALID_ERROR");
        return ErrorResponseDto.fail(CommonErrorCode.INVALID_INPUT_VALUE, e.getBindingResult());
    }

    //BAD_REQUEST
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponseDto> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("METHOD_NOT_ALLOWED");
        return ErrorResponseDto.fail(CommonErrorCode.METHOD_NOT_ALLOWED, e.getMessage());
    }

    //@PreAuthorize 검증 실패 시 호출.
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ErrorResponseDto> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("ACCESS_DENIED - {}", e.getMessage());
        return ErrorResponseDto.fail(AuthErrorCode.FORBIDDEN);
    }

    // NoHandlerFoundException
    @ExceptionHandler(NoHandlerFoundException.class)
    protected ResponseEntity<ErrorResponseDto> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("존재하지 않는 URL 입니다");
        return ErrorResponseDto.fail(AuthErrorCode.FORBIDDEN); // 이거랑 아래꺼 공통에러코드 넣고 수정해야함
    }
    // MethodArgumentTypeMismatchException
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponseDto> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("type is mismatched");
        return  ErrorResponseDto.fail(AuthErrorCode.FORBIDDEN);
    }

    // INTERNAL_SERVER_ERROR (500 에러)
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponseDto> handleException(Exception e) {
        log.error("내부 서버 오류 - {}", e.getMessage());
        return ErrorResponseDto.fail(CommonErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}