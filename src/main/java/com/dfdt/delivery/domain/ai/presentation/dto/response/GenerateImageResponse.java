package com.dfdt.delivery.domain.ai.presentation.dto.response;

import com.dfdt.delivery.domain.ai.application.dto.GenerateImageResult;

import java.util.UUID;

public record GenerateImageResponse(
        UUID aiLogId,
        String imageData,  // base64 인코딩된 이미지 데이터
        String mimeType    // e.g., "image/png"
) {
    public static GenerateImageResponse from(GenerateImageResult result) {
        return new GenerateImageResponse(
                result.aiLogId(),
                result.imageData(),
                result.mimeType()
        );
    }
}
