package com.dfdt.delivery.domain.ai.presentation.dto.request;

import com.dfdt.delivery.domain.ai.application.dto.GenerateImageCommand;
import com.dfdt.delivery.domain.ai.domain.entity.enums.AspectRatio;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record GenerateImageRequest(

        UUID productId,         // nullable — productName과 둘 중 하나 필수 (UseCase에서 검증)

        @Size(max = 120, message = "productName은 최대 120자까지 입력 가능합니다.")
        String productName,     // nullable

        @NotBlank(message = "prompt는 필수입니다.")
        @Size(max = 500, message = "이미지 생성 prompt는 최대 500자까지 입력 가능합니다.")
        String prompt,

        AspectRatio aspectRatio,    // nullable — 기본값 SQUARE (UseCase에서 처리)

        @Size(max = 50, message = "style은 최대 50자까지 입력 가능합니다.")
        String style,               // nullable — 이미지 스타일 자유 텍스트 (예: "수채화", "사진 실사")

        @NotNull(message = "includeText는 필수입니다.")
        Boolean includeText,    // 이미지에 텍스트 오버레이 포함 여부

        @Size(max = 50, message = "이미지 내 텍스트는 최대 50자까지 입력 가능합니다.")
        String text             // includeText=true인 경우 UseCase에서 필수 검증

) {
    public GenerateImageCommand toCommand(UUID storeId, CustomUserDetails userDetails) {
        return new GenerateImageCommand(
                storeId,
                userDetails.getUsername(),
                userDetails.getRole(),
                productId,
                productName,
                prompt,
                aspectRatio,
                style,
                Boolean.TRUE.equals(includeText),
                text
        );
    }
}
