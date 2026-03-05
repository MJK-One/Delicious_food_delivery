package com.dfdt.delivery.domain.auth.infrastructure.jwt;

import com.dfdt.delivery.common.exception.ErrorCode;
import com.dfdt.delivery.common.response.ErrorResponseDto;
import com.dfdt.delivery.domain.auth.domain.exception.error.enums.AuthErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 관련 인증 예외를 가로채서 공통 에러 응답(JSON)을 반환하는 필터.
 * JwtAuthenticationFilter 앞에서 작동하여 해당 필터에서 던지는 JwtException을 처리.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            log.error("JWT Exception: {}", e.getMessage());
            AuthErrorCode authErrorCode = AuthErrorCode.INVALID_ACCESS_TOKEN;
            try {
                authErrorCode = AuthErrorCode.valueOf(e.getMessage());
            } catch (IllegalArgumentException iae) {
                // message가 enum name이 아닐 경우 기본값 유지
            }
            setErrorResponse(response, authErrorCode);
        } catch (Exception e) {
            log.error("General Exception in Filter: {}", e.getMessage());
        }
    }

    /**
     * HttpServletResponse에 직접 JSON 에러 응답을 작성.
     * @param response HTTP 응답 객체
     * @param errorCode 발생한 에러 코드 정보
     */
    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponseDto errorResponse = ErrorResponseDto.of(errorCode);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
