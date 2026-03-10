package com.dfdt.delivery.domain.ai.infrastructure.client;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.domain.client.GeneratedImageData;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
import com.dfdt.delivery.domain.ai.infrastructure.config.GeminiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("GeminiImageWebClient 테스트")
class GeminiImageWebClientTest {

    private GeminiProperties properties;

    @BeforeEach
    void setUp() {
        properties = new GeminiProperties(
                "test-api-key",
                "gemini-2.0-flash",
                "https://generativelanguage.googleapis.com",
                "imagen-4.0-generate-001"
        );
    }

    // ──────────────────────────────────────────────────
    // parseImage() 단위 테스트 — 파싱/검증 핵심 로직
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("응답 JSON 파싱 (parseImage)")
    class ParseImageTests {

        private GeminiImageWebClient sut;

        @BeforeEach
        void setUp() {
            sut = new GeminiImageWebClient(mock(RestClient.class), properties);
        }

        @Test
        @DisplayName("정상 응답 JSON에서 base64 이미지 데이터를 추출한다")
        void shouldExtractImageDataFromValidJson() {
            // given — Imagen API 응답 구조: predictions[0].bytesBase64Encoded + mimeType
            String responseJson = """
                    {
                      "predictions": [
                        {
                          "bytesBase64Encoded": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJ",
                          "mimeType": "image/jpeg"
                        }
                      ]
                    }
                    """;

            // when
            GeneratedImageData result = sut.parseImage(responseJson);

            // then
            assertThat(result.base64Data()).isEqualTo("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJ");
            assertThat(result.mimeType()).isEqualTo("image/jpeg");
        }

        @Test
        @DisplayName("mimeType이 없으면 기본값 image/jpeg를 사용한다")
        void shouldDefaultToImageJpegWhenMimeTypeAbsent() {
            // given
            String responseJson = """
                    {
                      "predictions": [
                        {
                          "bytesBase64Encoded": "base64data"
                        }
                      ]
                    }
                    """;

            // when
            GeneratedImageData result = sut.parseImage(responseJson);

            // then
            assertThat(result.mimeType()).isEqualTo("image/jpeg");
        }

        @Test
        @DisplayName("predictions 배열이 비어 있으면 EXTERNAL_AI_EMPTY_RESPONSE")
        void shouldThrowWhenPredictionsEmpty() {
            assertThatThrownBy(() -> sut.parseImage("{\"predictions\": []}"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.EXTERNAL_AI_EMPTY_RESPONSE));
        }

        @Test
        @DisplayName("응답 자체가 null이면 EXTERNAL_AI_EMPTY_RESPONSE")
        void shouldThrowWhenResponseIsNull() {
            assertThatThrownBy(() -> sut.parseImage(null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.EXTERNAL_AI_EMPTY_RESPONSE));
        }

        @Test
        @DisplayName("bytesBase64Encoded가 없으면 EXTERNAL_AI_EMPTY_RESPONSE")
        void shouldThrowWhenImageDataBlank() {
            // given — predictions는 있지만 bytesBase64Encoded 필드 없음
            String responseJson = """
                    {
                      "predictions": [
                        {
                          "mimeType": "image/jpeg"
                        }
                      ]
                    }
                    """;

            assertThatThrownBy(() -> sut.parseImage(responseJson))
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

        private GeminiImageWebClient stubWith(String returnValue) {
            return new GeminiImageWebClient(mock(RestClient.class), properties) {
                @Override
                protected String executeHttpCall(String url, Object body) {
                    return returnValue;
                }
            };
        }

        private GeminiImageWebClient throwingWith(RuntimeException ex) {
            return new GeminiImageWebClient(mock(RestClient.class), properties) {
                @Override
                protected String executeHttpCall(String url, Object body) {
                    throw ex;
                }
            };
        }

        @Test
        @DisplayName("HTTP 호출 성공 시 parseImage를 거쳐 이미지 데이터를 반환한다")
        void shouldReturnParsedImageOnSuccess() {
            // given — Imagen API 응답 구조
            String responseJson = """
                    {
                      "predictions": [
                        {
                          "bytesBase64Encoded": "abc123base64==",
                          "mimeType": "image/jpeg"
                        }
                      ]
                    }
                    """;
            GeminiImageWebClient sut = stubWith(responseJson);

            // when
            GeneratedImageData result = sut.generate("음식 사진 생성", "1:1");

            // then
            assertThat(result.base64Data()).isEqualTo("abc123base64==");
            assertThat(result.mimeType()).isEqualTo("image/jpeg");
        }

        @Test
        @DisplayName("HTTP 호출에서 RuntimeException 발생 시 EXTERNAL_AI_CALL_FAILED")
        void shouldWrapRuntimeExceptionAsCallFailed() {
            // given
            GeminiImageWebClient sut = throwingWith(new RuntimeException("Connection refused"));

            // when & then
            assertThatThrownBy(() -> sut.generate("음식 사진 생성", "1:1"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.EXTERNAL_AI_CALL_FAILED));
        }

        @Test
        @DisplayName("HTTP 호출에서 BusinessException 발생 시 그대로 rethrow된다")
        void shouldRethrowBusinessException() {
            // given
            BusinessException original = new BusinessException(AiErrorCode.EXTERNAL_AI_TIMEOUT);
            GeminiImageWebClient sut = throwingWith(original);

            // when & then
            assertThatThrownBy(() -> sut.generate("음식 사진 생성", "1:1"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.EXTERNAL_AI_TIMEOUT));
        }
    }
}