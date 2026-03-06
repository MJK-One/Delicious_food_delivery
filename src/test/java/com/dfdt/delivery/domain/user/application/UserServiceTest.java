package com.dfdt.delivery.domain.user.application;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.common.util.RedisService;
import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import com.dfdt.delivery.domain.user.domain.repository.UserRepository;
import com.dfdt.delivery.domain.user.presentation.dto.SignupRequestDto;
import com.dfdt.delivery.domain.user.presentation.dto.UserResponseDto;
import com.dfdt.delivery.domain.user.presentation.dto.UserRoleUpdateRequestDto;
import com.dfdt.delivery.domain.user.presentation.dto.UserUpdateRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisService redisService;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signupSuccessTest() {
        // given
        SignupRequestDto requestDto = new SignupRequestDto("newuser", "password123!", "홍길동", UserRole.CUSTOMER);
        given(userRepository.findByUsername(anyString())).willReturn(Optional.empty());
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        
        User savedUser = User.builder()
                .username("newuser")
                .name("홍길동")
                .role(UserRole.CUSTOMER)
                .build();
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // when
        UserResponseDto result = userService.signup(requestDto);

        // then
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getName()).isEqualTo("홍길동");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 아이디 중복")
    void signupFailDuplicateTest() {
        // given
        SignupRequestDto requestDto = new SignupRequestDto("existing", "password", "이름", UserRole.CUSTOMER);
        given(userRepository.findByUsername("existing")).willReturn(Optional.of(User.builder().build()));

        // when & then
        assertThatThrownBy(() -> userService.signup(requestDto))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("내 프로필 수정 테스트")
    void updateProfileTest() {
        // given
        String username = "testuser";
        User user = User.builder().username(username).name("이전이름").role(UserRole.CUSTOMER).build();
        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));
        
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto("새이름");

        // when
        UserResponseDto result = userService.updateProfile(username, requestDto);

        // then
        assertThat(result.getName()).isEqualTo("새이름");
        assertThat(user.getName()).isEqualTo("새이름");
    }

    @Test
    @DisplayName("회원 탈퇴 테스트 - 세션 무효화 포함")
    void withdrawTest() {
        // given
        String username = "withdrawUser";
        User user = User.builder().username(username).role(UserRole.CUSTOMER).build();
        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));

        // when
        userService.withdraw(username);

        // then
        // Soft Delete 체크는 엔티티 내부 필드를 봐야 함 (필요시 추가 검증 가능)
        verify(redisService).deleteData("refresh:" + username);
        verify(redisService).deleteData("active_token:" + username);
    }

    @Test
    @DisplayName("권한 변경 테스트 - 세션 무효화 포함")
    void updateRoleTest() {
        // given
        String username = "user";
        User user = User.builder().username(username).role(UserRole.CUSTOMER).build();
        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));

        UserRoleUpdateRequestDto requestDto = new UserRoleUpdateRequestDto(username, UserRole.OWNER);

        // when
        userService.updateRole(requestDto, "admin");

        // then
        assertThat(user.getRole()).isEqualTo(UserRole.OWNER);
        verify(redisService).deleteData("refresh:" + username);
        verify(redisService).deleteData("active_token:" + username);
    }
}
