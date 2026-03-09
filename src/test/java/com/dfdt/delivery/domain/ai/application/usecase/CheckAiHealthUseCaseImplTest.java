package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.application.dto.AiHealthResult;
import com.dfdt.delivery.domain.ai.domain.client.GeminiClient;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
import com.dfdt.delivery.domain.ai.infrastructure.config.GeminiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckAiHealthUseCaseImpl 테스트")
class CheckAiHealthUseCaseImplTest {

    // GeminiProperties는 record(final class)라 Mockito로 mock 불가 → 실제 인스턴스 사용
    private static final GeminiProperties PROPERTIES =
            new GeminiProperties("test-api-key", "gemini-2.0-flash", "https://test.googleapis.com");

    @Mock
    private GeminiClient geminiClient;

    private CheckAiHealthUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CheckAiHealthUseCaseImpl(geminiClient, PROPERTIES);
    }

    // ──────────────────────────────────────────────────
    // 정상 케이스
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("정상 요청")
    class SuccessTests {

        @Test
        @DisplayName("Gemini API 호출 성공 시 UP 상태를 반환한다")
        void shouldReturnUpWhenGeminiResponds() {
            // given
            given(geminiClient.generate(anyString())).willReturn("pong");

            // when
            AiHealthResult result = useCase.execute();

            // then
            assertThat(result.status()).isEqualTo("UP");
            assertThat(result.modelName()).isEqualTo("gemini-2.0-flash");
            assertThat(result.responseTimeMs()).isGreaterThanOrEqualTo(0);
            assertThat(result.errorMessage()).isNull();
            assertThat(result.checkedAt()).isNotNull();
        }

        @Test
        @DisplayName("UP 상태 시 responseTimeMs가 0 이상으로 반환된다")
        void shouldReturnNonNegativeResponseTimeOnSuccess() {
            // given
            given(geminiClient.generate(anyString())).willReturn("ok");

            // when
            AiHealthResult result = useCase.execute();

            // then
            assertThat(result.responseTimeMs()).isGreaterThanOrEqualTo(0);
        }
    }

    // ──────────────────────────────────────────────────
    // 실패 케이스
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("Gemini API 실패")
    class FailureTests {

        @Test
        @DisplayName("BusinessException 발생 시 DOWN 상태와 에러 메시지를 반환한다")
        void shouldReturnDownOnBusinessException() {
            // given
            BusinessException ex = new BusinessException(AiErrorCode.EXTERNAL_AI_CALL_FAILED);
            given(geminiClient.generate(anyString())).willThrow(ex);

            // when
            AiHealthResult result = useCase.execute();

            // then
            assertThat(result.status()).isEqualTo("DOWN");
            assertThat(result.modelName()).isEqualTo("gemini-2.0-flash");
            assertThat(result.responseTimeMs()).isGreaterThanOrEqualTo(0);
            assertThat(result.errorMessage()).isNotNull();
            assertThat(result.checkedAt()).isNotNull();
        }

        @Test
        @DisplayName("RuntimeException 발생 시 DOWN 상태를 반환한다")
        void shouldReturnDownOnRuntimeException() {
            // given
            given(geminiClient.generate(anyString())).willThrow(new RuntimeException("network error"));

            // when
            AiHealthResult result = useCase.execute();

            // then
            assertThat(result.status()).isEqualTo("DOWN");
            assertThat(result.errorMessage()).isEqualTo("network error");
        }

        @Test
        @DisplayName("타임아웃 예외 발생 시 DOWN 상태를 반환한다")
        void shouldReturnDownOnTimeoutException() {
            // given
            BusinessException ex = new BusinessException(AiErrorCode.EXTERNAL_AI_TIMEOUT);
            given(geminiClient.generate(anyString())).willThrow(ex);

            // when
            AiHealthResult result = useCase.execute();

            // then
            assertThat(result.status()).isEqualTo("DOWN");
            assertThat(result.modelName()).isEqualTo("gemini-2.0-flash");
        }
    }
}
