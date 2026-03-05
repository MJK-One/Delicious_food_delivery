package com.dfdt.delivery.domain.auth.infrastructure.jwt;

import com.dfdt.delivery.common.util.RedisService;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
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

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private final RedisService redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = jwtProvider.resolveToken(request.getHeader(JwtProvider.AUTHORIZATION_HEADER));

        if (StringUtils.hasText(token)) {
            // 블랙리스트 확인 (로그아웃 된 토큰인지)
            if (redisService.hasKey("blacklist:" + token)) {
                log.warn("Blacklisted token accessed: {}", token);
                filterChain.doFilter(request, response);
                return;
            }

            if (!jwtProvider.validateToken(token)) {
                log.error("Token validation failed");
                filterChain.doFilter(request, response);
                return;
            }

            Claims info = jwtProvider.getUserInfoFromToken(token);
            setAuthentication(info.getSubject());
        }

        filterChain.doFilter(request, response);
    }

    public void setAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
