package com.dfdt.delivery.domain.ai.presentation.dto.response;

import com.dfdt.delivery.domain.ai.application.dto.RetryDescriptionResult;

import java.util.UUID;

public record RetryDescriptionResponse(
        UUID aiLogId,
        UUID sourceAiLogId,
        String responseText,
        String finalPrompt
) {
    public static RetryDescriptionResponse from(RetryDescriptionResult result) {
        return new RetryDescriptionResponse(
                result.aiLogId(),
                result.sourceAiLogId(),
                result.responseText(),
                result.finalPrompt()
        );
    }
}
