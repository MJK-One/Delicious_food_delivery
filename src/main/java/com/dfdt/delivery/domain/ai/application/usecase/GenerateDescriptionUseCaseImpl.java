package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.application.dto.GenerateDescriptionCommand;
import com.dfdt.delivery.domain.ai.application.dto.GenerateDescriptionResult;
import com.dfdt.delivery.domain.ai.domain.client.GeminiClient;
import com.dfdt.delivery.domain.ai.domain.entity.AiLogEntity;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
import com.dfdt.delivery.domain.ai.domain.policy.AiPromptPolicy;
import com.dfdt.delivery.domain.ai.domain.port.ProductForAiPort;
import com.dfdt.delivery.domain.ai.domain.port.ProductInfo;
import com.dfdt.delivery.domain.ai.domain.repository.AiLogRepository;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.repository.StoreRepository;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenerateDescriptionUseCaseImpl implements GenerateDescriptionUseCase {

    private final StoreRepository storeRepository;
    private final ProductForAiPort productForAiPort;
    private final AiLogRepository aiLogRepository;
    private final GeminiClient geminiClient;
    private final AiPromptPolicy aiPromptPolicy;

    @Override
    @Transactional
    public GenerateDescriptionResult execute(GenerateDescriptionCommand command) {

        // 1. Store 조회 (soft delete 포함)
        Store store = storeRepository.findByStoreIdAndNotDeleted(command.storeId())
                .orElseThrow(() -> new BusinessException(AiErrorCode.STORE_NOT_FOUND));

        // 2. OWNER 권한: 본인 가게인지 소유권 확인
        if (command.requestedByRole() == UserRole.OWNER) {
            if (!store.getUser().getUsername().equals(command.requestedBy())) {
                throw new BusinessException(AiErrorCode.STORE_ACCESS_DENIED);
            }
        }

        // 3. productId가 있으면 Product 조회 및 유효성 확인
        String resolvedProductName = command.productName();
        if (command.productId() != null) {
            ProductInfo productInfo = productForAiPort.findActive(command.productId(), command.storeId())
                    .orElseThrow(() -> new BusinessException(AiErrorCode.PRODUCT_NOT_FOUND));
            resolvedProductName = productInfo.name();
        }

        // 4. 최종 프롬프트 조립
        String finalPrompt = aiPromptPolicy.buildFinalPrompt(
                resolvedProductName,
                command.inputPrompt(),
                command.tone(),
                command.keywords()
        );

        // keywordsSnapshot: 키워드 리스트를 쉼표 구분 문자열로 변환
        String keywordsSnapshot = (command.keywords() != null && !command.keywords().isEmpty())
                ? String.join(",", command.keywords())
                : null;
        String toneSnapshot = command.tone().name();

        // 5. Gemini API 호출 — 실패 시 실패 로그 저장 후 예외 rethrow
        String rawResponse;
        String modelName = geminiClient.getModelName();
        long startMs = System.currentTimeMillis();
        try {
            rawResponse = geminiClient.generate(finalPrompt);
        } catch (BusinessException e) {
            int responseTimeMs = (int) (System.currentTimeMillis() - startMs);
            aiLogRepository.save(AiLogEntity.failureProductDescription(
                    command.storeId(),
                    command.productId(),
                    command.requestedBy(),
                    command.inputPrompt(),
                    finalPrompt,
                    e.getErrorCode().getErrorCode(),
                    e.getMessage(),
                    modelName,
                    responseTimeMs,
                    null,
                    toneSnapshot,
                    keywordsSnapshot
            ));
            throw e;
        }
        int responseTimeMs = (int) (System.currentTimeMillis() - startMs);

        // 6. 응답 후처리 (50자 trim)
        String responseText = aiPromptPolicy.trimResponse(rawResponse);

        // 7. 성공 로그 저장
        AiLogEntity savedLog = aiLogRepository.save(AiLogEntity.successProductDescription(
                command.storeId(),
                command.productId(),
                command.requestedBy(),
                command.inputPrompt(),
                finalPrompt,
                responseText,
                modelName,
                responseTimeMs,
                null,
                toneSnapshot,
                keywordsSnapshot
        ));

        return new GenerateDescriptionResult(savedLog.getAiLogId(), responseText, finalPrompt);
    }
}
