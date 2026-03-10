package com.dfdt.delivery.domain.ai.presentation.dto.request;

import jakarta.validation.constraints.Size;

public record RetryDescriptionRequest(

        @Size(max = 300, message = "overrideInputPrompt는 최대 300자까지 입력 가능합니다.")
        String overrideInputPrompt  // nullable — null이면 원본 inputPrompt 사용

) {
}
