package com.dfdt.delivery.domain.ai.application.dto;

import com.dfdt.delivery.domain.ai.domain.entity.enums.AiRequestType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AiLogSummaryResult(
        UUID aiLogId,
        UUID productId,
        String requestedBy,
        AiRequestType requestType,
        String tone,
        Boolean isSuccess,
        Boolean isApplied,
        OffsetDateTime appliedAt,
        String responseText,
        OffsetDateTime createdAt
) {}
