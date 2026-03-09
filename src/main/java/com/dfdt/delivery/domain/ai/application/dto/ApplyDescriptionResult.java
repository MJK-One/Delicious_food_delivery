package com.dfdt.delivery.domain.ai.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * AI 생성 상품 설명 적용 UseCase 결과.
 * UseCase → Controller 방향으로 전달됩니다.
 */
public record ApplyDescriptionResult(
        UUID aiLogId,
        UUID productId,
        String appliedDescription,
        OffsetDateTime appliedAt
) {
}
