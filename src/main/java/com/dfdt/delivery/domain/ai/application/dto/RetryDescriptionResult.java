package com.dfdt.delivery.domain.ai.application.dto;

import java.util.UUID;

/**
 * AI 로그 재실행 UseCase 출력 결과.
 * UseCase → Controller 방향으로 반환됩니다.
 */
public record RetryDescriptionResult(
        UUID aiLogId,         // 새로 생성된 로그 ID
        UUID sourceAiLogId,   // 원본 로그 ID
        String responseText,  // AI 생성 텍스트 (trimResponse 적용 후)
        String finalPrompt    // 실제로 AI에 전달된 최종 프롬프트
) {
}