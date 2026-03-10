package com.dfdt.delivery.domain.ai.infrastructure.adapter;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.domain.entity.AiLogEntity;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
import com.dfdt.delivery.domain.ai.domain.repository.AiLogRepository;
import com.dfdt.delivery.domain.product.domain.port.AiDescriptionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Product 도메인의 AiDescriptionPort를 AI 도메인 인프라에서 구현합니다.
 * Product 도메인이 AiLog 엔티티/레포지토리를 직접 참조하지 않도록 중간 역할을 합니다.
 */
@Component
@RequiredArgsConstructor
public class AiDescriptionPortAdapter implements AiDescriptionPort {

    private final AiLogRepository aiLogRepository;

    @Override
    public String validateAndLink(UUID aiLogId, UUID storeId, UUID productId,
                                  String previousDescription, String username) {
        AiLogEntity aiLog = aiLogRepository.findById(aiLogId)
                .orElseThrow(() -> new BusinessException(AiErrorCode.AI_LOG_NOT_FOUND));

        if (!aiLog.getStoreId().equals(storeId)) {
            throw new BusinessException(AiErrorCode.STORE_NOT_FOUND);
        }
        if (Boolean.TRUE.equals(aiLog.getIsApplied())) {
            throw new BusinessException(AiErrorCode.ALREADY_APPLIED);
        }
        if (aiLog.getProductId() != null) {
            throw new BusinessException(AiErrorCode.AI_LOG_PRODUCT_ALREADY_SET);
        }
        if (!Boolean.TRUE.equals(aiLog.getIsSuccess())) {
            throw new BusinessException(AiErrorCode.AI_LOG_NOT_APPLICABLE);
        }

        aiLog.linkToProduct(productId, previousDescription, username);
        return aiLog.getResponseText();
    }
}
