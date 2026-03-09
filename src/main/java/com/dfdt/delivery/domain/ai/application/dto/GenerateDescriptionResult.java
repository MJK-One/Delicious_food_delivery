package com.dfdt.delivery.domain.ai.application.dto;

import java.util.UUID;

/**
 * 상품 설명 AI 생성 UseCase 출력 결과.
 * UseCase → Controller 방향으로 반환됩니다.
 */
public record GenerateDescriptionResult(
        UUID aiLogId,
        String responseText,  // AI 생성 텍스트 (trimResponse 적용 후)
        String finalPrompt    // 실제로 AI에 전달된 최종 프롬프트 (디버깅/로그 확인용)
) {
}
