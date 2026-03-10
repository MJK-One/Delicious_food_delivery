package com.dfdt.delivery.domain.ai.infrastructure.adapter;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.domain.entity.AiLogEntity;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
import com.dfdt.delivery.domain.ai.domain.repository.AiLogRepository;
import com.dfdt.delivery.domain.product.domain.port.AiDescriptionPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Product 도메인의 AiDescriptionPort를 AI 도메인 인프라에서 구현합니다.
 * Product 도메인이 AiLog 엔티티/레포지토리를 직접 참조하지 않도록 중간 역할을 합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiDescriptionPortAdapter implements AiDescriptionPort {

    private final AiLogRepository aiLogRepository;

    @Override
    public String validateAndLink(UUID aiLogId, UUID storeId, UUID productId,
                                  String previousDescription, String username) {
        AiLogEntity aiLog = aiLogRepository.findById(aiLogId)
                .orElseThrow(() -> {
                    log.warn("[AiDescriptionPortAdapter] AI 로그 없음 - aiLogId={}", aiLogId);
                    return new BusinessException(AiErrorCode.AI_LOG_NOT_FOUND);
                });

        if (!aiLog.getStoreId().equals(storeId)) {
            log.warn("[AiDescriptionPortAdapter] storeId 불일치 - aiLogId={}, expected={}, actual={}",
                    aiLogId, storeId, aiLog.getStoreId());
            throw new BusinessException(AiErrorCode.STORE_NOT_FOUND);
        }
        if (Boolean.TRUE.equals(aiLog.getIsApplied())) {
            log.warn("[AiDescriptionPortAdapter] 이미 적용된 AI 로그 - aiLogId={}", aiLogId);
            throw new BusinessException(AiErrorCode.ALREADY_APPLIED);
        }
        if (aiLog.getProductId() != null) {
            log.warn("[AiDescriptionPortAdapter] 이미 상품 연결된 AI 로그 - aiLogId={}, productId={}",
                    aiLogId, aiLog.getProductId());
            throw new BusinessException(AiErrorCode.AI_LOG_PRODUCT_ALREADY_SET);
        }
        if (!Boolean.TRUE.equals(aiLog.getIsSuccess())) {
            log.warn("[AiDescriptionPortAdapter] 실패한 AI 로그는 적용 불가 - aiLogId={}", aiLogId);
            throw new BusinessException(AiErrorCode.AI_LOG_NOT_APPLICABLE);
        }

        aiLog.linkToProduct(productId, previousDescription, username);
        log.info("[AiDescriptionPortAdapter] AI 로그-상품 연결 완료 - aiLogId={}, productId={}", aiLogId, productId);
        return aiLog.getResponseText();
    }
}
