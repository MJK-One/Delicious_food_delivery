package com.dfdt.delivery.domain.auth.presentation;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.auth.application.AuthService;
import com.dfdt.delivery.domain.auth.infrastructure.jwt.JwtProvider;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.auth.presentation.dto.LoginRequestDto;
import com.dfdt.delivery.domain.auth.presentation.dto.TokenResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<TokenResponseDto>> login(@RequestBody @Valid LoginRequestDto requestDto) {
        TokenResponseDto response = authService.login(requestDto);
        return ApiResponseDto.success(200, "로그인 성공", response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<Void>> logout(
            @RequestHeader(JwtProvider.AUTHORIZATION_HEADER) String bearerToken,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.logout(bearerToken, userDetails.getUsername());
        return ApiResponseDto.success(200, "로그아웃 성공", null);
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponseDto<TokenResponseDto>> reissue(
            @RequestHeader(JwtProvider.AUTHORIZATION_HEADER) String bearerRefreshToken) {
        TokenResponseDto response = authService.reissue(bearerRefreshToken);
        return ApiResponseDto.success(200, "토큰 재발급 성공", response);
    }
}
