package com.dfdt.delivery.domain.auth.infrastructure.jwt;

import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest {

    private JwtProvider jwtProvider;
    private final String secretKey = "";

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        // @Value 필드 수동 주입
        ReflectionTestUtils.setField(jwtProvider, "secretKey", secretKey);
        jwtProvider.init();
    }

    @Test
    @DisplayName("Access Token 생성 및 username 추출 테스트")
    void createAndParseTokenTest() {
        // given
        String username = "testuser";
        UserRole role = UserRole.CUSTOMER;

        // when
        String accessToken = jwtProvider.createAccessToken(username, role);
        String pureToken = jwtProvider.resolveToken(accessToken);
        Claims claims = jwtProvider.getUserInfoFromToken(pureToken);

        // then
        assertThat(accessToken).startsWith("Bearer ");
        assertThat(claims.getSubject()).isEqualTo(username);
        assertThat(claims.get("role")).isEqualTo(role.name());
    }

    @Test
    @DisplayName("유효한 토큰 검증 테스트")
    void validateTokenSuccessTest() {
        // given
        String accessToken = jwtProvider.createAccessToken("user", UserRole.CUSTOMER);
        String pureToken = jwtProvider.resolveToken(accessToken);

        // when
        boolean isValid = jwtProvider.validateToken(pureToken);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("변조된 토큰 검증 시 JwtException 발생 테스트")
    void validateTokenFailTest() {
        // given
        String invalidToken = "invalid.token.here";

        // when & then
        assertThatThrownBy(() -> jwtProvider.validateToken(invalidToken))
                .isInstanceOf(JwtException.class);
    }
}
