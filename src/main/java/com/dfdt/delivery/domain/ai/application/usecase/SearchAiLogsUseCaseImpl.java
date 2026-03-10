package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.application.dto.AiLogSummaryResult;
import com.dfdt.delivery.domain.ai.application.dto.SearchAiLogsQuery;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
import com.dfdt.delivery.domain.ai.domain.repository.AiLogCustomRepository;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.repository.StoreRepository;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchAiLogsUseCaseImpl implements SearchAiLogsUseCase {

    private final StoreRepository storeRepository;
    private final AiLogCustomRepository aiLogCustomRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AiLogSummaryResult> execute(SearchAiLogsQuery query) {

        // OWNER: 본인 가게인지 소유권 확인 (MASTER는 스킵)
        if (query.requestedByRole() == UserRole.OWNER) {
            Store store = storeRepository.findByStoreIdAndNotDeleted(query.storeId())
                    .orElseThrow(() -> new BusinessException(AiErrorCode.STORE_NOT_FOUND));
            if (!store.getUser().getUsername().equals(query.requestedBy())) {
                throw new BusinessException(AiErrorCode.STORE_ACCESS_DENIED);
            }
        }

        Sort.Direction direction = query.isAsc() ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(query.page(), query.size(), Sort.by(direction, query.sortBy()));

        return aiLogCustomRepository.searchAiLogs(
                query.storeId(),
                query.productId(),
                query.isApplied(),
                query.isSuccess(),
                pageable
        );
    }
}