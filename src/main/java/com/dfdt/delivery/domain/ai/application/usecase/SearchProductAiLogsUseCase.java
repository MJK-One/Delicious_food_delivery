package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.domain.ai.application.dto.AiLogSummaryResult;
import com.dfdt.delivery.domain.ai.application.dto.SearchProductAiLogsQuery;
import org.springframework.data.domain.Page;

public interface SearchProductAiLogsUseCase {
    Page<AiLogSummaryResult> execute(SearchProductAiLogsQuery query);
}
