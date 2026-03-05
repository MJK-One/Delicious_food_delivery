package com.dfdt.delivery.domain.auth.infrastructure.jwt;

import com.dfdt.delivery.common.exception.ErrorCode;
import com.dfdt.delivery.common.response.ErrorResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dfdt.delivery.domain.auth.domain.exception.error.enums.AuthErrorCode;
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

    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponseDto errorResponse = ErrorResponseDto.of(errorCode);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
