package com.dfdt.delivery.domain.ai.application.dto;

import com.dfdt.delivery.domain.ai.domain.entity.enums.AspectRatio;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;

import java.util.UUID;

/**
 * 음식 이미지 AI 생성 UseCase 입력 커맨드.
 * Controller → UseCase 방향으로 전달됩니다.
 */
public record GenerateImageCommand(
        UUID storeId,
        String requestedBy,        // 인증된 사용자 username
        UserRole requestedByRole,  // 소유권 체크에 사용 (OWNER만 storeId 검증)
        UUID productId,            // nullable — productName과 둘 중 하나 필수
        String productName,        // nullable — productId 없을 때 사용
        String prompt,             // 이미지 생성 프롬프트 (max 500)
        AspectRatio aspectRatio,   // nullable — 기본값 SQUARE (UseCase에서 처리)
        String style,              // nullable — 이미지 스타일 자유 텍스트 (max 50)
        boolean includeText,       // 이미지에 텍스트 오버레이 포함 여부
        String text                // includeText=true일 때 필수 (max 50)
) {
}
