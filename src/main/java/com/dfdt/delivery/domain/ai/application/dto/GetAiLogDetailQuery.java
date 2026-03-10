package com.dfdt.delivery.domain.ai.application.dto;

import com.dfdt.delivery.domain.user.domain.enums.UserRole;

import java.util.UUID;

public record GetAiLogDetailQuery(
        UUID storeId,
        UUID aiLogId,
        String requestedBy,
        UserRole requestedByRole
) {}