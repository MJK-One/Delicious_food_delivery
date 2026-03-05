package com.dfdt.delivery.domain.user.application;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.common.util.RedisService;
import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import com.dfdt.delivery.domain.user.domain.exception.error.enums.UserErrorCode;
import com.dfdt.delivery.domain.user.domain.repository.UserRepository;
import com.dfdt.delivery.domain.user.presentation.dto.SignupRequestDto;
import com.dfdt.delivery.domain.user.presentation.dto.UserResponseDto;
import com.dfdt.delivery.domain.user.presentation.dto.UserRoleUpdateRequestDto;
import com.dfdt.delivery.domain.user.presentation.dto.UserUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관리와 관련된 비즈니스 로직을 처리하는 서비스 클래스.
 * 회원가입, 프로필 관리, 권한 관리 및 회원 탈퇴 기능을 수행.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;

    /**
     * 새로운 사용자를 등록(회원가입).
     * @param requestDto 가입 정보 (아이디, 비밀번호, 이름, 권한 등)
     * @return 등록된 사용자 정보
     */
    @Transactional
    public UserResponseDto signup(SignupRequestDto requestDto) {
        if (userRepository.findByUsername(requestDto.getUsername()).isPresent()) {
            throw new BusinessException(UserErrorCode.DUPLICATE_USERNAME);
        }

        User user = User.builder()
                .username(requestDto.getUsername())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .name(requestDto.getName())
                .role(requestDto.getRole())
                .build();

        return UserResponseDto.from(userRepository.save(user));
    }

    /**
     * 특정 사용자의 프로필 정보를 조회.
     * @param username 조회할 사용자 아이디
     * @return 사용자 응답 DTO
     */
    public UserResponseDto getUserProfile(String username) {
        User user = findUserOrThrow(username);
        return UserResponseDto.from(user);
    }

    /**
     * 사용자의 프로필 정보를 수정. (이름 등)
     * @param username 수정할 사용자 아이디
     * @param requestDto 수정할 정보
     * @return 수정된 사용자 정보
     */
    @Transactional
    public UserResponseDto updateProfile(String username, UserUpdateRequestDto requestDto) {
        User user = findUserOrThrow(username);
        user.updateProfile(requestDto.getName(), username);
        return UserResponseDto.from(user);
    }

    /**
     * 회원 탈퇴를 처리. (Soft Delete)
     * @param username 탈퇴할 사용자 아이디
     */
    @Transactional
    public void withdraw(String username) {
        User user = findUserOrThrow(username);
        user.delete(username); 

        redisService.deleteData("refresh:" + username);
        redisService.deleteData("active_token:" + username);
    }


    /**
     * 관리자 전용: 사용자의 권한을 변경
     * @param requestDto 대상 사용자 아이디와 변경할 권한 정보
     * @param adminName 수정을 지시한 관리자 아이디
     */
    @Transactional
    public void updateRole(UserRoleUpdateRequestDto requestDto, String adminName) {
        User user = findUserOrThrow(requestDto.getUsername());
        user.updateRole(requestDto.getNewRole(), adminName);

        redisService.deleteData("refresh:" + requestDto.getUsername());
        redisService.deleteData("active_token:" + requestDto.getUsername());
    }

    /**
     * 공통 메서드: 아이디로 유저를 조회하거나 예외를 던집니다.
     */
    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }
}
