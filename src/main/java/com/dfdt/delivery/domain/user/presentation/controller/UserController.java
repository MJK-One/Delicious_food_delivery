package com.dfdt.delivery.domain.user.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.user.application.UserService;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import com.dfdt.delivery.domain.user.presentation.dto.SignupRequestDto;
import com.dfdt.delivery.domain.user.presentation.dto.UserResponseDto;
import com.dfdt.delivery.domain.user.presentation.dto.UserRoleUpdateRequestDto;
import com.dfdt.delivery.domain.user.presentation.dto.UserUpdateRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관리와 관련된 HTTP 요청을 처리하는 컨트롤러입니다.
 * 회원가입, 내 정보 관리, 관리자용 유저 권한 변경 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 신규 회원가입 처리.
     * @param requestDto 가입할 유저 정보
     * @return 가입 완료 처리 메시지.
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> signup(@RequestBody @Valid SignupRequestDto requestDto) {
        UserResponseDto response = userService.signup(requestDto);
        return ApiResponseDto.success(201, "회원가입 성공", response);
    }

    /**
     * 현재 로그인한 사용자의 프로필 정보 조회.
     * @param userDetails 인증 컨텍스트의 유저 상세 정보
     * @return 유저 정보
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponseDto response = userService.getUserProfile(userDetails.getUsername());
        return ApiResponseDto.success(200, "내 정보 조회 성공", response);
    }

    /**
     * 현재 로그인한 사용자의 프로필 정보를 수정.
     * @param userDetails 인증 컨텍스트의 유저 상세 정보
     * @param requestDto 수정할 데이터
     * @return 수정된 유저 정보
     */
    @PatchMapping("/me")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid UserUpdateRequestDto requestDto) {
        UserResponseDto response = userService.updateProfile(userDetails.getUsername(), requestDto);
        return ApiResponseDto.success(200, "정보 수정 성공", response);
    }

    /**
     * 현재 로그인한 사용자의 계정을 탈퇴(삭제) 처리.
     * @param userDetails 인증 컨텍스트의 유저 상세 정보
     * @return 탈퇴 성공 메세지
     */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponseDto<Void>> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.withdraw(userDetails.getUsername());
        return ApiResponseDto.success(200, "회원 탈퇴 성공", null);
    }

    /**
     * 관리자(MASTER) 전용 : 특정 사용자의 권한을 강제로 변경.
     * @param requestDto 대상 사용자 아이디와 변경할 권한 정보
     * @param adminDetails 수정을 지시한 관리자 아이디
     * @return 권한 변경 성공 메세지
     */
    @PatchMapping("/role")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponseDto<Void>> updateRole(
            @RequestParam UserRoleUpdateRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails adminDetails) {
        userService.updateRole(requestDto, adminDetails.getUsername());
        return ApiResponseDto.success(200, "권한 변경 성공", null);
    }
}
