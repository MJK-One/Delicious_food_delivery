package com.dfdt.delivery.domain.ai.presentation.dto.response;

import com.dfdt.delivery.domain.ai.application.dto.ApplyDescriptionResult;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ApplyDescriptionResponse(
        UUID aiLogId,
        UUID productId,
        String appliedDescription,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime appliedAt
) {
    public static ApplyDescriptionResponse from(ApplyDescriptionResult result) {
        return new ApplyDescriptionResponse(
                result.aiLogId(),
                result.productId(),
                result.appliedDescription(),
                result.appliedAt()
        );
    }
}