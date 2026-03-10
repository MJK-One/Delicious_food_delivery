package com.dfdt.delivery.domain.ai.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yaml의 ai.gemini 설정값을 바인딩합니다.
 *
 * ai:
 *   gemini:
 *     api-key: ...
 *     model: gemini-2.0-flash
 *     image-model: gemini-2.0-flash-exp
 *     base-url: https://generativelanguage.googleapis.com
 */
@ConfigurationProperties(prefix = "ai.gemini")
public record GeminiProperties(
        String apiKey,
        String model,
        String baseUrl,
        String imageModel  // 이미지 생성 전용 모델 (responseModalities: IMAGE 지원 모델)
) {
}
