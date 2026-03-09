package com.dfdt.delivery.domain.ai.presentation.dto.response;

import com.dfdt.delivery.domain.ai.application.dto.AiHealthResult;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

public record AiHealthResponse(
        String status,
        String modelName,
        Integer responseTimeMs,
        String errorMessage,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime checkedAt
) {
    public static AiHealthResponse from(AiHealthResult result) {
        return new AiHealthResponse(
                result.status(),
                result.modelName(),
                result.responseTimeMs(),
                result.errorMessage(),
                result.checkedAt()
        );
    }
}
