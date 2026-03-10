package com.dfdt.delivery.domain.auth.presentation.Controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.auth.presentation.dto.LoginRequestDto;
import com.dfdt.delivery.domain.auth.presentation.dto.TokenResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth (인증)", description = "로그인, 로그아웃 및 토큰 재발급을 담당합니다.")
public interface AuthControllerDocs {

    @Operation(summary = "API-Auth-001 로그인", description = "사용자 아이디와 비밀번호를 받아 인증 토큰을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "입력 형식 오류", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "입력 형식 오류", value = AuthErrorDocs.INVALID_INPUT_VALUE)
            })),
            @ApiResponse(responseCode = "401", description = "로그인 실패", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "로그인 실패", value = AuthErrorDocs.LOGIN_FAILED)
            }))
    })
    ResponseEntity<ApiResponseDto<TokenResponseDto>> login(
            @RequestBody @Valid LoginRequestDto requestDto);

    @Operation(summary = "API-Auth-003 로그아웃", description = "현재 사용 중인 토큰을 무효화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 오류", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "만료된 토큰", value = AuthErrorDocs.EXPIRED_ACCESS_TOKEN),
                    @ExampleObject(name = "유효하지 않은 토큰", value = AuthErrorDocs.INVALID_ACCESS_TOKEN),
                    @ExampleObject(name = "권한 정보 변경", value = AuthErrorDocs.TOKEN_VERSION_MISMATCH)
            })),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "접근 권한 없음", value = AuthErrorDocs.FORBIDDEN)
            }))
    })
    ResponseEntity<ApiResponseDto<Void>> logout(
            @Parameter(description = "Access Token (Bearer )") String bearerToken,
            @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "API-Auth-002 토큰 재발급", description = "Refresh Token을 사용하여 새로운 Access/Refresh 토큰을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "리프레시 토큰 오류", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "유효하지 않은 리프레시 토큰", value = AuthErrorDocs.INVALID_REFRESH_TOKEN)
            }))
    })
    ResponseEntity<ApiResponseDto<TokenResponseDto>> reissue(
            @Parameter(description = "Refresh Token (Bearer )") String bearerRefreshToken);
}