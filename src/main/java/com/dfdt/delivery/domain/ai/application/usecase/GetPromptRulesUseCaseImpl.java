package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.domain.ai.application.dto.AiPromptRulesResult;
import com.dfdt.delivery.domain.ai.domain.policy.AiPromptPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPromptRulesUseCaseImpl implements GetPromptRulesUseCase {

    private final AiPromptPolicy aiPromptPolicy;

    // GenerateDescriptionRequest @Size 제약 조건과 동일한 값을 여기서 상수로 통합 관리
    private static final int MAX_INPUT_PROMPT_LENGTH = 300;
    private static final int MAX_PRODUCT_NAME_LENGTH = 120;
    private static final int MAX_KEYWORDS_COUNT = 10;
    private static final int MAX_KEYWORD_ITEM_LENGTH = 30;

    private static final String PROMPT_TEMPLATE =
            "[톤 지시문]\n" +
            "(상품명: {productName})\n" +
            "(키워드: {keyword1, keyword2, ...})\n" +
            "{inputPrompt}" + AiPromptPolicy.MANDATORY_SUFFIX;

    @Override
    public AiPromptRulesResult execute() {
        List<AiPromptRulesResult.ToneRuleResult> tones = aiPromptPolicy.availableToneRules()
                .stream()
                .map(r -> new AiPromptRulesResult.ToneRuleResult(r.tone(), r.instruction()))
                .toList();

        return new AiPromptRulesResult(
                AiPromptPolicy.MANDATORY_SUFFIX,
                AiPromptPolicy.MAX_RESPONSE_LENGTH,
                MAX_INPUT_PROMPT_LENGTH,
                MAX_PRODUCT_NAME_LENGTH,
                MAX_KEYWORDS_COUNT,
                MAX_KEYWORD_ITEM_LENGTH,
                tones,
                PROMPT_TEMPLATE
        );
    }
}