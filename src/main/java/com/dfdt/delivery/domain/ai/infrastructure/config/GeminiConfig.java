package com.dfdt.delivery.domain.ai.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
public class GeminiConfig {

    /**
     * Gemini API 전용 RestClient 빈.
     * 기본 URL만 미리 설정하고, API 키는 요청 시 쿼리 파라미터로 추가합니다.
     */
    @Bean("geminiRestClient")
    public RestClient geminiRestClient(GeminiProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
