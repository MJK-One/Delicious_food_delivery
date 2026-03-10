package com.dfdt.delivery.domain.ai.application.dto;

import com.dfdt.delivery.domain.user.domain.enums.UserRole;

import java.util.UUID;

/**
 * AI 설명 원복 UseCase 입력 커맨드.
 */
public record RollbackDescriptionCommand(
        UUID storeId,
        UUID aiLogId,
        String requestedBy,
        UserRole requestedByRole
) {
}
