package com.dfdt.delivery.domain.auth.infrastructure.jwt;

import com.dfdt.delivery.common.config.SecurityConfig;
import com.dfdt.delivery.common.config.WebConfig;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import com.dfdt.delivery.domain.user.domain.repository.UserRepository;
import com.dfdt.delivery.domain.user.presentation.TestController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TestController.class)
@Import({SecurityConfig.class, WebConfig.class, JwtProvider.class})
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetailsService userDetailsService;

    @MockBean
    private com.dfdt.delivery.common.util.RedisService redisService;

    @Test
    @DisplayName("유효한 JWT 토큰으로 API 호출 시 인증 성공")
    void apiAccessSuccessWithValidToken() throws Exception {
        // given
        String username = "testuser";
        User user = User.builder()
                .username(username)
                .name("홍길동")
                .password("password")
                .role(UserRole.CUSTOMER)
                .build();
        
        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        given(userDetailsService.loadUserByUsername(username)).willReturn(customUserDetails);
        given(redisService.hasKey(anyString())).willReturn(false); // 블랙리스트 아님

        String accessToken = jwtProvider.createAccessToken(username, UserRole.CUSTOMER);

        // when & then
        mockMvc.perform(get("/api/v1/api/test/me")
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello 홍길동 (testuser)"));
    }

    @Test
    @DisplayName("잘못된 토큰으로 API 호출 시 JwtExceptionFilter에서 에러 응답 반환")
    void apiAccessFailWithInvalidToken() throws Exception {
        // given
        String invalidToken = "Bearer invalid.token.here";

        // when & then
        mockMvc.perform(get("/api/v1/api/test/me")
                        .header("Authorization", invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH-4014"))
                .andExpect(jsonPath("$.message").value("유효하지 않거나 손상된 Access Token입니다."));
    }
}
