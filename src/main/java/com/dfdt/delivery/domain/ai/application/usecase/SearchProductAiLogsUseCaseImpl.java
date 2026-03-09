package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.application.dto.AiLogSummaryResult;
import com.dfdt.delivery.domain.ai.application.dto.SearchProductAiLogsQuery;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
import com.dfdt.delivery.domain.ai.domain.repository.AiLogCustomRepository;
import com.dfdt.delivery.domain.product.domain.repository.ProductRepository;
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
public class SearchProductAiLogsUseCaseImpl implements SearchProductAiLogsUseCase {

    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final AiLogCustomRepository aiLogCustomRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AiLogSummaryResult> execute(SearchProductAiLogsQuery query) {

        // 1. OWNER: 본인 가게인지 소유권 확인 (MASTER는 스킵)
        if (query.requestedByRole() == UserRole.OWNER) {
            Store store = storeRepository.findByStoreIdAndNotDeleted(query.storeId())
                    .orElseThrow(() -> new BusinessException(AiErrorCode.STORE_NOT_FOUND));
            if (!store.getUser().getUsername().equals(query.requestedBy())) {
                throw new BusinessException(AiErrorCode.STORE_ACCESS_DENIED);
            }
        }

        // 2. 상품이 해당 가게에 속하는지 검증
        productRepository.findByProductIdAndStoreId(query.productId(), query.storeId())
                .orElseThrow(() -> new BusinessException(AiErrorCode.PRODUCT_NOT_FOUND));

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
