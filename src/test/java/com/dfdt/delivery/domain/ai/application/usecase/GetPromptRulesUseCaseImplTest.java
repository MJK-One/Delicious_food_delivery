package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.domain.ai.application.dto.AiPromptRulesResult;
import com.dfdt.delivery.domain.ai.domain.policy.AiPromptPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GetPromptRulesUseCaseImpl 테스트")
class GetPromptRulesUseCaseImplTest {

    // AiPromptPolicy는 상태가 없는 순수 도메인 컴포넌트 → 실제 인스턴스 사용
    private GetPromptRulesUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetPromptRulesUseCaseImpl(new AiPromptPolicy());
    }

    // ──────────────────────────────────────────────────
    // 정적 규칙 반환 검증
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("프롬프트 규칙 조회")
    class RulesTests {

        @Test
        @DisplayName("강제 문구와 최대 응답 길이를 올바르게 반환한다")
        void shouldReturnMandatorySuffixAndMaxLength() {
            // when
            AiPromptRulesResult result = useCase.execute();

            // then
            assertThat(result.mandatorySuffix()).isEqualTo(AiPromptPolicy.MANDATORY_SUFFIX);
            assertThat(result.maxResponseLength()).isEqualTo(AiPromptPolicy.MAX_RESPONSE_LENGTH);
        }

        @Test
        @DisplayName("입력 제약 조건을 올바르게 반환한다")
        void shouldReturnInputConstraints() {
            // when
            AiPromptRulesResult result = useCase.execute();

            // then
            assertThat(result.maxInputPromptLength()).isEqualTo(300);
            assertThat(result.maxProductNameLength()).isEqualTo(120);
            assertThat(result.maxKeywordsCount()).isEqualTo(10);
            assertThat(result.maxKeywordItemLength()).isEqualTo(30);
        }

        @Test
        @DisplayName("3개의 톤 규칙(FRIENDLY, SALESY, INFORMATIVE)을 반환한다")
        void shouldReturnThreeToneRules() {
            // when
            AiPromptRulesResult result = useCase.execute();

            // then
            assertThat(result.availableTones()).hasSize(3);
            assertThat(result.availableTones())
                    .extracting(AiPromptRulesResult.ToneRuleResult::tone)
                    .containsExactlyInAnyOrder("FRIENDLY", "SALESY", "INFORMATIVE");
        }

        @Test
        @DisplayName("각 톤에 대한 지시문이 비어있지 않다")
        void shouldReturnNonBlankInstructionForEachTone() {
            // when
            AiPromptRulesResult result = useCase.execute();

            // then
            result.availableTones().forEach(tone ->
                    assertThat(tone.instruction())
                            .as("톤 '%s'의 지시문이 비어있으면 안 됩니다.", tone.tone())
                            .isNotBlank()
            );
        }

        @Test
        @DisplayName("프롬프트 템플릿이 강제 문구를 포함한다")
        void shouldReturnTemplateContainingMandatorySuffix() {
            // when
            AiPromptRulesResult result = useCase.execute();

            // then
            assertThat(result.promptTemplate()).contains(AiPromptPolicy.MANDATORY_SUFFIX);
        }
    }
}
