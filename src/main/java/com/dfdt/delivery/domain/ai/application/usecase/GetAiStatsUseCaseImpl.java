package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.application.dto.AiStatsQuery;
import com.dfdt.delivery.domain.ai.application.dto.AiStatsResult;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
import com.dfdt.delivery.domain.ai.domain.repository.AiLogCustomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class GetAiStatsUseCaseImpl implements GetAiStatsUseCase {

    private final AiLogCustomRepository aiLogCustomRepository;

    @Override
    @Transactional(readOnly = true)
    public AiStatsResult execute(AiStatsQuery query) {

        // 1. 날짜 범위 유효성 검증
        if (query.fromDateTime() != null && query.toDateTime() != null) {
            if (query.fromDateTime().isAfter(query.toDateTime())) {
                throw new BusinessException(AiErrorCode.INVALID_DATE_RANGE);
            }
            long days = ChronoUnit.DAYS.between(query.fromDateTime(), query.toDateTime());
            if (days > 90) {
                throw new BusinessException(AiErrorCode.DATE_RANGE_EXCEEDED);
            }
        }

        // 2. QueryDSL 집계 호출
        return aiLogCustomRepository.getAiStats(
                query.storeId(),
                query.fromDateTime(),
                query.toDateTime(),
                query.requestType()
        );
    }
}
