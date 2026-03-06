package com.dfdt.delivery.domain.auth.infrastructure.jwt;

import com.dfdt.delivery.domain.auth.domain.exception.error.enums.AuthErrorCode;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    // 만료 시간 설정 (Access: 1시간, Refresh: 7일)
    private final long ACCESS_TOKEN_TIME = 60 * 60 * 1000L;
    private final long REFRESH_TOKEN_TIME = 7 * 24 * 60 * 60 * 1000L;

    /**
     * Secret Key를 설정합니다.
     */
    @PostConstruct
    public void init() {
        byte[] bytes = Decoders.BASE64.decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    /**
     * 사용자 정보를 Access Token을 생성.
     * @param username 사용자 아이디
     * @param role 사용자 권한
     * @return 생성된 Access Token (Bearer 접두사 포함)
     */
    public String createAccessToken(String username, UserRole role) {
        return createToken(username, role, ACCESS_TOKEN_TIME);
    }

    /**
     * 사용자 정보를 Refresh Token을 생성.
     * @param username 사용자 아이디
     * @param role 사용자 권한
     * @return 생성된 Refresh Token (Bearer 접두사 포함)
     */
    public String createRefreshToken(String username, UserRole role) {
        return createToken(username, role, REFRESH_TOKEN_TIME);
    }

    /**
     * 공통 토큰 생성 로직.
     * @param username 사용자 아이디
     * @param role 사용자 권한
     * @param expireTime 만료 시간 (밀리초)
     * @return 생성된 JWT 토큰 문자열
     */
    private String createToken(String username, UserRole role, long expireTime) {
        Date date = new Date();
        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(username)
                        .claim("role", role.name())
                        .setExpiration(new Date(date.getTime() + expireTime))
                        .setIssuedAt(date)
                        .signWith(key, signatureAlgorithm)
                        .compact();
    }

    /**
     * 토큰의 유효성 및 만료 여부를 검증.
     * @param token 검증할 순수 토큰 문자열
     * @return 유효할 경우 true 반환
     * @throws JwtException 유효하지 않거나 만료된 토큰일 경우 예외 발생
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
            throw new JwtException(AuthErrorCode.INVALID_ACCESS_TOKEN.name());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT token 입니다.");
            throw new JwtException(AuthErrorCode.EXPIRED_ACCESS_TOKEN.name());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
            throw new JwtException(AuthErrorCode.INVALID_ACCESS_TOKEN.name());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
            throw new JwtException(AuthErrorCode.INVALID_ACCESS_TOKEN.name());
        }
    }

    /**
     * 토큰 내부의 사용자 정보(Claims)를 추출.
     * @param token 추출할 순수 토큰 문자열
     * @return 토큰에 포함된 Claims 정보
     */
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    /**
     * Header에서 Bearer 접두사를 제거하고 순수한 토큰 값만 추출.
     * @param bearerToken "Bearer "로 시작하는 토큰 문자열
     * @return 접두사가 제거된 순수 토큰 (Bearer로 시작하지 않으면 null 반환)
     */
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 토큰의 남은 유효 시간을 계산.
     * @param token 계산할 순수 토큰 문자열
     * @return 남은 시간 (밀리초)
     */
    public long getExpiration(String token) {
        return getUserInfoFromToken(token).getExpiration().getTime() - new Date().getTime();
    }
}
