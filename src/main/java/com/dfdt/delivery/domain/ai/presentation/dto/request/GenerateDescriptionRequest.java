package com.dfdt.delivery.domain.ai.presentation.dto.request;

import com.dfdt.delivery.domain.ai.application.dto.GenerateDescriptionCommand;
import com.dfdt.delivery.domain.ai.domain.entity.enums.Tone;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record GenerateDescriptionRequest(

        UUID productId,         // nullable — productName과 둘 중 하나 필수 (UseCase에서 검증)

        @Size(max = 120, message = "productName은 최대 120자까지 입력 가능합니다.")
        String productName,     // nullable

        @NotBlank(message = "inputPrompt는 필수입니다.")
        @Size(max = 300, message = "inputPrompt는 최대 300자까지 입력 가능합니다.")
        String inputPrompt,

        @NotNull(message = "tone은 필수입니다.")
        Tone tone,

        @Size(max = 10, message = "keywords는 최대 10개까지 입력 가능합니다.")
        List<@NotBlank @Size(max = 30, message = "keyword 항목은 최대 30자까지 입력 가능합니다.") String> keywords

) {
    /**
     * 컨트롤러에서 UseCase 커맨드 객체로 변환합니다.
     */
    public GenerateDescriptionCommand toCommand(UUID storeId, CustomUserDetails userDetails) {
        return new GenerateDescriptionCommand(
                storeId,
                userDetails.getUsername(),
                userDetails.getRole(),
                productId,
                productName,
                inputPrompt,
                tone,
                keywords
        );
    }
}
