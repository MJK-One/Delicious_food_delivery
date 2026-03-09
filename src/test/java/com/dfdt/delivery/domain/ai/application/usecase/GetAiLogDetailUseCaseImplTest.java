package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.application.dto.AiLogDetailResult;
import com.dfdt.delivery.domain.ai.application.dto.GetAiLogDetailQuery;
import com.dfdt.delivery.domain.ai.domain.entity.AiLogEntity;
import com.dfdt.delivery.domain.ai.domain.entity.enums.AiRequestType;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
import com.dfdt.delivery.domain.ai.domain.repository.AiLogRepository;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.repository.StoreRepository;
import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAiLogDetailUseCaseImpl 테스트")
class GetAiLogDetailUseCaseImplTest {

    @InjectMocks
    private GetAiLogDetailUseCaseImpl useCase;

    @Mock
    private AiLogRepository aiLogRepository;

    @Mock
    private StoreRepository storeRepository;

    private UUID storeId;
    private UUID aiLogId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
        aiLogId = UUID.randomUUID();
        productId = UUID.randomUUID();
    }

    // ──────────────────────────────────────────────────
    // 정상 케이스
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("정상 요청")
    class SuccessTests {

        @Test
        @DisplayName("OWNER - 소유권 검증 통과 후 AI 로그 상세 반환")
        void ownerShouldReturnAiLogDetail() {
            // given
            AiLogEntity aiLog = mockAiLog(storeId, productId);
            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.of(aiLog));

            Store store = mockStore("owner123");
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(store));

            GetAiLogDetailQuery query = ownerQuery("owner123");

            // when
            AiLogDetailResult result = useCase.execute(query);

            // then
            assertThat(result.aiLogId()).isEqualTo(aiLogId);
            assertThat(result.storeId()).isEqualTo(storeId);
            verify(storeRepository).findByStoreIdAndNotDeleted(storeId);
        }

        @Test
        @DisplayName("MASTER - 소유권 검증 없이 AI 로그 상세 반환")
        void masterShouldSkipOwnershipCheck() {
            // given
            AiLogEntity aiLog = mockAiLog(storeId, productId);
            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.of(aiLog));

            GetAiLogDetailQuery query = new GetAiLogDetailQuery(storeId, aiLogId, "master", UserRole.MASTER);

            // when
            AiLogDetailResult result = useCase.execute(query);

            // then
            assertThat(result.aiLogId()).isEqualTo(aiLogId);
            verify(storeRepository, never()).findByStoreIdAndNotDeleted(any());
        }
    }

    // ──────────────────────────────────────────────────
    // AI 로그 / storeId 예외
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("AI 로그 / storeId 예외")
    class NotFoundTests {

        @Test
        @DisplayName("aiLogId에 해당하는 로그가 없으면 AI_LOG_NOT_FOUND 예외 발생")
        void shouldThrowWhenAiLogNotFound() {
            // given
            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.empty());

            GetAiLogDetailQuery query = ownerQuery("owner123");

            // when & then
            assertThatThrownBy(() -> useCase.execute(query))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.AI_LOG_NOT_FOUND));

            verify(storeRepository, never()).findByStoreIdAndNotDeleted(any());
        }

        @Test
        @DisplayName("storeId가 AI 로그의 storeId와 다르면 STORE_NOT_FOUND 예외 발생")
        void shouldThrowWhenStoreIdMismatch() {
            // given: AI 로그의 storeId는 다른 UUID
            UUID differentStoreId = UUID.randomUUID();
            AiLogEntity aiLog = mockAiLog(differentStoreId, productId);
            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.of(aiLog));

            GetAiLogDetailQuery query = ownerQuery("owner123"); // query.storeId() == storeId

            // when & then
            assertThatThrownBy(() -> useCase.execute(query))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.STORE_NOT_FOUND));

            verify(storeRepository, never()).findByStoreIdAndNotDeleted(any());
        }
    }

    // ──────────────────────────────────────────────────
    // 소유권 예외
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("소유권 예외")
    class OwnershipFailureTests {

        @Test
        @DisplayName("OWNER - 가게가 존재하지 않으면 STORE_NOT_FOUND 예외 발생")
        void shouldThrowWhenStoreNotFound() {
            // given
            AiLogEntity aiLog = mockAiLog(storeId, productId);
            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.of(aiLog));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.empty());

            GetAiLogDetailQuery query = ownerQuery("owner123");

            // when & then
            assertThatThrownBy(() -> useCase.execute(query))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.STORE_NOT_FOUND));
        }

        @Test
        @DisplayName("OWNER - 본인 가게가 아니면 STORE_ACCESS_DENIED 예외 발생")
        void shouldThrowWhenNotOwner() {
            // given: AI 로그의 가게 owner는 "realOwner", 요청자는 "intruder"
            AiLogEntity aiLog = mockAiLog(storeId, productId);
            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.of(aiLog));

            Store store = mockStore("realOwner");
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(store));

            GetAiLogDetailQuery query = ownerQuery("intruder");

            // when & then
            assertThatThrownBy(() -> useCase.execute(query))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.STORE_ACCESS_DENIED));
        }
    }

    // ──────────────────────────────────────────────────
    // 헬퍼 메서드
    // ──────────────────────────────────────────────────

    private GetAiLogDetailQuery ownerQuery(String username) {
        return new GetAiLogDetailQuery(storeId, aiLogId, username, UserRole.OWNER);
    }

    private Store mockStore(String ownerUsername) {
        User mockUser = mock(User.class);
        given(mockUser.getUsername()).willReturn(ownerUsername);
        Store mockStore = mock(Store.class);
        given(mockStore.getUser()).willReturn(mockUser);
        return mockStore;
    }

    private AiLogEntity mockAiLog(UUID aiLogStoreId, UUID aiLogProductId) {
        AiLogEntity aiLog = mock(AiLogEntity.class);
        // storeId 일치 검증에 항상 사용
        given(aiLog.getStoreId()).willReturn(aiLogStoreId);
        // 성공 경로에서 AiLogDetailResult.from() 매핑 시 사용 (예외 경로에서는 lenient)
        lenient().doReturn(aiLogId).when(aiLog).getAiLogId();
        lenient().doReturn(aiLogProductId).when(aiLog).getProductId();
        lenient().doReturn("owner123").when(aiLog).getRequestedBy();
        lenient().doReturn(AiRequestType.PRODUCT_DESCRIPTION).when(aiLog).getRequestType();
        lenient().doReturn("FRIENDLY").when(aiLog).getTone();
        lenient().doReturn("테스트 프롬프트").when(aiLog).getInputPrompt();
        lenient().doReturn("최종 프롬프트").when(aiLog).getFinalPrompt();
        lenient().doReturn("테스트 응답").when(aiLog).getResponseText();
        lenient().doReturn(true).when(aiLog).getIsSuccess();
        lenient().doReturn(false).when(aiLog).getIsApplied();
        lenient().doReturn(null).when(aiLog).getCreateAudit();
        return aiLog;
    }
}