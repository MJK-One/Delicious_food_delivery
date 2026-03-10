package com.dfdt.delivery.domain.ai.presentation.dto.response;

import com.dfdt.delivery.domain.ai.application.dto.AiStatsResult;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AiStatsResponse(
        UUID storeId,
        long totalCount,
        long successCount,
        long failureCount,
        double successRate,       // 0.0 ~ 100.0 (%, 소수점 1자리)
        Long avgResponseTimeMs,   // nullable
        OffsetDateTime fromDateTime,
        OffsetDateTime toDateTime
) {
    public static AiStatsResponse from(AiStatsResult result) {
        return new AiStatsResponse(
                result.storeId(),
                result.totalCount(),
                result.successCount(),
                result.failureCount(),
                result.successRate(),
                result.avgResponseTimeMs(),
                result.fromDateTime(),
                result.toDateTime()
        );
    }
}
