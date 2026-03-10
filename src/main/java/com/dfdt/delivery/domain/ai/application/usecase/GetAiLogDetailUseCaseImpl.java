package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.application.dto.AiLogDetailResult;
import com.dfdt.delivery.domain.ai.application.dto.GetAiLogDetailQuery;
import com.dfdt.delivery.domain.ai.domain.entity.AiLogEntity;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
import com.dfdt.delivery.domain.ai.domain.repository.AiLogRepository;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.repository.StoreRepository;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAiLogDetailUseCaseImpl implements GetAiLogDetailUseCase {

    private final AiLogRepository aiLogRepository;
    private final StoreRepository storeRepository;

    @Override
    @Transactional(readOnly = true)
    public AiLogDetailResult execute(GetAiLogDetailQuery query) {

        // 1. AI 로그 조회
        AiLogEntity aiLog = aiLogRepository.findById(query.aiLogId())
                .orElseThrow(() -> new BusinessException(AiErrorCode.AI_LOG_NOT_FOUND));

        // 2. storeId 일치 검증 (URL 위변조 방지)
        if (!aiLog.getStoreId().equals(query.storeId())) {
            throw new BusinessException(AiErrorCode.STORE_NOT_FOUND);
        }

        // 3. OWNER 권한: 본인 가게인지 소유권 확인 (MASTER는 스킵)
        if (query.requestedByRole() == UserRole.OWNER) {
            Store store = storeRepository.findByStoreIdAndNotDeleted(query.storeId())
                    .orElseThrow(() -> new BusinessException(AiErrorCode.STORE_NOT_FOUND));
            if (!store.getUser().getUsername().equals(query.requestedBy())) {
                throw new BusinessException(AiErrorCode.STORE_ACCESS_DENIED);
            }
        }

        return AiLogDetailResult.from(aiLog);
    }
}