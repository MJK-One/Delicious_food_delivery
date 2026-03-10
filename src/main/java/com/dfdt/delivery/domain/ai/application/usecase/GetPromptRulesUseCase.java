package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.domain.ai.application.dto.AiPromptRulesResult;

public interface GetPromptRulesUseCase {
    AiPromptRulesResult execute();
}