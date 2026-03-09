package com.dfdt.delivery.domain.ai.application.dto;

import java.time.OffsetDateTime;

public record AiHealthResult(
        String status,          // "UP" | "DOWN"
        String modelName,
        Integer responseTimeMs,
        String errorMessage,    // UP 시 null
        OffsetDateTime checkedAt
) {}
