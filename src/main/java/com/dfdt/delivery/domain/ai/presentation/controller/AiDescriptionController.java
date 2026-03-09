package com.dfdt.delivery.domain.ai.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.ai.application.dto.AiLogDetailResult;
import com.dfdt.delivery.domain.ai.application.dto.AiLogSummaryResult;
import com.dfdt.delivery.domain.ai.application.dto.ApplyDescriptionCommand;
import com.dfdt.delivery.domain.ai.application.dto.ApplyDescriptionResult;
import com.dfdt.delivery.domain.ai.application.dto.GenerateDescriptionCommand;
import com.dfdt.delivery.domain.ai.application.dto.GenerateDescriptionResult;
import com.dfdt.delivery.domain.ai.application.dto.GetAiLogDetailQuery;
import com.dfdt.delivery.domain.ai.application.dto.SearchAiLogsQuery;
import com.dfdt.delivery.domain.ai.application.usecase.ApplyDescriptionUseCase;
import com.dfdt.delivery.domain.ai.application.usecase.GenerateDescriptionUseCase;
import com.dfdt.delivery.domain.ai.application.usecase.GetAiLogDetailUseCase;
import com.dfdt.delivery.domain.ai.application.usecase.SearchAiLogsUseCase;
import com.dfdt.delivery.domain.ai.presentation.dto.request.GenerateDescriptionRequest;
import com.dfdt.delivery.domain.ai.presentation.dto.response.AiLogDetailResponse;
import com.dfdt.delivery.domain.ai.presentation.dto.response.AiLogSummaryResponse;
import com.dfdt.delivery.domain.ai.presentation.dto.response.ApplyDescriptionResponse;
import com.dfdt.delivery.domain.ai.presentation.dto.response.GenerateDescriptionResponse;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    private final ApplyDescriptionUseCase applyDescriptionUseCase;
    private final SearchAiLogsUseCase searchAiLogsUseCase;
    private final GetAiLogDetailUseCase getAiLogDetailUseCase;

    /**
     * AI 로그 목록 조회 (API-AI-101)
     * GET /api/v1/ai/stores/{storeId}/logs
     *
     * - OWNER: 본인 가게 로그만 조회 가능 (UseCase에서 소유권 체크)
     * - MASTER: 모든 가게 로그 조회 가능
     */
    @GetMapping("/stores/{storeId}/logs")
    @PreAuthorize("hasAnyRole('OWNER', 'MASTER')")
    public ResponseEntity<ApiResponseDto<Page<AiLogSummaryResponse>>> searchAiLogs(
            @PathVariable UUID storeId,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) Boolean isApplied,
            @RequestParam(required = false) Boolean isSuccess,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "false") boolean isAsc,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        SearchAiLogsQuery query = new SearchAiLogsQuery(
                storeId, userDetails.getUsername(), userDetails.getRole(),
                productId, isApplied, isSuccess, page, size, sortBy, isAsc
        );
        Page<AiLogSummaryResult> results = searchAiLogsUseCase.execute(query);

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "AI 로그 목록을 조회했습니다.",
                results.map(AiLogSummaryResponse::from)
        );
    }

    /**
     * AI 로그 상세 조회 (API-AI-102)
     * GET /api/v1/ai/stores/{storeId}/logs/{aiLogId}
     *
     * - OWNER: 본인 가게 로그만 조회 가능 (UseCase에서 소유권 체크)
     * - MASTER: 모든 가게 로그 조회 가능
     */
    @GetMapping("/stores/{storeId}/logs/{aiLogId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MASTER')")
    public ResponseEntity<ApiResponseDto<AiLogDetailResponse>> getAiLogDetail(
            @PathVariable UUID storeId,
            @PathVariable UUID aiLogId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        GetAiLogDetailQuery query = new GetAiLogDetailQuery(
                storeId, aiLogId, userDetails.getUsername(), userDetails.getRole()
        );
        AiLogDetailResult result = getAiLogDetailUseCase.execute(query);

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "AI 로그 상세 정보를 조회했습니다.",
                AiLogDetailResponse.from(result)
        );
    }

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

    /**
     * AI 생성 상품 설명 적용 (API-AI-002)
     * PATCH /api/v1/ai/stores/{storeId}/descriptions/{aiLogId}/apply
     *
     * - OWNER: 본인 가게만 적용 가능 (UseCase에서 소유권 체크)
     * - MASTER: 모든 가게 적용 가능
     */
    @PatchMapping("/stores/{storeId}/descriptions/{aiLogId}/apply")
    @PreAuthorize("hasAnyRole('OWNER', 'MASTER')")
    public ResponseEntity<ApiResponseDto<ApplyDescriptionResponse>> applyDescriptionPreview(
            @PathVariable UUID storeId,
            @PathVariable UUID aiLogId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ApplyDescriptionCommand command = new ApplyDescriptionCommand(
                storeId, aiLogId, userDetails.getUsername(), userDetails.getRole()
        );
        ApplyDescriptionResult result = applyDescriptionUseCase.execute(command);

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "AI 상품 설명이 적용되었습니다.",
                ApplyDescriptionResponse.from(result)
        );
    }
}
