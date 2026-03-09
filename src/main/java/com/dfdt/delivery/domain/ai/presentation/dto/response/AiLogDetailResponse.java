package com.dfdt.delivery.domain.ai.presentation.dto.response;

import com.dfdt.delivery.domain.ai.application.dto.AiLogDetailResult;
import com.dfdt.delivery.domain.ai.domain.entity.enums.AiRequestType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AiLogDetailResponse(
        UUID aiLogId,
        UUID storeId,
        UUID productId,
        String requestedBy,
        AiRequestType requestType,
        String tone,
        String inputPrompt,
        String finalPrompt,
        String responseText,
        Boolean isSuccess,
        String errorCode,
        String errorMessage,
        String modelName,
        Integer responseTimeMs,
        Integer promptCharCount,
        Integer responseCharCount,
        Boolean isApplied,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime appliedAt,
        String appliedBy,
        UUID sourceAiLogId,
        String previousDescription,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime rolledBackAt,
        String rolledBackBy,
        String keywordsSnapshot,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime createdAt
) {
    public static AiLogDetailResponse from(AiLogDetailResult result) {
        return new AiLogDetailResponse(
                result.aiLogId(),
                result.storeId(),
                result.productId(),
                result.requestedBy(),
                result.requestType(),
                result.tone(),
                result.inputPrompt(),
                result.finalPrompt(),
                result.responseText(),
                result.isSuccess(),
                result.errorCode(),
                result.errorMessage(),
                result.modelName(),
                result.responseTimeMs(),
                result.promptCharCount(),
                result.responseCharCount(),
                result.isApplied(),
                result.appliedAt(),
                result.appliedBy(),
                result.sourceAiLogId(),
                result.previousDescription(),
                result.rolledBackAt(),
                result.rolledBackBy(),
                result.keywordsSnapshot(),
                result.createdAt()
        );
    }
}