package com.dfdt.delivery.domain.ai.presentation.dto.response;

import com.dfdt.delivery.domain.ai.application.dto.RollbackDescriptionResult;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RollbackDescriptionResponse(
        UUID aiLogId,
        UUID productId,
        String restoredDescription,   // 복원된 설명, nullable
        OffsetDateTime rolledBackAt
) {
    public static RollbackDescriptionResponse from(RollbackDescriptionResult result) {
        return new RollbackDescriptionResponse(
                result.aiLogId(),
                result.productId(),
                result.restoredDescription(),
                result.rolledBackAt()
        );
    }
}
