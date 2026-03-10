package com.dfdt.delivery.domain.ai.application.dto;

import com.dfdt.delivery.domain.ai.domain.entity.AiLogEntity;
import com.dfdt.delivery.domain.ai.domain.entity.enums.AiRequestType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AiLogDetailResult(
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
        OffsetDateTime appliedAt,
        String appliedBy,
        UUID sourceAiLogId,
        String previousDescription,
        OffsetDateTime rolledBackAt,
        String rolledBackBy,
        String keywordsSnapshot,
        OffsetDateTime createdAt
) {
    public static AiLogDetailResult from(AiLogEntity entity) {
        return new AiLogDetailResult(
                entity.getAiLogId(),
                entity.getStoreId(),
                entity.getProductId(),
                entity.getRequestedBy(),
                entity.getRequestType(),
                entity.getTone(),
                entity.getInputPrompt(),
                entity.getFinalPrompt(),
                entity.getResponseText(),
                entity.getIsSuccess(),
                entity.getErrorCode(),
                entity.getErrorMessage(),
                entity.getModelName(),
                entity.getResponseTimeMs(),
                entity.getPromptCharCount(),
                entity.getResponseCharCount(),
                entity.getIsApplied(),
                entity.getAppliedAt(),
                entity.getAppliedBy(),
                entity.getSourceAiLogId(),
                entity.getPreviousDescription(),
                entity.getRolledBackAt(),
                entity.getRolledBackBy(),
                entity.getKeywordsSnapshot(),
                entity.getCreateAudit() != null ? entity.getCreateAudit().getCreatedAt() : null
        );
    }
}