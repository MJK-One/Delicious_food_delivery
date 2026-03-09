package com.dfdt.delivery.domain.ai.application.dto;

import com.dfdt.delivery.domain.user.domain.enums.UserRole;

import java.util.UUID;

public record SearchAiLogsQuery(
        UUID storeId,
        String requestedBy,
        UserRole requestedByRole,
        UUID productId,       // nullable — productId 필터
        Boolean isApplied,    // nullable — 적용 여부 필터
        Boolean isSuccess,    // nullable — 성공 여부 필터
        int page,
        int size,
        String sortBy,
        boolean isAsc
) {}