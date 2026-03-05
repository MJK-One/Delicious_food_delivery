package com.dfdt.delivery.domain.auth.application;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.common.util.RedisService;
import com.dfdt.delivery.domain.auth.infrastructure.jwt.JwtProvider;
import com.dfdt.delivery.domain.auth.presentation.dto.LoginRequestDto;
import com.dfdt.delivery.domain.auth.presentation.dto.TokenResponseDto;
import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import com.dfdt.delivery.domain.user.domain.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RedisService redisService;

    @Test
    @DisplayName("로그인 성공 테스트 - 활성 토큰 저장 포함")
    void loginSuccessTest() {
        // given
        String username = "testuser";
        String password = "password";
        LoginRequestDto requestDto = new LoginRequestDto(username, password);
        User user = User.builder()
                .username(username)
                .password("encodedPassword")
                .role(UserRole.CUSTOMER)
                .build();

        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(password, user.getPassword())).willReturn(true);
        
        given(jwtProvider.createAccessToken(anyString(), any())).willReturn("Bearer accessToken");
        given(jwtProvider.createRefreshToken(anyString(), any())).willReturn("Bearer refreshToken");
        
        // resolveToken이 두 번 호출됨 (Access, Refresh 용)
        given(jwtProvider.resolveToken(contains("accessToken"))).willReturn("accessToken");
        given(jwtProvider.resolveToken(contains("refreshToken"))).willReturn("refreshToken");

        // when
        TokenResponseDto result = authService.login(requestDto);

        // then
        assertThat(result.getAccessToken()).isEqualTo("Bearer accessToken");
        assertThat(result.getRefreshToken()).isEqualTo("Bearer refreshToken");
        
        // Redis 저장 확인
        verify(redisService).setData(eq("refresh:" + username), eq("refreshToken"), anyLong());
        verify(redisService).setData(eq("active_token:" + username), eq("accessToken"), anyLong());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void loginFailTest() {
        // given
        String username = "testuser";
        LoginRequestDto requestDto = new LoginRequestDto(username, "wrongPassword");
        User user = User.builder()
                .username(username)
                .password("encodedPassword")
                .build();

        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(requestDto))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("로그아웃 테스트")
    void logoutTest() {
        // given
        String bearerToken = "Bearer accessToken";
        String username = "testuser";
        given(jwtProvider.resolveToken(bearerToken)).willReturn("accessToken");
        given(jwtProvider.getExpiration("accessToken")).willReturn(1000L);

        // when
        authService.logout(bearerToken, username);

        // then
        verify(redisService).deleteData("refresh:" + username);
        verify(redisService).setData(eq("blacklist:accessToken"), eq("logout"), eq(1000L));
    }

    @Test
    @DisplayName("토큰 재발급 테스트")
    void reissueTest() {
        // given
        String bearerRefreshToken = "Bearer oldRefreshToken";
        String refreshToken = "oldRefreshToken";
        String username = "testuser";
        UserRole role = UserRole.CUSTOMER;

        given(jwtProvider.resolveToken(bearerRefreshToken)).willReturn(refreshToken);
        given(jwtProvider.validateToken(refreshToken)).willReturn(true);
        
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("role", role.name());

        given(jwtProvider.getUserInfoFromToken(refreshToken)).willReturn(claims);
        given(redisService.getData("refresh:" + username)).willReturn(refreshToken);
        
        given(jwtProvider.createAccessToken(username, role)).willReturn("Bearer newAccessToken");
        given(jwtProvider.createRefreshToken(username, role)).willReturn("Bearer newRefreshToken");
        given(jwtProvider.resolveToken(contains("newRefreshToken"))).willReturn("newRefreshToken");

        // when
        TokenResponseDto result = authService.reissue(bearerRefreshToken);

        // then
        assertThat(result.getAccessToken()).isEqualTo("Bearer newAccessToken");
        assertThat(result.getRefreshToken()).isEqualTo("Bearer newRefreshToken");
        verify(redisService).setData(eq("refresh:" + username), eq("newRefreshToken"), anyLong());
    }
}
