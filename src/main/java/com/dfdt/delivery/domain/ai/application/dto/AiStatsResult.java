package com.dfdt.delivery.domain.ai.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 가게별/기간별 AI 호출 통계 결과.
 */
public record AiStatsResult(
        UUID storeId,
        long totalCount,           // 총 AI 호출 수
        long successCount,         // 성공 수
        long failureCount,         // 실패 수
        double successRate,        // 성공률 (%, 소수점 1자리)
        Long avgResponseTimeMs,    // 평균 응답 시간 (ms) — 성공 로그 기준, nullable
        OffsetDateTime fromDateTime,
        OffsetDateTime toDateTime
) {
}
