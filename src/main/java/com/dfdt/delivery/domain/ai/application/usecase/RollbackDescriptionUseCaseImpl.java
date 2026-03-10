package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.application.dto.RollbackDescriptionCommand;
import com.dfdt.delivery.domain.ai.application.dto.RollbackDescriptionResult;
import com.dfdt.delivery.domain.ai.domain.entity.AiLogEntity;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
import com.dfdt.delivery.domain.ai.domain.port.ProductForAiPort;
import com.dfdt.delivery.domain.ai.domain.repository.AiLogRepository;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.repository.StoreRepository;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RollbackDescriptionUseCaseImpl implements RollbackDescriptionUseCase {

    private final AiLogRepository aiLogRepository;
    private final StoreRepository storeRepository;
    private final ProductForAiPort productForAiPort;

    @Override
    @Transactional
    public RollbackDescriptionResult execute(RollbackDescriptionCommand command) {

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

        // 4. 아직 적용되지 않은 로그인지 확인
        if (!Boolean.TRUE.equals(aiLog.getIsApplied())) {
            throw new BusinessException(AiErrorCode.NOT_YET_APPLIED);
        }

        // 5. 이미 롤백된 로그인지 확인
        if (aiLog.getRolledBackAt() != null) {
            throw new BusinessException(AiErrorCode.ALREADY_ROLLED_BACK);
        }

        // 6+7. Product 조회 및 설명 원복
        boolean restored = productForAiPort.restoreDescription(
                aiLog.getProductId(), command.storeId(),
                aiLog.getPreviousDescription(), command.requestedBy()
        );
        if (!restored) {
            throw new BusinessException(AiErrorCode.PRODUCT_NOT_FOUND);
        }

        // 8. AiLog에 롤백 기록
        aiLog.rollback(command.requestedBy());

        return new RollbackDescriptionResult(
                aiLog.getAiLogId(),
                aiLog.getProductId(),
                aiLog.getPreviousDescription(),
                aiLog.getRolledBackAt()
        );
    }
}
