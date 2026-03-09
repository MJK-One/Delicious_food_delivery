package com.dfdt.delivery.domain.ai.application.dto;

import com.dfdt.delivery.domain.user.domain.enums.UserRole;

import java.util.UUID;

/**
 * AI 생성 상품 설명 적용 UseCase 입력 커맨드.
 * Controller → UseCase 방향으로 전달됩니다.
 */
public record ApplyDescriptionCommand(
        UUID storeId,
        UUID aiLogId,
        String requestedBy,       // 인증된 사용자 username
        UserRole requestedByRole  // 소유권 체크에 사용 (OWNER만 storeId 검증)
) {
}
