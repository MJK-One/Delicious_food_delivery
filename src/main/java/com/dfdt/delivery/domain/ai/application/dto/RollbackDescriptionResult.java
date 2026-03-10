package com.dfdt.delivery.domain.ai.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * AI 설명 원복 결과.
 */
public record RollbackDescriptionResult(
        UUID aiLogId,
        UUID productId,
        String restoredDescription,   // 복원된 설명 (apply 이전 값), nullable
        OffsetDateTime rolledBackAt
) {
}
