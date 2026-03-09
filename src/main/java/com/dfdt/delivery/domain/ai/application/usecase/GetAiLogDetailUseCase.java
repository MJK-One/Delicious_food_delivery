package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.domain.ai.application.dto.AiLogDetailResult;
import com.dfdt.delivery.domain.ai.application.dto.GetAiLogDetailQuery;

public interface GetAiLogDetailUseCase {
    AiLogDetailResult execute(GetAiLogDetailQuery query);
}