package com.dfdt.delivery.domain.ai.presentation.dto.response;

import com.dfdt.delivery.domain.ai.application.dto.GenerateDescriptionResult;

import java.util.UUID;

public record GenerateDescriptionResponse(
        UUID aiLogId,
        String responseText,
        String finalPrompt
) {
    public static GenerateDescriptionResponse from(GenerateDescriptionResult result) {
        return new GenerateDescriptionResponse(
                result.aiLogId(),
                result.responseText(),
                result.finalPrompt()
        );
    }
}
