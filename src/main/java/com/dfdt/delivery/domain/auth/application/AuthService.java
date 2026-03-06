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

/**
 * 인증 관련 핵심 비즈니스 로직을 담당하는 서비스 클래스.
 * 로그인, 로그아웃, 토큰 재발급 및 세션 관리(중복 로그인 방지)를 수행.
 */
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

    /**
     * 사용자 로그인을 처리.
     * @param requestDto 아이디와 비밀번호 정보를 담은 DTO
     * @return Access Token과 Refresh Token 세트
     */
    @Transactional
    public TokenResponseDto login(LoginRequestDto requestDto) {
        // 1. 사용자 존재 여부 확인
        User user = userRepository.findByUsername(requestDto.getUsername())
                .orElseThrow(() -> new BusinessException(AuthErrorCode.LOGIN_FAILED));

        // 2. 비밀번호 일치 여부 확인 (BCrypt 대조)
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new BusinessException(AuthErrorCode.LOGIN_FAILED);
        }

        // 3. 로그인 성공 기록 (Last Login At 갱신)
        user.recordLogin();

        // 4. 새로운 토큰 쌍(AT, RT) 생성
        String accessToken = jwtProvider.createAccessToken(user.getUsername(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken(user.getUsername(), user.getRole());

        // 5. Redis 저장용 순수 토큰 값 추출 (Bearer 접두사 제거)
        String pureAccessToken = jwtProvider.resolveToken(accessToken);
        String pureRefreshToken = jwtProvider.resolveToken(refreshToken);

        // 6. Redis 데이터 갱신
        // - Refresh Token: 재발급용 (7일 유지)
        redisService.setData(REFRESH_TOKEN_PREFIX + user.getUsername(), pureRefreshToken, 7 * 24 * 60 * 60 * 1000L);
        // - Active Token: 중복 로그인 방지용 (1시간 유지)
        redisService.setData(ACTIVE_TOKEN_PREFIX + user.getUsername(), pureAccessToken, 60 * 60 * 1000L);

        return TokenResponseDto.of(accessToken, refreshToken);
    }

    /**
     * 사용자 로그아웃을 처리.
     * @param bearerToken 현재 사용 중인 Access Token
     * @param username 로그아웃할 사용자의 아이디
     */
    @Transactional
    public void logout(String bearerToken, String username) {
        String accessToken = jwtProvider.resolveToken(bearerToken);
        
        // 1. Redis에서 해당 유저의 Refresh Token 삭제 (재발급 불가 처리)
        redisService.deleteData(REFRESH_TOKEN_PREFIX + username);
        // 2. Redis에서 해당 유저의 활성 토큰 정보 삭제
        redisService.deleteData(ACTIVE_TOKEN_PREFIX + username);

        // 3. Access Token 블랙리스트 등록 (남은 유효시간 동안 해당 토큰 사용 차단)
        long expiration = jwtProvider.getExpiration(accessToken);
        redisService.setData(BLACKLIST_PREFIX + accessToken, "logout", expiration);
    }

    /**
     * Refresh Token을 사용하여 새로운 Access/Refresh 토큰을 재발급.
     * @param bearerRefreshToken 사용자가 제출한 Refresh Token
     * @return 새로운 Access Token과 Refresh Token 세트
     */
    @Transactional
    public TokenResponseDto reissue(String bearerRefreshToken) {
        String refreshToken = jwtProvider.resolveToken(bearerRefreshToken);

        // 1. Refresh Token 자체의 유효성(만료, 서명 등) 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 2. 토큰에서 유저 정보 추출
        Claims claims = jwtProvider.getUserInfoFromToken(refreshToken);
        String username = claims.getSubject();
        String roleStr = claims.get("role", String.class);
        UserRole role = UserRole.valueOf(roleStr);

        // 3. Redis에 저장된 토큰과 사용자가 제출한 토큰이 일치하는지 확인 (탈취 방지)
        String savedToken = (String) redisService.getData(REFRESH_TOKEN_PREFIX + username);
        if (savedToken == null || !savedToken.equals(refreshToken)) {
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 4. 새로운 토큰 쌍 발급
        String newAccessToken = jwtProvider.createAccessToken(username, role);
        String newRefreshToken = jwtProvider.createRefreshToken(username, role);

        String pureNewAccessToken = jwtProvider.resolveToken(newAccessToken);
        String pureNewRefreshToken = jwtProvider.resolveToken(newRefreshToken);

        // 5. Redis 데이터 최신화 (새로운 RT와 AT 정보 기록)
        redisService.setData(REFRESH_TOKEN_PREFIX + username, pureNewRefreshToken, 7 * 24 * 60 * 60 * 1000L);
        redisService.setData(ACTIVE_TOKEN_PREFIX + username, pureNewAccessToken, 60 * 60 * 1000L);

        return TokenResponseDto.of(newAccessToken, newRefreshToken);
    }
}
