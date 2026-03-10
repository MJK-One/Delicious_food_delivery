package com.dfdt.delivery.domain.ai.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.ai.application.dto.AiHealthResult;
import com.dfdt.delivery.domain.ai.application.dto.AiLogDetailResult;
import com.dfdt.delivery.domain.ai.application.dto.AiLogSummaryResult;
import com.dfdt.delivery.domain.ai.application.dto.ApplyDescriptionCommand;
import com.dfdt.delivery.domain.ai.application.dto.ApplyDescriptionResult;
import com.dfdt.delivery.domain.ai.application.dto.GenerateDescriptionCommand;
import com.dfdt.delivery.domain.ai.application.dto.GenerateDescriptionResult;
import com.dfdt.delivery.domain.ai.application.dto.GetAiLogDetailQuery;
import com.dfdt.delivery.domain.ai.application.dto.SearchAiLogsQuery;
import com.dfdt.delivery.domain.ai.application.dto.SearchProductAiLogsQuery;
import com.dfdt.delivery.domain.ai.application.usecase.ApplyDescriptionUseCase;
import com.dfdt.delivery.domain.ai.application.usecase.CheckAiHealthUseCase;
import com.dfdt.delivery.domain.ai.application.usecase.GenerateDescriptionUseCase;
import com.dfdt.delivery.domain.ai.application.usecase.GetAiLogDetailUseCase;
import com.dfdt.delivery.domain.ai.application.usecase.GetPromptRulesUseCase;
import com.dfdt.delivery.domain.ai.application.usecase.SearchAiLogsUseCase;
import com.dfdt.delivery.domain.ai.application.usecase.SearchProductAiLogsUseCase;
import com.dfdt.delivery.domain.ai.presentation.dto.request.GenerateDescriptionRequest;
import com.dfdt.delivery.domain.ai.presentation.dto.response.AiHealthResponse;
import com.dfdt.delivery.domain.ai.presentation.dto.response.AiLogDetailResponse;
import com.dfdt.delivery.domain.ai.presentation.dto.response.AiLogSummaryResponse;
import com.dfdt.delivery.domain.ai.presentation.dto.response.AiPromptRulesResponse;
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
    private final SearchProductAiLogsUseCase searchProductAiLogsUseCase;
    private final CheckAiHealthUseCase checkAiHealthUseCase;
    private final GetPromptRulesUseCase getPromptRulesUseCase;

    /**
     * AI 연동 상태 확인 (API-AI-201)
     * GET /api/v1/ai/health
     *
     * - MASTER: Gemini API 연결 상태 확인 (UP/DOWN)
     * - 항상 HTTP 200 반환, 상태는 응답 body의 status 필드로 구분
     */
    @GetMapping("/health")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponseDto<AiHealthResponse>> checkAiHealth() {
        AiHealthResult result = checkAiHealthUseCase.execute();
        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "AI 연동 상태를 조회했습니다.",
                AiHealthResponse.from(result)
        );
    }

    /**
     * AI 프롬프트 규칙 미리보기 (API-AI-202)
     * GET /api/v1/ai/prompt-rules
     *
     * - OWNER / MASTER: 프롬프트 구성 규칙(제약 조건, 톤 목록, 강제 문구, 템플릿) 조회
     * - 외부 API 호출 없음, 항상 HTTP 200 반환
     */
    @GetMapping("/prompt-rules")
    @PreAuthorize("hasAnyRole('OWNER', 'MASTER')")
    public ResponseEntity<ApiResponseDto<AiPromptRulesResponse>> getPromptRules() {
        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "AI 프롬프트 규칙을 조회했습니다.",
                AiPromptRulesResponse.from(getPromptRulesUseCase.execute())
        );
    }

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
     * 상품 기준 AI 로그 목록 조회 (API-AI-103)
     * GET /api/v1/ai/stores/{storeId}/products/{productId}/logs
     *
     * - OWNER: 본인 가게 상품 로그만 조회 가능 (UseCase에서 소유권 체크)
     * - MASTER: 모든 가게 상품 로그 조회 가능
     */
    @GetMapping("/stores/{storeId}/products/{productId}/logs")
    @PreAuthorize("hasAnyRole('OWNER', 'MASTER')")
    public ResponseEntity<ApiResponseDto<Page<AiLogSummaryResponse>>> searchProductAiLogs(
            @PathVariable UUID storeId,
            @PathVariable UUID productId,
            @RequestParam(required = false) Boolean isApplied,
            @RequestParam(required = false) Boolean isSuccess,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "false") boolean isAsc,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        SearchProductAiLogsQuery query = new SearchProductAiLogsQuery(
                storeId, productId, userDetails.getUsername(), userDetails.getRole(),
                isApplied, isSuccess, page, size, sortBy, isAsc
        );
        Page<AiLogSummaryResult> results = searchProductAiLogsUseCase.execute(query);

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "상품 AI 로그 목록을 조회했습니다.",
                results.map(AiLogSummaryResponse::from)
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
