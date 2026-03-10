package com.dfdt.delivery.domain.ai.presentation.dto.response;

import com.dfdt.delivery.domain.ai.application.dto.AiPromptRulesResult;

import java.util.List;

public record AiPromptRulesResponse(
        String mandatorySuffix,
        int maxResponseLength,
        int maxInputPromptLength,
        int maxProductNameLength,
        int maxKeywordsCount,
        int maxKeywordItemLength,
        List<ToneRuleResponse> availableTones,
        String promptTemplate
) {
    public record ToneRuleResponse(String tone, String instruction) {}

    public static AiPromptRulesResponse from(AiPromptRulesResult result) {
        List<ToneRuleResponse> tones = result.availableTones()
                .stream()
                .map(r -> new ToneRuleResponse(r.tone(), r.instruction()))
                .toList();

        return new AiPromptRulesResponse(
                result.mandatorySuffix(),
                result.maxResponseLength(),
                result.maxInputPromptLength(),
                result.maxProductNameLength(),
                result.maxKeywordsCount(),
                result.maxKeywordItemLength(),
                tones,
                result.promptTemplate()
        );
    }
}
