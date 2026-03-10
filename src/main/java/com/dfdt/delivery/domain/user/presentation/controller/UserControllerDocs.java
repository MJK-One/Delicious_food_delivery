package com.dfdt.delivery.domain.user.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.user.presentation.dto.SignupRequestDto;
import com.dfdt.delivery.domain.user.presentation.dto.UserResponseDto;
import com.dfdt.delivery.domain.user.presentation.dto.UserRoleUpdateRequestDto;
import com.dfdt.delivery.domain.user.presentation.dto.UserUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User (사용자)", description = "회원가입, 프로필 관리 및 관리자 전용 권한 변경을 담당합니다.")
public interface UserControllerDocs {

    @Operation(summary = "API-User-001 회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "입력 형식 오류", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "아이디 형식 오류", value = UserErrorDocs.INVALID_USERNAME_FORMAT),
                    @ExampleObject(name = "비밀번호 형식 오류", value = UserErrorDocs.INVALID_PASSWORD_FORMAT)
            })),
            @ApiResponse(responseCode = "409", description = "중복 오류", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "아이디 중복", value = UserErrorDocs.DUPLICATE_USERNAME)
            }))
    })
    ResponseEntity<ApiResponseDto<UserResponseDto>> signup(@Valid @RequestBody SignupRequestDto requestDto);

    @Operation(summary = "API-User-002 내 정보 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "사용자 조회 실패", value = UserErrorDocs.USER_NOT_FOUND)
            }))
    })
    ResponseEntity<ApiResponseDto<UserResponseDto>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "API-User-003 권한 변경", description = "특정 사용자의 권한을 변경합니다. (MASTER 전용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 권한 값", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "유효하지 않은 권한", value = UserErrorDocs.INVALID_ROLE_VALUE)
            })),
            @ApiResponse(responseCode = "403", description = "권한 부족"),
            @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "대상 사용자 없음", value = UserErrorDocs.USER_NOT_FOUND)
            }))
    })
    ResponseEntity<ApiResponseDto<Void>> updateRole(
            @RequestBody UserRoleUpdateRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails adminDetails);

    @Operation(summary = "API-User-004 회원 탈퇴", description = "현재 로그인한 사용자의 계정을 탈퇴(Soft Delete) 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "사용자 조회 실패", value = UserErrorDocs.USER_NOT_FOUND)
            }))
    })
    ResponseEntity<ApiResponseDto<Void>> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "API-User-005 내 정보 수정", description = "현재 로그인한 사용자의 닉네임, 이메일 등을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "사용자 조회 실패", value = UserErrorDocs.USER_NOT_FOUND)
            }))
    })
    ResponseEntity<ApiResponseDto<UserResponseDto>> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserUpdateRequestDto requestDto);
}