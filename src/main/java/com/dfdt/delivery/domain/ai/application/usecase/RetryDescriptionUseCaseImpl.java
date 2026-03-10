package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.application.dto.RetryDescriptionCommand;
import com.dfdt.delivery.domain.ai.application.dto.RetryDescriptionResult;
import com.dfdt.delivery.domain.ai.domain.client.GeminiClient;
import com.dfdt.delivery.domain.ai.domain.entity.AiLogEntity;
import com.dfdt.delivery.domain.ai.domain.entity.enums.AiRequestType;
import com.dfdt.delivery.domain.ai.domain.entity.enums.Tone;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
import com.dfdt.delivery.domain.ai.domain.policy.AiPromptPolicy;
import com.dfdt.delivery.domain.ai.domain.repository.AiLogRepository;
import com.dfdt.delivery.domain.product.domain.entity.Product;
import com.dfdt.delivery.domain.product.domain.repository.ProductRepository;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.repository.StoreRepository;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RetryDescriptionUseCaseImpl implements RetryDescriptionUseCase {

    private final AiLogRepository aiLogRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final GeminiClient geminiClient;
    private final AiPromptPolicy aiPromptPolicy;

    @Override
    @Transactional
    public RetryDescriptionResult execute(RetryDescriptionCommand command) {

        // 1. 원본 AI 로그 조회
        AiLogEntity sourceLog = aiLogRepository.findById(command.sourceAiLogId())
                .orElseThrow(() -> new BusinessException(AiErrorCode.AI_LOG_NOT_FOUND));

        // 2. storeId 일치 검증 (URL 위변조 방지)
        if (!sourceLog.getStoreId().equals(command.storeId())) {
            throw new BusinessException(AiErrorCode.STORE_NOT_FOUND);
        }

        // 3. OWNER 권한: 본인 가게인지 소유권 확인
        if (command.requestedByRole() == UserRole.OWNER) {
            Store store = storeRepository.findByStoreIdAndNotDeleted(command.storeId())
                    .orElseThrow(() -> new BusinessException(AiErrorCode.STORE_NOT_FOUND));
            if (!store.getUser().getUsername().equals(command.requestedBy())) {
                throw new BusinessException(AiErrorCode.STORE_ACCESS_DENIED);
            }
        }

        // 4. PRODUCT_DESCRIPTION 타입만 재실행 지원
        if (sourceLog.getRequestType() != AiRequestType.PRODUCT_DESCRIPTION) {
            throw new BusinessException(AiErrorCode.RETRY_NOT_SUPPORTED_TYPE);
        }

        // 5. 입력 프롬프트 결정: override가 있으면 사용, 없으면 원본 사용
        String inputPrompt = (command.overrideInputPrompt() != null && !command.overrideInputPrompt().isBlank())
                ? command.overrideInputPrompt()
                : sourceLog.getInputPrompt();

        // 6. Tone 파싱 (원본 로그의 tone 문자열 → Tone enum)
        Tone tone = Tone.valueOf(sourceLog.getTone());

        // 7. keywords 파싱 (쉼표 구분 문자열 → List)
        List<String> keywords = null;
        if (sourceLog.getKeywordsSnapshot() != null && !sourceLog.getKeywordsSnapshot().isBlank()) {
            keywords = Arrays.stream(sourceLog.getKeywordsSnapshot().split(","))
                    .filter(k -> !k.isBlank())
                    .toList();
        }

        // 8. 상품명 조회 (productId가 있는 경우)
        String productName = null;
        if (sourceLog.getProductId() != null) {
            Product product = productRepository.findByProductIdAndStoreId(
                    sourceLog.getProductId(), command.storeId())
                    .orElseThrow(() -> new BusinessException(AiErrorCode.PRODUCT_NOT_FOUND));
            // getSoftDeleteAudit()이 null이면 deleted_at/deleted_by 컬럼이 모두 null인
            // 활성 상품 (JPA @Embedded 특성상 모든 컬럼이 null이면 객체 자체가 null로 반환됨)
            if (product.getSoftDeleteAudit() != null && product.getSoftDeleteAudit().isDeleted()) {
                throw new BusinessException(AiErrorCode.PRODUCT_NOT_FOUND);
            }
            productName = product.getName();
        }

        // 9. 최종 프롬프트 조립
        String finalPrompt = aiPromptPolicy.buildFinalPrompt(productName, inputPrompt, tone, keywords);

        String toneSnapshot = tone.name();
        String keywordsSnapshot = (keywords != null && !keywords.isEmpty())
                ? String.join(",", keywords)
                : null;

        // 10. Gemini API 호출 — 실패 시 실패 로그 저장 후 예외 rethrow
        String rawResponse;
        try {
            rawResponse = geminiClient.generate(finalPrompt);
        } catch (BusinessException e) {
            aiLogRepository.save(AiLogEntity.failureProductDescription(
                    command.storeId(),
                    sourceLog.getProductId(),
                    command.requestedBy(),
                    inputPrompt,
                    finalPrompt,
                    e.getErrorCode().getErrorCode(),
                    e.getMessage(),
                    null,
                    null,
                    command.sourceAiLogId(),
                    toneSnapshot,
                    keywordsSnapshot
            ));
            throw e;
        }

        // 11. 응답 후처리 (50자 trim)
        String responseText = aiPromptPolicy.trimResponse(rawResponse);

        // 12. 성공 로그 저장 (sourceAiLogId로 원본 연결)
        AiLogEntity savedLog = aiLogRepository.save(AiLogEntity.successProductDescription(
                command.storeId(),
                sourceLog.getProductId(),
                command.requestedBy(),
                inputPrompt,
                finalPrompt,
                responseText,
                null,
                null,
                command.sourceAiLogId(),
                toneSnapshot,
                keywordsSnapshot
        ));

        return new RetryDescriptionResult(
                savedLog.getAiLogId(),
                command.sourceAiLogId(),
                responseText,
                finalPrompt
        );
    }
}
