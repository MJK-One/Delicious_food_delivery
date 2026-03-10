package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.application.dto.GenerateImageCommand;
import com.dfdt.delivery.domain.ai.application.dto.GenerateImageResult;
import com.dfdt.delivery.domain.ai.domain.client.GeneratedImageData;
import com.dfdt.delivery.domain.ai.domain.client.ImageGenerationClient;
import com.dfdt.delivery.domain.ai.domain.entity.AiLogEntity;
import com.dfdt.delivery.domain.ai.domain.entity.enums.AspectRatio;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
import com.dfdt.delivery.domain.ai.domain.repository.AiLogRepository;
import com.dfdt.delivery.domain.product.domain.entity.Product;
import com.dfdt.delivery.domain.product.domain.repository.ProductRepository;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.repository.StoreRepository;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenerateImageUseCaseImpl implements GenerateImageUseCase {

    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final AiLogRepository aiLogRepository;
    private final ImageGenerationClient imageGenerationClient;

    @Override
    @Transactional
    public GenerateImageResult execute(GenerateImageCommand command) {

        // 1. Store 조회
        Store store = storeRepository.findByStoreIdAndNotDeleted(command.storeId())
                .orElseThrow(() -> new BusinessException(AiErrorCode.STORE_NOT_FOUND));

        // 2. OWNER 권한: 본인 가게인지 소유권 확인
        if (command.requestedByRole() == UserRole.OWNER) {
            if (!store.getUser().getUsername().equals(command.requestedBy())) {
                throw new BusinessException(AiErrorCode.STORE_ACCESS_DENIED);
            }
        }

        // 3. productId / productName 둘 다 없으면 오류
        if (command.productId() == null && (command.productName() == null || command.productName().isBlank())) {
            throw new BusinessException(AiErrorCode.PRODUCT_IDENTIFIER_REQUIRED);
        }

        // 4. productId가 있으면 Product 조회 및 상품명 확인
        String resolvedProductName = command.productName();
        if (command.productId() != null) {
            Product product = productRepository.findByProductIdAndStoreId(command.productId(), command.storeId())
                    .orElseThrow(() -> new BusinessException(AiErrorCode.PRODUCT_NOT_FOUND));
            // getSoftDeleteAudit()이 null이면 활성 상품 (JPA @Embedded 특성상 모든 컬럼 null → 객체 null)
            if (product.getSoftDeleteAudit() != null && product.getSoftDeleteAudit().isDeleted()) {
                throw new BusinessException(AiErrorCode.PRODUCT_NOT_FOUND);
            }
            resolvedProductName = product.getName();
        }

        // 5. includeText = true인데 text가 없으면 오류
        if (command.includeText() && (command.text() == null || command.text().isBlank())) {
            throw new BusinessException(AiErrorCode.INCLUDE_TEXT_REQUIRED);
        }

        // 6. aspectRatio 기본값 처리 (null → SQUARE)
        AspectRatio aspectRatio = command.aspectRatio() != null ? command.aspectRatio() : AspectRatio.SQUARE;

        // 7. 이미지 생성 프롬프트 조립 (aspectRatio는 API 파라미터로 전달하므로 프롬프트에서 제외)
        String finalPrompt = buildImagePrompt(resolvedProductName, command.prompt(),
                command.style(), command.includeText(), command.text());

        // 8. 이미지 생성 API 호출 — 실패 시 실패 로그 저장 후 예외 rethrow
        GeneratedImageData imageData;
        String modelName = imageGenerationClient.getModelName();
        long startMs = System.currentTimeMillis();
        try {
            imageData = imageGenerationClient.generate(finalPrompt, aspectRatio.getRatio());
        } catch (BusinessException e) {
            int responseTimeMs = (int) (System.currentTimeMillis() - startMs);
            aiLogRepository.save(AiLogEntity.failureFoodImageGeneration(
                    command.storeId(),
                    command.productId(),
                    command.requestedBy(),
                    command.prompt(),
                    finalPrompt,
                    e.getErrorCode().getErrorCode(),
                    e.getMessage(),
                    modelName,
                    responseTimeMs,
                    null,
                    null,
                    null
            ));
            throw e;
        }
        int responseTimeMs = (int) (System.currentTimeMillis() - startMs);

        // 9. 성공 로그 저장 (imageData.base64Data를 responseText에 저장)
        AiLogEntity savedLog = aiLogRepository.save(AiLogEntity.successFoodImageGeneration(
                command.storeId(),
                command.productId(),
                command.requestedBy(),
                command.prompt(),
                finalPrompt,
                imageData.base64Data(),
                modelName,
                responseTimeMs,
                null,
                null,
                null
        ));

        return new GenerateImageResult(savedLog.getAiLogId(), imageData.base64Data(), imageData.mimeType());
    }

    /**
     * 이미지 생성 프롬프트를 조립합니다.
     * 구성: [음식 사진 생성] + 상품명 + 사용자 프롬프트 + (선택) 스타일 + (선택) 텍스트 오버레이 지시
     * aspectRatio는 Imagen API의 네이티브 파라미터로 직접 전달되므로 프롬프트에서 제외합니다.
     */
    private String buildImagePrompt(String productName, String userPrompt,
                                    String style, boolean includeText, String text) {
        StringBuilder sb = new StringBuilder();
        sb.append("음식 사진을 생성해줘.");

        if (productName != null && !productName.isBlank()) {
            sb.append(" 상품명: ").append(productName).append(".");
        }

        sb.append(" ").append(userPrompt).append(".");

        if (style != null && !style.isBlank()) {
            sb.append(" 스타일: ").append(style).append(".");
        }

        if (includeText && text != null && !text.isBlank()) {
            sb.append(" 이미지에 '").append(text).append("' 텍스트를 눈에 잘 띄게 포함해줘.");
        }

        return sb.toString();
    }
}
