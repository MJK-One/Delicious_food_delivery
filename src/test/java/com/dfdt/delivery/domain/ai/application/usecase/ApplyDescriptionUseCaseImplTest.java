package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.application.dto.ApplyDescriptionCommand;
import com.dfdt.delivery.domain.ai.application.dto.ApplyDescriptionResult;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplyDescriptionUseCaseImpl 테스트")
class ApplyDescriptionUseCaseImplTest {

    @Mock
    private AiLogRepository aiLogRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private ProductForAiPort productForAiPort;

    @InjectMocks
    private ApplyDescriptionUseCaseImpl sut;

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
    @DisplayName("정상 실행")
    class SuccessTests {

        @Test
        @DisplayName("OWNER가 본인 가게의 AI 설명을 Product에 적용하면 성공")
        void ownerAppliesDescription_success() {
            // given
            AiLogEntity mockLog = mockAiLog(storeId, productId, false, "바삭한 치킨입니다!");
            Store mockStore = mockStore(requestedBy);

            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.of(mockLog));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));
            given(productForAiPort.applyAiDescription(eq(productId), eq(storeId), eq("바삭한 치킨입니다!"), eq(requestedBy)))
                    .willReturn(Optional.of("이전 설명"));
            given(mockLog.getAppliedAt()).willReturn(OffsetDateTime.now());

            ApplyDescriptionCommand command = new ApplyDescriptionCommand(
                    storeId, aiLogId, requestedBy, UserRole.OWNER
            );

            // when
            ApplyDescriptionResult result = sut.execute(command);

            // then
            assertThat(result.aiLogId()).isEqualTo(aiLogId);
            assertThat(result.productId()).isEqualTo(productId);
            assertThat(result.appliedDescription()).isEqualTo("바삭한 치킨입니다!");
            then(productForAiPort).should().applyAiDescription(eq(productId), eq(storeId), eq("바삭한 치킨입니다!"), eq(requestedBy));
            then(mockLog).should().applyDescription(any(), eq(requestedBy));
        }

        @Test
        @DisplayName("MASTER는 소유권 검사 없이 적용 가능")
        void masterAppliesDescription_skipsOwnershipCheck() {
            // given
            AiLogEntity mockLog = mockAiLog(storeId, productId, false, "치킨 설명");

            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.of(mockLog));
            given(productForAiPort.applyAiDescription(eq(productId), eq(storeId), eq("치킨 설명"), eq("masterUser")))
                    .willReturn(Optional.of("이전 설명"));
            given(mockLog.getAppliedAt()).willReturn(OffsetDateTime.now());

            ApplyDescriptionCommand command = new ApplyDescriptionCommand(
                    storeId, aiLogId, "masterUser", UserRole.MASTER
            );

            // when & then (소유권 체크 없이 성공)
            assertThatCode(() -> sut.execute(command)).doesNotThrowAnyException();
            then(storeRepository).should(never()).findByStoreIdAndNotDeleted(any());
        }
    }

    // ──────────────────────────────────────────────────
    // AiLog 조회 관련 예외
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("AiLog 관련 예외")
    class AiLogExceptionTests {

        @Test
        @DisplayName("존재하지 않는 aiLogId → AI_LOG_NOT_FOUND")
        void shouldThrowWhenAiLogNotFound() {
            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.empty());

            ApplyDescriptionCommand command = new ApplyDescriptionCommand(
                    storeId, aiLogId, requestedBy, UserRole.OWNER
            );

            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.AI_LOG_NOT_FOUND));
        }

        @Test
        @DisplayName("aiLog.storeId가 pathVariable storeId와 불일치 → STORE_NOT_FOUND")
        void shouldThrowWhenStoreIdMismatch() {
            UUID differentStoreId = UUID.randomUUID();
            AiLogEntity mockLog = mockAiLog(differentStoreId, productId, false, "설명");
            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.of(mockLog));

            ApplyDescriptionCommand command = new ApplyDescriptionCommand(
                    storeId, aiLogId, requestedBy, UserRole.OWNER
            );

            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.STORE_NOT_FOUND));
        }

        @Test
        @DisplayName("이미 적용된 AiLog → ALREADY_APPLIED")
        void shouldThrowWhenAlreadyApplied() {
            AiLogEntity mockLog = mockAiLog(storeId, productId, true, "설명");
            Store mockStore = mockStore(requestedBy);

            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.of(mockLog));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));

            ApplyDescriptionCommand command = new ApplyDescriptionCommand(
                    storeId, aiLogId, requestedBy, UserRole.OWNER
            );

            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.ALREADY_APPLIED));
        }

        @Test
        @DisplayName("productId가 없는 미리보기 전용 로그 → PRODUCT_ID_REQUIRED_FOR_APPLY")
        void shouldThrowWhenProductIdMissing() {
            AiLogEntity mockLog = mockAiLog(storeId, null, false, "설명");
            Store mockStore = mockStore(requestedBy);

            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.of(mockLog));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));

            ApplyDescriptionCommand command = new ApplyDescriptionCommand(
                    storeId, aiLogId, requestedBy, UserRole.OWNER
            );

            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.PRODUCT_ID_REQUIRED_FOR_APPLY));
        }
    }

    // ──────────────────────────────────────────────────
    // 소유권 예외
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("소유권 예외")
    class OwnershipExceptionTests {

        @Test
        @DisplayName("OWNER가 타인 가게 aiLog 적용 시도 → STORE_ACCESS_DENIED")
        void shouldThrowWhenOwnerAccessesDifferentStore() {
            AiLogEntity mockLog = mockAiLog(storeId, productId, false, "설명");
            Store mockStore = mockStore("otherOwner");

            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.of(mockLog));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));

            ApplyDescriptionCommand command = new ApplyDescriptionCommand(
                    storeId, aiLogId, requestedBy, UserRole.OWNER // owner123 != otherOwner
            );

            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.STORE_ACCESS_DENIED));
        }

        @Test
        @DisplayName("Store가 조회되지 않으면 → STORE_NOT_FOUND")
        void shouldThrowWhenStoreNotFoundForOwnership() {
            AiLogEntity mockLog = mockAiLog(storeId, productId, false, "설명");

            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.of(mockLog));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.empty());

            ApplyDescriptionCommand command = new ApplyDescriptionCommand(
                    storeId, aiLogId, requestedBy, UserRole.OWNER
            );

            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.STORE_NOT_FOUND));
        }
    }

    // ──────────────────────────────────────────────────
    // Product 관련 예외
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("Product 관련 예외")
    class ProductExceptionTests {

        @Test
        @DisplayName("Product가 조회되지 않으면 → PRODUCT_NOT_FOUND")
        void shouldThrowWhenProductNotFound() {
            AiLogEntity mockLog = mockAiLog(storeId, productId, false, "설명");
            Store mockStore = mockStore(requestedBy);

            given(aiLogRepository.findById(aiLogId)).willReturn(Optional.of(mockLog));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));
            given(productForAiPort.applyAiDescription(eq(productId), eq(storeId), any(), eq(requestedBy)))
                    .willReturn(Optional.empty());

            ApplyDescriptionCommand command = new ApplyDescriptionCommand(
                    storeId, aiLogId, requestedBy, UserRole.OWNER
            );

            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.PRODUCT_NOT_FOUND));
        }
    }

    // ──────────────────────────────────────────────────
    // 헬퍼 메서드
    // ──────────────────────────────────────────────────

    private AiLogEntity mockAiLog(UUID aiLogStoreId, UUID aiLogProductId, boolean isApplied, String responseText) {
        AiLogEntity mockLog = mock(AiLogEntity.class);
        lenient().when(mockLog.getAiLogId()).thenReturn(aiLogId);
        lenient().when(mockLog.getStoreId()).thenReturn(aiLogStoreId);
        lenient().when(mockLog.getProductId()).thenReturn(aiLogProductId);
        lenient().when(mockLog.getIsApplied()).thenReturn(isApplied);
        lenient().when(mockLog.getResponseText()).thenReturn(responseText);
        return mockLog;
    }

    private Store mockStore(String ownerUsername) {
        User mockUser = mock(User.class);
        lenient().when(mockUser.getUsername()).thenReturn(ownerUsername);
        Store mockStore = mock(Store.class);
        lenient().when(mockStore.getUser()).thenReturn(mockUser);
        return mockStore;
    }
}