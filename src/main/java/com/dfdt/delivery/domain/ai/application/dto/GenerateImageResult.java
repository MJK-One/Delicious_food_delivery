package com.dfdt.delivery.domain.ai.application.dto;

import java.util.UUID;

/**
 * 음식 이미지 AI 생성 UseCase 출력 결과.
 * UseCase → Controller 방향으로 반환됩니다.
 */
public record GenerateImageResult(
        UUID aiLogId,
        String imageData,  // base64 인코딩된 이미지 데이터
        String mimeType    // e.g., "image/png"
) {
}
