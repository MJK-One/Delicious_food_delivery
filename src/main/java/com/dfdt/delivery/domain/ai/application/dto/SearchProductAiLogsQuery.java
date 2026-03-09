package com.dfdt.delivery.domain.ai.application.dto;

import com.dfdt.delivery.domain.user.domain.enums.UserRole;

import java.util.UUID;

public record SearchProductAiLogsQuery(
        UUID storeId,
        UUID productId,
        String requestedBy,
        UserRole requestedByRole,
        Boolean isApplied,
        Boolean isSuccess,
        int page,
        int size,
        String sortBy,
        boolean isAsc
) {}
