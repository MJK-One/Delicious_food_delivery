package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.application.dto.ApplyDescriptionCommand;
import com.dfdt.delivery.domain.ai.application.dto.ApplyDescriptionResult;
import com.dfdt.delivery.domain.ai.domain.entity.AiLogEntity;
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
public class ApplyDescriptionUseCaseImpl implements ApplyDescriptionUseCase {

    private final AiLogRepository aiLogRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ApplyDescriptionResult execute(ApplyDescriptionCommand command) {

        // 1. AiLog 조회
        AiLogEntity aiLog = aiLogRepository.findById(command.aiLogId())
                .orElseThrow(() -> new BusinessException(AiErrorCode.AI_LOG_NOT_FOUND));

        // 2. storeId 일치 검증 (URL 위변조 방지)
        if (!aiLog.getStoreId().equals(command.storeId())) {
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

        // 4. 이미 적용된 로그인지 확인
        if (Boolean.TRUE.equals(aiLog.getIsApplied())) {
            throw new BusinessException(AiErrorCode.ALREADY_APPLIED);
        }

        // 5. productId가 없으면 미리보기 전용 로그 → 적용 불가
        if (aiLog.getProductId() == null) {
            throw new BusinessException(AiErrorCode.PRODUCT_ID_REQUIRED_FOR_APPLY);
        }

        // 6. Product 조회
        Product product = productRepository.findByProductIdAndStoreId(aiLog.getProductId(), command.storeId())
                .orElseThrow(() -> new BusinessException(AiErrorCode.PRODUCT_NOT_FOUND));

        // 7. 기존 설명 저장 후 AI 설명 적용
        String previousDescription = product.getDescription();
        product.applyAiDescription(aiLog.getResponseText(), command.requestedBy());

        // 8. AiLog에 적용 완료 기록
        aiLog.applyDescription(previousDescription, command.requestedBy());

        return new ApplyDescriptionResult(
                aiLog.getAiLogId(),
                aiLog.getProductId(),
                aiLog.getResponseText(),
                aiLog.getAppliedAt()
        );
    }
}
