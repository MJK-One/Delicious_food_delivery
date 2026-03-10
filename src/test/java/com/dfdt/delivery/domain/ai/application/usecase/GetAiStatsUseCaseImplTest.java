package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.application.dto.AiStatsQuery;
import com.dfdt.delivery.domain.ai.application.dto.AiStatsResult;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
import com.dfdt.delivery.domain.ai.domain.repository.AiLogCustomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAiStatsUseCaseImpl 테스트")
class GetAiStatsUseCaseImplTest {

    @Mock
    private AiLogCustomRepository aiLogCustomRepository;

    private GetAiStatsUseCaseImpl sut;

    private UUID storeId;

    @BeforeEach
    void setUp() {
        sut = new GetAiStatsUseCaseImpl(aiLogCustomRepository);
        storeId = UUID.randomUUID();
    }

    // ──────────────────────────────────────────────────
    // 정상 케이스
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("정상 요청")
    class SuccessTests {

        @Test
        @DisplayName("날짜 조건 없이 전체 기간 통계를 반환한다")
        void shouldReturnStatsWithoutDateRange() {
            // given
            AiStatsResult mockResult = new AiStatsResult(storeId, 10L, 8L, 2L, 80.0, 350L, null, null);
            given(aiLogCustomRepository.getAiStats(eq(storeId), isNull(), isNull(), isNull()))
                    .willReturn(mockResult);

            AiStatsQuery query = new AiStatsQuery(storeId, null, null, null);

            // when
            AiStatsResult result = sut.execute(query);

            // then
            assertThat(result.totalCount()).isEqualTo(10L);
            assertThat(result.successCount()).isEqualTo(8L);
            assertThat(result.failureCount()).isEqualTo(2L);
            assertThat(result.successRate()).isEqualTo(80.0);
            assertThat(result.avgResponseTimeMs()).isEqualTo(350L);
        }

        @Test
        @DisplayName("날짜 범위를 지정해도 정상 반환한다")
        void shouldReturnStatsWithDateRange() {
            // given
            OffsetDateTime from = OffsetDateTime.now().minusDays(7);
            OffsetDateTime to = OffsetDateTime.now();
            AiStatsResult mockResult = new AiStatsResult(storeId, 5L, 5L, 0L, 100.0, 200L, from, to);
            given(aiLogCustomRepository.getAiStats(eq(storeId), eq(from), eq(to), isNull()))
                    .willReturn(mockResult);

            AiStatsQuery query = new AiStatsQuery(storeId, from, to, null);

            // when
            AiStatsResult result = sut.execute(query);

            // then
            assertThat(result.successRate()).isEqualTo(100.0);
            assertThat(result.fromDateTime()).isEqualTo(from);
        }

        @Test
        @DisplayName("AI 호출이 없으면 totalCount=0, successRate=0.0을 반환한다")
        void shouldReturnZeroStatsWhenNoLogs() {
            // given
            AiStatsResult mockResult = new AiStatsResult(storeId, 0L, 0L, 0L, 0.0, null, null, null);
            given(aiLogCustomRepository.getAiStats(any(), any(), any(), any())).willReturn(mockResult);

            AiStatsQuery query = new AiStatsQuery(storeId, null, null, null);

            // when
            AiStatsResult result = sut.execute(query);

            // then
            assertThat(result.totalCount()).isEqualTo(0L);
            assertThat(result.successRate()).isEqualTo(0.0);
            assertThat(result.avgResponseTimeMs()).isNull();
        }
    }

    // ──────────────────────────────────────────────────
    // 날짜 범위 검증
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("날짜 범위 검증 예외")
    class DateRangeValidationTests {

        @Test
        @DisplayName("fromDateTime이 toDateTime보다 이후면 INVALID_DATE_RANGE")
        void shouldThrowWhenFromAfterTo() {
            // given
            OffsetDateTime from = OffsetDateTime.now();
            OffsetDateTime to = from.minusDays(1);

            AiStatsQuery query = new AiStatsQuery(storeId, from, to, null);

            // when & then
            assertThatThrownBy(() -> sut.execute(query))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.INVALID_DATE_RANGE));
        }

        @Test
        @DisplayName("날짜 범위가 90일을 초과하면 DATE_RANGE_EXCEEDED")
        void shouldThrowWhenRangeExceeds90Days() {
            // given
            OffsetDateTime from = OffsetDateTime.now().minusDays(91);
            OffsetDateTime to = OffsetDateTime.now();

            AiStatsQuery query = new AiStatsQuery(storeId, from, to, null);

            // when & then
            assertThatThrownBy(() -> sut.execute(query))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.DATE_RANGE_EXCEEDED));
        }

        @Test
        @DisplayName("날짜 범위가 정확히 90일이면 통과한다")
        void shouldPassWhenRangeIsExactly90Days() {
            // given
            OffsetDateTime from = OffsetDateTime.now().minusDays(90);
            OffsetDateTime to = OffsetDateTime.now();
            AiStatsResult mockResult = new AiStatsResult(storeId, 0L, 0L, 0L, 0.0, null, from, to);
            given(aiLogCustomRepository.getAiStats(any(), any(), any(), any())).willReturn(mockResult);

            AiStatsQuery query = new AiStatsQuery(storeId, from, to, null);

            // when & then
            assertThatCode(() -> sut.execute(query)).doesNotThrowAnyException();
        }
    }
}
