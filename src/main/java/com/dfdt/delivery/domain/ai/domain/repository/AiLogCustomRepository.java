package com.dfdt.delivery.domain.ai.domain.repository;

import com.dfdt.delivery.domain.ai.application.dto.AiLogSummaryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AiLogCustomRepository {

    Page<AiLogSummaryResult> searchAiLogs(
            UUID storeId,
            UUID productId,
            Boolean isApplied,
            Boolean isSuccess,
            Pageable pageable
    );
}