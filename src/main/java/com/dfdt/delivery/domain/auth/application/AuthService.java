package com.dfdt.delivery.domain.auth.application;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.common.util.RedisService;
import com.dfdt.delivery.domain.auth.domain.exception.error.enums.AuthErrorCode;
import com.dfdt.delivery.domain.auth.infrastructure.jwt.JwtProvider;
import com.dfdt.delivery.domain.auth.presentation.dto.LoginRequestDto;
import com.dfdt.delivery.domain.auth.presentation.dto.TokenResponseDto;
import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import com.dfdt.delivery.domain.user.domain.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String ACTIVE_TOKEN_PREFIX = "active_token:";

    @Transactional
    public TokenResponseDto login(LoginRequestDto requestDto) {
        User user = userRepository.findByUsername(requestDto.getUsername())
                .orElseThrow(() -> new BusinessException(AuthErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new BusinessException(AuthErrorCode.LOGIN_FAILED);
        }

        // 로그인 기록 갱신
        user.recordLogin();

        String accessToken = jwtProvider.createAccessToken(user.getUsername(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken(user.getUsername(), user.getRole());

        String pureAccessToken = jwtProvider.resolveToken(accessToken);
        String pureRefreshToken = jwtProvider.resolveToken(refreshToken);

        // Redis에 Refresh Token 저장 (7일)
        redisService.setData(REFRESH_TOKEN_PREFIX + user.getUsername(), pureRefreshToken, 7 * 24 * 60 * 60 * 1000L);
        
        // Redis에 현재 활성화된 Access Token 저장 (중복 로그인 방지용)
        redisService.setData(ACTIVE_TOKEN_PREFIX + user.getUsername(), pureAccessToken, 60 * 60 * 1000L);

        return TokenResponseDto.of(accessToken, refreshToken);
    }

    @Transactional
    public void logout(String bearerToken, String username) {
        String accessToken = jwtProvider.resolveToken(bearerToken);
        
        // 1. Redis에서 Refresh Token 삭제
        redisService.deleteData(REFRESH_TOKEN_PREFIX + username);

        // 2. Access Token 블랙리스트 등록 (남은 유효시간 동안)
        long expiration = jwtProvider.getExpiration(accessToken);
        redisService.setData(BLACKLIST_PREFIX + accessToken, "logout", expiration);
    }

    @Transactional
    public TokenResponseDto reissue(String bearerRefreshToken) {
        String refreshToken = jwtProvider.resolveToken(bearerRefreshToken);

        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        Claims claims = jwtProvider.getUserInfoFromToken(refreshToken);
        String username = claims.getSubject();
        String roleStr = claims.get("role", String.class);
        UserRole role = UserRole.valueOf(roleStr);

        // Redis에 저장된 토큰과 일치하는지 확인
        String savedToken = (String) redisService.getData(REFRESH_TOKEN_PREFIX + username);
        if (savedToken == null || !savedToken.equals(refreshToken)) {
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 새로운 토큰 발급
        String newAccessToken = jwtProvider.createAccessToken(username, role);
        String newRefreshToken = jwtProvider.createRefreshToken(username, role);

        String pureNewAccessToken = jwtProvider.resolveToken(newAccessToken);
        String pureNewRefreshToken = jwtProvider.resolveToken(newRefreshToken);

        // Redis 갱신 (RT와 AT 모두 갱신)
        redisService.setData(ACTIVE_TOKEN_PREFIX + username, pureNewAccessToken, 60 * 60 * 1000L);
        redisService.setData(REFRESH_TOKEN_PREFIX + username, pureNewRefreshToken, 7 * 24 * 60 * 60 * 1000L);

        return TokenResponseDto.of(newAccessToken, newRefreshToken);
    }
}
