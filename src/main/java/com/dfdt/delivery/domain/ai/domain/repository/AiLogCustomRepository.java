package com.dfdt.delivery.domain.ai.domain.repository;

import com.dfdt.delivery.domain.ai.application.dto.AiLogSummaryResult;
import com.dfdt.delivery.domain.ai.application.dto.AiStatsResult;
import com.dfdt.delivery.domain.ai.domain.entity.enums.AiRequestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface AiLogCustomRepository {

    Page<AiLogSummaryResult> searchAiLogs(
            UUID storeId,
            UUID productId,
            Boolean isApplied,
            Boolean isSuccess,
            Pageable pageable
    );

    /**
     * 가게별/기간별 AI 호출 통계를 집계합니다.
     *
     * @param storeId     가게 ID
     * @param from        시작 시간 (nullable)
     * @param to          종료 시간 (nullable)
     * @param requestType 요청 타입 필터 (nullable)
     */
    AiStatsResult getAiStats(UUID storeId, OffsetDateTime from, OffsetDateTime to, AiRequestType requestType);
}