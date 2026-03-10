package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.domain.ai.application.dto.AiStatsQuery;
import com.dfdt.delivery.domain.ai.application.dto.AiStatsResult;

public interface GetAiStatsUseCase {

    AiStatsResult execute(AiStatsQuery query);
}
