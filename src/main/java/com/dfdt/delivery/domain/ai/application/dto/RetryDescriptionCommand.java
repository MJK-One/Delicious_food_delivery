package com.dfdt.delivery.domain.ai.application.dto;

import com.dfdt.delivery.domain.user.domain.enums.UserRole;

import java.util.UUID;

/**
 * AI 로그 재실행 UseCase 입력 커맨드.
 * Controller → UseCase 방향으로 전달됩니다.
 */
public record RetryDescriptionCommand(
        UUID storeId,
        UUID sourceAiLogId,        // 재실행 원본 AI 로그 ID
        String requestedBy,        // 인증된 사용자 username
        UserRole requestedByRole,  // 소유권 체크에 사용 (OWNER만 storeId 검증)
        String overrideInputPrompt // nullable — null이면 원본 inputPrompt 사용
) {
}
