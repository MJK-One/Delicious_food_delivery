package com.dfdt.delivery.domain.ai.application.dto;

import com.dfdt.delivery.domain.ai.domain.entity.enums.AiRequestType;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 가게별/기간별 AI 호출 통계 UseCase 입력 쿼리.
 */
public record AiStatsQuery(
        UUID storeId,
        OffsetDateTime fromDateTime,   // nullable — 시작 시간 (포함)
        OffsetDateTime toDateTime,     // nullable — 종료 시간 (포함)
        AiRequestType requestType      // nullable — 요청 타입 필터
) {
}
