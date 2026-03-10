package com.dfdt.delivery.domain.ai.application.dto;

import java.util.List;

public record AiPromptRulesResult(
        String mandatorySuffix,
        int maxResponseLength,
        int maxInputPromptLength,
        int maxProductNameLength,
        int maxKeywordsCount,
        int maxKeywordItemLength,
        List<ToneRuleResult> availableTones,
        String promptTemplate
) {
    public record ToneRuleResult(String tone, String instruction) {}
}