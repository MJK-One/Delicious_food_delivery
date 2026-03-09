package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.domain.ai.application.dto.AiHealthResult;
import com.dfdt.delivery.domain.ai.domain.client.GeminiClient;
import com.dfdt.delivery.domain.ai.infrastructure.config.GeminiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class CheckAiHealthUseCaseImpl implements CheckAiHealthUseCase {

    private final GeminiClient geminiClient;
    private final GeminiProperties geminiProperties;

    private static final String PING_PROMPT = "ping";

    @Override
    public AiHealthResult execute() {
        OffsetDateTime checkedAt = OffsetDateTime.now();
        long start = System.currentTimeMillis();
        try {
            geminiClient.generate(PING_PROMPT);
            int responseTimeMs = (int) (System.currentTimeMillis() - start);
            return new AiHealthResult("UP", geminiProperties.model(), responseTimeMs, null, checkedAt);
        } catch (Exception e) {
            int responseTimeMs = (int) (System.currentTimeMillis() - start);
            return new AiHealthResult("DOWN", geminiProperties.model(), responseTimeMs, e.getMessage(), checkedAt);
        }
    }
}
