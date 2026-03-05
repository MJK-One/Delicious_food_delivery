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

    @PostConstruct
    public void init() {
        byte[] bytes = Decoders.BASE64.decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    // 토큰 생성
    public String createAccessToken(String username, UserRole role) {
        return createToken(username, role, ACCESS_TOKEN_TIME);
    }

    public String createRefreshToken(String username, UserRole role) {
        return createToken(username, role, REFRESH_TOKEN_TIME);
    }

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

    // 토큰 검증 (예외를 직접 던짐)
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

    // 토큰에서 정보 추출
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public long getExpiration(String token) {
        return getUserInfoFromToken(token).getExpiration().getTime() - new Date().getTime();
    }
}
