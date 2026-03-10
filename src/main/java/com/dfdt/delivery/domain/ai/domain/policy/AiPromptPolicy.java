package com.dfdt.delivery.domain.ai.domain.policy;

import com.dfdt.delivery.domain.ai.domain.entity.enums.Tone;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class AiPromptPolicy {

    public static final String MANDATORY_SUFFIX = " 답변을 최대한 간결하게 50자 이하로 작성해줘.";
    public static final int MAX_RESPONSE_LENGTH = 50;

    /**
     * 톤(Tone)과 해당 지시문을 쌍으로 나타내는 도메인 레코드
     */
    public record ToneRule(String tone, String instruction) {}

    /**
     * 사용 가능한 모든 톤 규칙 목록을 반환합니다.
     */
    public List<ToneRule> availableToneRules() {
        return Arrays.stream(Tone.values())
                .map(t -> new ToneRule(t.name(), toneInstruction(t)))
                .toList();
    }

    /**
     * 최종 프롬프트를 조립합니다.
     * 구성: 톤 지시문 → 상품명(선택) → 키워드(선택) → inputPrompt → 강제 문구
     */
    public String buildFinalPrompt(
            String productName,
            String inputPrompt,
            Tone tone,
            List<String> keywords
    ) {
        StringBuilder sb = new StringBuilder();

        // 1. 톤 지시문
        sb.append(toneInstruction(tone)).append("\n");

        // 2. 상품명 (null 또는 blank 제외)
        if (productName != null && !productName.isBlank()) {
            sb.append("상품명: ").append(productName).append("\n");
        }

        // 3. 키워드 (null 또는 빈 리스트 제외)
        if (keywords != null && !keywords.isEmpty()) {
            sb.append("키워드: ").append(String.join(", ", keywords)).append("\n");
        }

        // 4. 사용자 입력 프롬프트
        sb.append(inputPrompt);

        // 5. 강제 문구 (항상 마지막)
        sb.append(MANDATORY_SUFFIX);

        return sb.toString();
    }

    /**
     * AI 응답을 후처리합니다.
     * - null 또는 blank 입력 → null 반환
     * - MAX_RESPONSE_LENGTH 초과 → 잘라서 반환
     */
    public String trimResponse(String response) {
        if (response == null || response.isBlank()) {
            return null;
        }
        if (response.length() > MAX_RESPONSE_LENGTH) {
            return response.substring(0, MAX_RESPONSE_LENGTH);
        }
        return response;
    }

    private String toneInstruction(Tone tone) {
        return switch (tone) {
            case FRIENDLY -> "친근하고 따뜻한 톤으로 작성해줘.";
            case SALESY -> "세일즈 톤으로 구매 욕구를 자극하게 작성해줘.";
            case INFORMATIVE -> "정보 전달 위주의 간결한 톤으로 작성해줘.";
        };
    }
}
