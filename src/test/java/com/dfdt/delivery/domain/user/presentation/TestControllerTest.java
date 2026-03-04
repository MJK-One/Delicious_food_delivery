package com.dfdt.delivery.domain.user.presentation;

import com.dfdt.delivery.common.config.SecurityConfig;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestController.class)
@Import(SecurityConfig.class)
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("AuthenticationPrincipal - CustomUserDetails 정보 주입 확인")
    void getMeTest() throws Exception {
        // given
        User user = User.builder()
                .username("testuser")
                .name("홍길동")
                .password("password")
                .role(UserRole.CUSTOMER)
                .build();
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        // when & then
        mockMvc.perform(get("/api/v1/test/me")
                        .with(user(customUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello 홍길동 testuser"));
    }
}