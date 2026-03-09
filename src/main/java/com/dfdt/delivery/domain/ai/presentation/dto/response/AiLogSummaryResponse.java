package com.dfdt.delivery.domain.ai.presentation.dto.response;

import com.dfdt.delivery.domain.ai.application.dto.AiLogSummaryResult;
import com.dfdt.delivery.domain.ai.domain.entity.enums.AiRequestType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AiLogSummaryResponse(
        UUID aiLogId,
        UUID productId,
        String requestedBy,
        AiRequestType requestType,
        String tone,
        Boolean isSuccess,
        Boolean isApplied,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime appliedAt,
        String responseText,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime createdAt
) {
    public static AiLogSummaryResponse from(AiLogSummaryResult result) {
        return new AiLogSummaryResponse(
                result.aiLogId(),
                result.productId(),
                result.requestedBy(),
                result.requestType(),
                result.tone(),
                result.isSuccess(),
                result.isApplied(),
                result.appliedAt(),
                result.responseText(),
                result.createdAt()
        );
    }
}
