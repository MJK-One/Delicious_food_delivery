package com.dfdt.delivery.domain.ai.domain.policy;

import com.dfdt.delivery.domain.ai.domain.entity.enums.Tone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AiPromptPolicy 테스트")
class AiPromptPolicyTest {

    private AiPromptPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new AiPromptPolicy();
    }

    // ──────────────────────────────────────────────────
    // buildFinalPrompt 테스트
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("finalPrompt 생성")
    class BuildFinalPromptTest {

        @Test
        @DisplayName("강제 문구가 항상 포함된다")
        void shouldAlwaysIncludeMandatorySuffix() {
            // given
            String inputPrompt = "치킨 신메뉴 설명 작성해줘";

            // when
            String result = policy.buildFinalPrompt(null, inputPrompt, Tone.INFORMATIVE, null);

            // then
            // 강제 문구가 포함되어야 한다
            assertThat(result).contains(AiPromptPolicy.MANDATORY_SUFFIX);
        }

        @Test
        @DisplayName("FRIENDLY 톤 지시문이 포함된다")
        void shouldIncludeFriendlyToneInstruction() {
            // given & when
            String result = policy.buildFinalPrompt(null, "설명 작성해줘", Tone.FRIENDLY, null);

            // then
            assertThat(result).contains("친근");
        }

        @Test
        @DisplayName("SALESY 톤 지시문이 포함된다")
        void shouldIncludeSalesyToneInstruction() {
            // given & when
            String result = policy.buildFinalPrompt(null, "설명 작성해줘", Tone.SALESY, null);

            // then
            assertThat(result).contains("세일즈");
        }

        @Test
        @DisplayName("INFORMATIVE 톤 지시문이 포함된다")
        void shouldIncludeInformativeToneInstruction() {
            // given & when
            String result = policy.buildFinalPrompt(null, "설명 작성해줘", Tone.INFORMATIVE, null);

            // then
            assertThat(result).contains("정보");
        }

        @Test
        @DisplayName("productName이 있으면 finalPrompt에 포함된다")
        void shouldIncludeProductNameWhenProvided() {
            // given
            String productName = "황금 바삭치킨";

            // when
            String result = policy.buildFinalPrompt(productName, "설명 작성해줘", Tone.FRIENDLY, null);

            // then
            assertThat(result).contains(productName);
        }

        @Test
        @DisplayName("productName이 null이면 포함되지 않는다")
        void shouldSkipProductNameWhenNull() {
            // given & when
            String result = policy.buildFinalPrompt(null, "설명 작성해줘", Tone.FRIENDLY, null);

            // then
            // "상품명:" 이라는 라벨이 없어야 한다
            assertThat(result).doesNotContain("상품명:");
        }

        @Test
        @DisplayName("keywords가 있으면 finalPrompt에 포함된다")
        void shouldIncludeKeywordsWhenProvided() {
            // given
            List<String> keywords = List.of("바삭", "국내산", "신선한");

            // when
            String result = policy.buildFinalPrompt(null, "설명 작성해줘", Tone.FRIENDLY, keywords);

            // then
            assertThat(result).contains("바삭");
            assertThat(result).contains("국내산");
            assertThat(result).contains("신선한");
        }

        @Test
        @DisplayName("keywords가 빈 리스트이면 키워드 영역이 포함되지 않는다")
        void shouldSkipKeywordsWhenEmpty() {
            // given & when
            String result = policy.buildFinalPrompt(null, "설명 작성해줘", Tone.FRIENDLY, List.of());

            // then
            assertThat(result).doesNotContain("키워드:");
        }

        @Test
        @DisplayName("inputPrompt가 finalPrompt에 포함된다")
        void shouldIncludeInputPrompt() {
            // given
            String inputPrompt = "겉은 바삭하고 속은 촉촉한 특징을 강조해줘";

            // when
            String result = policy.buildFinalPrompt(null, inputPrompt, Tone.FRIENDLY, null);

            // then
            assertThat(result).contains(inputPrompt);
        }

        @Test
        @DisplayName("강제 문구는 finalPrompt의 가장 마지막에 위치한다")
        void mandatorySuffixShouldBeAtEnd() {
            // given & when
            String result = policy.buildFinalPrompt("치킨", "설명 작성해줘", Tone.FRIENDLY, List.of("바삭"));

            // then
            assertThat(result).endsWith(AiPromptPolicy.MANDATORY_SUFFIX);
        }
    }

    // ──────────────────────────────────────────────────
    // trimResponse 테스트
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("AI 응답 후처리 (trimResponse)")
    class TrimResponseTest {

        @Test
        @DisplayName("50자 이하이면 그대로 반환한다")
        void shouldReturnAsIsWhenUnder50() {
            // given
            String response = "맛있는 치킨입니다!"; // 9자

            // when
            String result = policy.trimResponse(response);

            // then
            assertThat(result).isEqualTo(response);
        }

        @Test
        @DisplayName("50자 초과이면 50자로 잘린다")
        void shouldTrimWhenOver50() {
            // given
            String response = "a".repeat(60); // 60자

            // when
            String result = policy.trimResponse(response);

            // then
            assertThat(result).hasSize(AiPromptPolicy.MAX_RESPONSE_LENGTH);
        }

        @Test
        @DisplayName("null 입력이면 null을 반환한다")
        void shouldReturnNullWhenInputIsNull() {
            // when
            String result = policy.trimResponse(null);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("공백만 있는 응답이면 null을 반환한다")
        void shouldReturnNullWhenInputIsBlank() {
            // when
            String result = policy.trimResponse("   ");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("정확히 50자이면 그대로 반환한다")
        void shouldReturnAsIsWhenExactly50() {
            // given
            String response = "a".repeat(50); // 정확히 50자

            // when
            String result = policy.trimResponse(response);

            // then
            assertThat(result).hasSize(50);
            assertThat(result).isEqualTo(response);
        }
    }
}
