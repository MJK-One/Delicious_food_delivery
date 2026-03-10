package com.dfdt.delivery.domain.auth.presentation.Controller;

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

/**
 * 사용자 인증(로그인, 로그아웃, 토큰 재발급)을 위한 API 컨트롤러.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;

    /**
     * 로그인을 수행하고 인증 토큰을 발급.
     * @param requestDto 아이디, 비밀번호
     * @return AccessToken, RefreshToken 세트
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<TokenResponseDto>> login(@RequestBody @Valid LoginRequestDto requestDto) {
        TokenResponseDto response = authService.login(requestDto);
        return ApiResponseDto.success(200, "로그인 성공", response);
    }

    /**
     * 로그아웃을 처리하고 토큰을 무효화.
     * @param bearerToken 현재 사용 중인 Access Token (Header)
     * @param userDetails 인증된 사용자 상세 정보
     * @return 로그아웃 성공 메시지
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<Void>> logout(
            @RequestHeader(JwtProvider.AUTHORIZATION_HEADER) String bearerToken,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.logout(bearerToken, userDetails.getUsername());
        return ApiResponseDto.success(200, "로그아웃 성공", null);
    }

    /**
     * 만료된 Access Token을 Refresh Token을 통해 재발급.
     * @param bearerRefreshToken 현재 사용 중인 Refresh Token (Header)
     * @return 새로운 AccessToken, RefreshToken 세트
     */
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponseDto<TokenResponseDto>> reissue(
            @RequestHeader(JwtProvider.AUTHORIZATION_HEADER) String bearerRefreshToken) {
        TokenResponseDto response = authService.reissue(bearerRefreshToken);
        return ApiResponseDto.success(200, "토큰 재발급 성공", response);
    }
}
