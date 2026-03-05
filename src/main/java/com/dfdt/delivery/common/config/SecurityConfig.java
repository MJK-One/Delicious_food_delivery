package com.dfdt.delivery.common.config;

import com.dfdt.delivery.common.util.RedisService;
import com.dfdt.delivery.domain.auth.infrastructure.jwt.JwtAuthenticationFilter;
import com.dfdt.delivery.domain.auth.infrastructure.jwt.JwtExceptionFilter;
import com.dfdt.delivery.domain.auth.infrastructure.jwt.JwtProvider;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 애플리케이션의 전반적인 보안 설정을 담당하는 클래스.
 * JWT 기반의 Stateless 인증 방식을 구성하며, 필터 체인 및 경로별 접근 권한을 정의.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    /**
     * 비밀번호 암호화에 사용할 PasswordEncoder를 빈으로 등록.
     * 보안 표준인 BCrypt 알고리즘을 사용.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * JWT 인증을 수행하는 커스텀 필터를 빈으로 등록.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider, userDetailsService, redisService);
    }

    /**
     * JWT 인증 과정 중 발생하는 예외를 처리하는 필터를 빈으로 등록.
     */
    @Bean
    public JwtExceptionFilter jwtExceptionFilter() {
        return new JwtExceptionFilter(objectMapper);
    }

    /**
     * HTTP 보안 필터 체인을 구성.
     * CSRF 비활성화, 세션 정책 설정, 경로별 권한 설정, 커스텀 필터 등록 등을 수행.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 설정 비활성화 (Stateless한 JWT 방식에서는 불필요)
                .csrf(AbstractHttpConfigurer::disable)
                
                // 세션 관리 정책 설정: 세션을 사용하지 않음 (STATELESS)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // HTTP 요청별 접근 제어 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/swagger",
                                "/api/v1/swagger-ui/**",
                                "/api/v1/v3/api-docs/**",
                                "/api/v1/api-docs/**",
                                "/swagger",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                
                // 커스텀 필터 배치 순서 결정
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter(), JwtAuthenticationFilter.class);

        return http.build();
    }
}
