package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.domain.ai.application.dto.AiLogSummaryResult;
import com.dfdt.delivery.domain.ai.application.dto.SearchAiLogsQuery;
import org.springframework.data.domain.Page;

public interface SearchAiLogsUseCase {
    Page<AiLogSummaryResult> execute(SearchAiLogsQuery query);
}