package com.dfdt.delivery.domain.ai.application.dto;

import com.dfdt.delivery.domain.ai.domain.entity.enums.Tone;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;

import java.util.List;
import java.util.UUID;

/**
 * 상품 설명 AI 생성 UseCase 입력 커맨드.
 * Controller → UseCase 방향으로 전달됩니다.
 */
public record GenerateDescriptionCommand(
        UUID storeId,
        String requestedBy,        // 인증된 사용자 username
        UserRole requestedByRole,  // 소유권 체크에 사용 (OWNER만 storeId 검증)
        UUID productId,            // nullable - 없으면 productName으로 미리보기
        String productName,        // nullable - productId 없을 때 사용
        String inputPrompt,
        Tone tone,
        List<String> keywords      // nullable
) {
}
