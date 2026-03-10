package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.application.dto.RollbackDescriptionCommand;
import com.dfdt.delivery.domain.ai.application.dto.RollbackDescriptionResult;
import com.dfdt.delivery.domain.ai.domain.entity.AiLogEntity;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
import com.dfdt.delivery.domain.ai.domain.port.ProductForAiPort;
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

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("RollbackDescriptionUseCaseImpl 테스트")
class RollbackDescriptionUseCaseImplTest {

    @Mock
    private AiLogRepository aiLogRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private ProductForAiPort productForAiPort;

    @InjectMocks
    private RollbackDescriptionUseCaseImpl sut;

    private UUID storeId;
    private UUID aiLogId;
    private UUID productId;
    private String requestedBy;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
        aiLogId = UUID.randomUUID();
        productId = UUID.randomUUID();
        requestedBy = "owner123";
    }

    // ──────────────────────────────────────────────────
    // 정상 케이스
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("정상 원복")
    class SuccessTests {

        @Test
        @DisplayName("apply된 로그를 OWNER가 원복하면 이전 설명으로 복원된다")
        void shouldRollbackSuccessfullyForOwner() {
            // given
            AiLogEntity mockLog = mockAppliedLog(storeId, productId, "이전 설명", null);
            Store mockStore = mockStore(requestedBy);

            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.of(mockLog));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));
            given(productForAiPort.restoreDescription(eq(productId), eq(storeId), eq("이전 설명"), eq(requestedBy)))
                    .willReturn(true);

            RollbackDescriptionCommand command = new RollbackDescriptionCommand(
                    storeId, aiLogId, requestedBy, UserRole.OWNER
            );

            // when & then
            assertThatCode(() -> sut.execute(command)).doesNotThrowAnyException();
            then(productForAiPort).should().restoreDescription(eq(productId), eq(storeId), eq("이전 설명"), eq(requestedBy));
        }

        @Test
        @DisplayName("MASTER는 소유권 체크 없이 원복할 수 있다")
        void masterCanRollbackWithoutOwnershipCheck() {
            // given
            AiLogEntity mockLog = mockAppliedLog(storeId, productId, "이전 설명", null);

            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.of(mockLog));
            given(productForAiPort.restoreDescription(any(), any(), any(), any())).willReturn(true);

            RollbackDescriptionCommand command = new RollbackDescriptionCommand(
                    storeId, aiLogId, "masterUser", UserRole.MASTER
            );

            // when & then
            assertThatCode(() -> sut.execute(command)).doesNotThrowAnyException();
            then(storeRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("previousDescription이 null이어도 원복이 가능하다 (설명 없는 상태로 복원)")
        void shouldRollbackEvenWhenPreviousDescriptionIsNull() {
            // given
            AiLogEntity mockLog = mockAppliedLog(storeId, productId, null, null);

            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.of(mockLog));
            given(productForAiPort.restoreDescription(any(), any(), isNull(), eq("masterUser"))).willReturn(true);

            RollbackDescriptionCommand command = new RollbackDescriptionCommand(
                    storeId, aiLogId, "masterUser", UserRole.MASTER
            );

            // when & then
            assertThatCode(() -> sut.execute(command)).doesNotThrowAnyException();
            then(productForAiPort).should().restoreDescription(any(), any(), isNull(), eq("masterUser"));
        }
    }

    // ──────────────────────────────────────────────────
    // AiLog 관련 예외
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("AiLog 관련 예외")
    class AiLogExceptionTests {

        @Test
        @DisplayName("존재하지 않는 aiLogId → AI_LOG_NOT_FOUND")
        void shouldThrowWhenAiLogNotFound() {
            // given
            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.empty());

            RollbackDescriptionCommand command = new RollbackDescriptionCommand(
                    storeId, aiLogId, requestedBy, UserRole.OWNER
            );

            // when & then
            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.AI_LOG_NOT_FOUND));
        }

        @Test
        @DisplayName("storeId 불일치 → STORE_NOT_FOUND")
        void shouldThrowWhenStoreIdMismatch() {
            // given
            UUID differentStoreId = UUID.randomUUID();
            AiLogEntity mockLog = mockAppliedLog(differentStoreId, productId, "이전 설명", null);
            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.of(mockLog));

            RollbackDescriptionCommand command = new RollbackDescriptionCommand(
                    storeId, aiLogId, requestedBy, UserRole.OWNER
            );

            // when & then
            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.STORE_NOT_FOUND));
        }

        @Test
        @DisplayName("아직 적용되지 않은 로그 → NOT_YET_APPLIED")
        void shouldThrowWhenNotYetApplied() {
            // given — isApplied = false
            AiLogEntity mockLog = mockNotAppliedLog(storeId, productId);
            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.of(mockLog));

            RollbackDescriptionCommand command = new RollbackDescriptionCommand(
                    storeId, aiLogId, "masterUser", UserRole.MASTER
            );

            // when & then
            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.NOT_YET_APPLIED));
        }

        @Test
        @DisplayName("이미 롤백된 로그 → ALREADY_ROLLED_BACK")
        void shouldThrowWhenAlreadyRolledBack() {
            // given — rolledBackAt != null
            AiLogEntity mockLog = mockAppliedLog(storeId, productId, "이전 설명", OffsetDateTime.now().minusMinutes(5));
            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.of(mockLog));

            RollbackDescriptionCommand command = new RollbackDescriptionCommand(
                    storeId, aiLogId, "masterUser", UserRole.MASTER
            );

            // when & then
            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.ALREADY_ROLLED_BACK));
        }
    }

    // ──────────────────────────────────────────────────
    // Store 소유권 예외
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("Store 소유권 예외")
    class StoreOwnershipTests {

        @Test
        @DisplayName("OWNER가 타인 가게 로그 원복 시도 → STORE_ACCESS_DENIED")
        void shouldThrowWhenOwnerAccessesDifferentStore() {
            // given
            AiLogEntity mockLog = mockAppliedLog(storeId, productId, "이전 설명", null);
            Store mockStore = mockStore("anotherOwner");

            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.of(mockLog));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));

            RollbackDescriptionCommand command = new RollbackDescriptionCommand(
                    storeId, aiLogId, requestedBy, UserRole.OWNER
            );

            // when & then
            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.STORE_ACCESS_DENIED));
        }
    }

    // ──────────────────────────────────────────────────
    // 헬퍼 메서드
    // ──────────────────────────────────────────────────

    private AiLogEntity mockAppliedLog(UUID logStoreId, UUID logProductId,
                                       String previousDescription, OffsetDateTime rolledBackAt) {
        AiLogEntity log = mock(AiLogEntity.class);
        lenient().when(log.getStoreId()).thenReturn(logStoreId);
        lenient().when(log.getProductId()).thenReturn(logProductId);
        lenient().when(log.getIsApplied()).thenReturn(true);
        lenient().when(log.getRolledBackAt()).thenReturn(rolledBackAt);
        lenient().when(log.getPreviousDescription()).thenReturn(previousDescription);
        lenient().when(log.getAiLogId()).thenReturn(aiLogId);
        lenient().when(log.getRolledBackAt()).thenReturn(rolledBackAt);
        return log;
    }

    private AiLogEntity mockNotAppliedLog(UUID logStoreId, UUID logProductId) {
        AiLogEntity log = mock(AiLogEntity.class);
        lenient().when(log.getStoreId()).thenReturn(logStoreId);
        lenient().when(log.getProductId()).thenReturn(logProductId);
        lenient().when(log.getIsApplied()).thenReturn(false);
        return log;
    }

    private Store mockStore(String ownerUsername) {
        User mockUser = mock(User.class);
        lenient().when(mockUser.getUsername()).thenReturn(ownerUsername);
        Store mockStore = mock(Store.class);
        lenient().when(mockStore.getUser()).thenReturn(mockUser);
        return mockStore;
    }
}
