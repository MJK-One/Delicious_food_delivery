package com.dfdt.delivery.domain.ai.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.ai.application.dto.GenerateDescriptionCommand;
import com.dfdt.delivery.domain.ai.application.dto.GenerateDescriptionResult;
import com.dfdt.delivery.domain.ai.application.usecase.GenerateDescriptionUseCase;
import com.dfdt.delivery.domain.ai.presentation.dto.request.GenerateDescriptionRequest;
import com.dfdt.delivery.domain.ai.presentation.dto.response.GenerateDescriptionResponse;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AiDescriptionController {

    private final GenerateDescriptionUseCase generateDescriptionUseCase;

    /**
     * AI 상품 설명 미리보기 생성 (API-AI-001)
     * POST /api/v1/ai/stores/{storeId}/descriptions/preview
     *
     * - OWNER: 본인 가게만 요청 가능 (UseCase에서 소유권 체크)
     * - MASTER: 모든 가게 요청 가능
     */
    @PostMapping("/stores/{storeId}/descriptions/preview")
    @PreAuthorize("hasAnyRole('OWNER', 'MASTER')")
    public ResponseEntity<ApiResponseDto<GenerateDescriptionResponse>> generateDescriptionPreview(
            @PathVariable UUID storeId,
            @Valid @RequestBody GenerateDescriptionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        GenerateDescriptionCommand command = request.toCommand(storeId, userDetails);
        GenerateDescriptionResult result = generateDescriptionUseCase.execute(command);

        return ApiResponseDto.success(
                HttpStatus.CREATED.value(),
                "AI 상품 설명이 생성되었습니다.",
                GenerateDescriptionResponse.from(result)
        );
    }
}
