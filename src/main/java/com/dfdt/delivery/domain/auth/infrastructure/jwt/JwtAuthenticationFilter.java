package com.dfdt.delivery.domain.auth.infrastructure.jwt;

import com.dfdt.delivery.common.util.RedisService;
import com.dfdt.delivery.domain.auth.domain.exception.error.enums.AuthErrorCode;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 모든 요청에서 JWT 토큰을 추출하고 유효성을 검증하여 Spring Security 인증 정보를 설정하는 필터.
 * 중복 로그인 방지 로직과 블랙리스트 확인 로직을 포함.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private final RedisService redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 요청 헤더에서 JWT 토큰을 추출.
        String token = jwtProvider.resolveToken(request.getHeader(JwtProvider.AUTHORIZATION_HEADER));

        if (StringUtils.hasText(token)) {
            // 블랙리스트 확인: 사용자가 로그아웃한 토큰인지 Redis에서 조회.
            if (redisService.hasKey("blacklist:" + token)) {
                log.warn("Blacklisted token accessed: {}", token);
                filterChain.doFilter(request, response);
                return;
            }

            // JWT 유효성 및 만료 기간 확인: 기술적으로 유효한 토큰인지 검증.
            if (!jwtProvider.validateToken(token)) {
                log.error("Token validation failed");
                filterChain.doFilter(request, response);
                return;
            }

            // 사용자 정보 추출: 토큰 내부의 Claims에서 username(Subject)을 가져옵니다.
            Claims info = jwtProvider.getUserInfoFromToken(token);
            String username = info.getSubject();

            // 중복 로그인 확인 (Active Token 체크)
            String activeToken = (String) redisService.getData("active_token:" + username);
            if (activeToken != null && !activeToken.equals(token)) {
                log.warn("Duplicate login detected for user: {}. Current token is outdated.", username);
                throw new JwtException(AuthErrorCode.INVALID_ACCESS_TOKEN.name());
            }

            setAuthentication(username);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Spring Security의 ContextHolder에 인증 객체를 담는다.
     * 비즈니스 로직(Controller 등)에서 @AuthenticationPrincipal을 사용.
     * @param username 인증된 사용자의 아이디
     */
    public void setAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
