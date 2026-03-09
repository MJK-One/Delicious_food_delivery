package com.dfdt.delivery.domain.ai.infrastructure.client;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
import com.dfdt.delivery.domain.ai.infrastructure.config.GeminiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("GeminiWebClient 테스트")
class GeminiWebClientTest {

    private GeminiProperties properties;

    @BeforeEach
    void setUp() {
        properties = new GeminiProperties(
                "test-api-key",
                "gemini-2.0-flash",
                "https://generativelanguage.googleapis.com"
        );
    }

    // ──────────────────────────────────────────────────
    // parseText() 단위 테스트 — 파싱/검증 핵심 로직
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("응답 JSON 파싱 (parseText)")
    class ParseTextTests {

        private GeminiWebClient sut;

        @BeforeEach
        void setUp() {
            // parseText()는 RestClient를 사용하지 않으므로 mock 전달
            sut = new GeminiWebClient(mock(RestClient.class), properties);
        }

        @Test
        @DisplayName("정상 응답 JSON에서 텍스트를 추출한다")
        void shouldExtractTextFromValidJson() {
            // given
            String responseJson = """
                    {
                      "candidates": [
                        {
                          "content": {
                            "parts": [{"text": "맛있는 황금 치킨입니다!"}]
                          }
                        }
                      ]
                    }
                    """;

            // when
            String result = sut.parseText(responseJson);

            // then
            assertThat(result).isEqualTo("맛있는 황금 치킨입니다!");
        }

        @Test
        @DisplayName("candidates 배열이 비어 있으면 EXTERNAL_AI_EMPTY_RESPONSE")
        void shouldThrowWhenCandidatesEmpty() {
            assertThatThrownBy(() -> sut.parseText("{\"candidates\": []}"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.EXTERNAL_AI_EMPTY_RESPONSE));
        }

        @Test
        @DisplayName("응답 자체가 null이면 EXTERNAL_AI_EMPTY_RESPONSE")
        void shouldThrowWhenResponseIsNull() {
            assertThatThrownBy(() -> sut.parseText(null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.EXTERNAL_AI_EMPTY_RESPONSE));
        }

        @Test
        @DisplayName("응답이 빈 문자열이면 EXTERNAL_AI_EMPTY_RESPONSE")
        void shouldThrowWhenResponseIsBlank() {
            assertThatThrownBy(() -> sut.parseText("   "))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.EXTERNAL_AI_EMPTY_RESPONSE));
        }
    }

    // ──────────────────────────────────────────────────
    // generate() 테스트 — executeHttpCall을 오버라이드하여 HTTP 없이 테스트
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("HTTP 호출 (generate)")
    class GenerateTests {

        /**
         * executeHttpCall()을 오버라이드한 테스트용 서브클래스.
         * RestClient 체인을 mock하지 않고도 generate()의 흐름을 검증할 수 있습니다.
         */
        private GeminiWebClient stubWith(String returnValue) {
            return new GeminiWebClient(mock(RestClient.class), properties) {
                @Override
                protected String executeHttpCall(String url, Object body) {
                    return returnValue;
                }
            };
        }

        private GeminiWebClient throwingWith(RuntimeException ex) {
            return new GeminiWebClient(mock(RestClient.class), properties) {
                @Override
                protected String executeHttpCall(String url, Object body) {
                    throw ex;
                }
            };
        }

        @Test
        @DisplayName("HTTP 호출 성공 시 parseText를 거쳐 텍스트를 반환한다")
        void shouldReturnParsedTextOnSuccess() {
            // given
            String responseJson = """
                    {
                      "candidates": [
                        {"content": {"parts": [{"text": "바삭한 치킨!"}]}}
                      ]
                    }
                    """;
            GeminiWebClient sut = stubWith(responseJson);

            // when
            String result = sut.generate("치킨 설명");

            // then
            assertThat(result).isEqualTo("바삭한 치킨!");
        }

        @Test
        @DisplayName("HTTP 호출에서 RuntimeException 발생 시 EXTERNAL_AI_CALL_FAILED")
        void shouldWrapRuntimeExceptionAsCallFailed() {
            // given
            GeminiWebClient sut = throwingWith(new RuntimeException("Connection refused"));

            // when & then
            assertThatThrownBy(() -> sut.generate("치킨 설명"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.EXTERNAL_AI_CALL_FAILED));
        }

        @Test
        @DisplayName("HTTP 호출에서 BusinessException 발생 시 그대로 rethrow된다")
        void shouldRethrowBusinessException() {
            // given - BusinessException은 포장 없이 그대로 던져진다
            BusinessException original = new BusinessException(AiErrorCode.EXTERNAL_AI_TIMEOUT);
            GeminiWebClient sut = throwingWith(original);

            // when & then
            assertThatThrownBy(() -> sut.generate("치킨 설명"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.EXTERNAL_AI_TIMEOUT));
        }
    }
}
