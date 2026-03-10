package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.application.dto.GenerateDescriptionCommand;
import com.dfdt.delivery.domain.ai.application.dto.GenerateDescriptionResult;
import com.dfdt.delivery.domain.ai.domain.client.GeminiClient;
import com.dfdt.delivery.domain.ai.domain.entity.AiLogEntity;
import com.dfdt.delivery.domain.ai.domain.entity.enums.Tone;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
import com.dfdt.delivery.domain.ai.domain.policy.AiPromptPolicy;
import com.dfdt.delivery.domain.ai.domain.port.ProductForAiPort;
import com.dfdt.delivery.domain.ai.domain.port.ProductInfo;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateDescriptionUseCaseImpl 테스트")
class GenerateDescriptionUseCaseImplTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private ProductForAiPort productForAiPort;

    @Mock
    private AiLogRepository aiLogRepository;

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private AiPromptPolicy aiPromptPolicy;

    @InjectMocks
    private GenerateDescriptionUseCaseImpl sut;

    // 공통 픽스처
    private UUID storeId;
    private UUID productId;
    private String requestedBy;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
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
        @DisplayName("productId 없이 productName으로 미리보기 성공")
        void shouldSucceedWithProductNameOnly() {
            // given
            Store mockStore = mockStore(requestedBy);
            GenerateDescriptionCommand command = new GenerateDescriptionCommand(
                    storeId, requestedBy, UserRole.OWNER,
                    null, "황금 바삭치킨",
                    "겉은 바삭 속은 촉촉하게 작성해줘", Tone.FRIENDLY, List.of("바삭")
            );

            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));
            given(aiPromptPolicy.buildFinalPrompt(any(), any(), any(), any())).willReturn("최종프롬프트");
            given(geminiClient.generate(any())).willReturn("바삭바삭한 황금 치킨입니다.");
            given(aiPromptPolicy.trimResponse(any())).willReturn("바삭바삭한 황금 치킨입니다.");
            given(aiLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            GenerateDescriptionResult result = sut.execute(command);

            // then
            assertThat(result.responseText()).isEqualTo("바삭바삭한 황금 치킨입니다.");
            assertThat(result.finalPrompt()).isEqualTo("최종프롬프트");
            then(aiLogRepository).should().save(any(AiLogEntity.class));
        }

        @Test
        @DisplayName("productId가 있으면 product.getName()이 프롬프트에 사용된다")
        void shouldUseProductNameFromProductEntityWhenProductIdProvided() {
            // given
            Store mockStore = mockStore(requestedBy);

            GenerateDescriptionCommand command = new GenerateDescriptionCommand(
                    storeId, requestedBy, UserRole.OWNER,
                    productId, null,
                    "설명 작성해줘", Tone.INFORMATIVE, null
            );

            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));
            given(productForAiPort.findActive(productId, storeId)).willReturn(Optional.of(new ProductInfo(productId, "황금 바삭치킨")));
            given(aiPromptPolicy.buildFinalPrompt(eq("황금 바삭치킨"), any(), any(), any())).willReturn("최종프롬프트");
            given(geminiClient.generate(any())).willReturn("황금 바삭치킨 설명");
            given(aiPromptPolicy.trimResponse(any())).willReturn("황금 바삭치킨 설명");
            given(aiLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            sut.execute(command);

            // then
            then(aiPromptPolicy).should().buildFinalPrompt(eq("황금 바삭치킨"), any(), any(), any());
        }

        @Test
        @DisplayName("MASTER 역할은 가게 소유권 체크 없이 통과한다")
        void masterRoleShouldSkipOwnershipCheck() {
            // given
            Store mockStore = mockStore("otherOwner"); // 다른 사람 소유
            GenerateDescriptionCommand command = new GenerateDescriptionCommand(
                    storeId, "masterUser", UserRole.MASTER,
                    null, "치킨",
                    "설명 작성", Tone.SALESY, null
            );

            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));
            given(aiPromptPolicy.buildFinalPrompt(any(), any(), any(), any())).willReturn("최종프롬프트");
            given(geminiClient.generate(any())).willReturn("치킨 맛있어요");
            given(aiPromptPolicy.trimResponse(any())).willReturn("치킨 맛있어요");
            given(aiLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when & then (예외 없이 통과)
            assertThatCode(() -> sut.execute(command)).doesNotThrowAnyException();
        }
    }

    // ──────────────────────────────────────────────────
    // 예외 케이스 — Store
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("Store 관련 예외")
    class StoreExceptionTests {

        @Test
        @DisplayName("존재하지 않는 storeId → STORE_NOT_FOUND")
        void shouldThrowWhenStoreNotFound() {
            // given
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.empty());

            GenerateDescriptionCommand command = new GenerateDescriptionCommand(
                    storeId, requestedBy, UserRole.OWNER,
                    null, "치킨", "설명", Tone.FRIENDLY, null
            );

            // when & then
            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.STORE_NOT_FOUND));
        }

        @Test
        @DisplayName("OWNER가 본인 소유가 아닌 가게 접근 → STORE_ACCESS_DENIED")
        void shouldThrowWhenOwnerAccessesDifferentStore() {
            // given
            Store mockStore = mockStore("anotherOwner"); // 다른 사람 소유
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));

            GenerateDescriptionCommand command = new GenerateDescriptionCommand(
                    storeId, requestedBy, UserRole.OWNER, // requestedBy = "owner123"
                    null, "치킨", "설명", Tone.FRIENDLY, null
            );

            // when & then
            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.STORE_ACCESS_DENIED));
        }
    }

    // ──────────────────────────────────────────────────
    // 예외 케이스 — Product
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("Product 관련 예외")
    class ProductExceptionTests {

        @Test
        @DisplayName("productId에 해당하는 상품이 없으면 PRODUCT_NOT_FOUND")
        void shouldThrowWhenProductNotFound() {
            // given
            Store mockStore = mockStore(requestedBy);
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));
            given(productForAiPort.findActive(productId, storeId)).willReturn(Optional.empty());

            GenerateDescriptionCommand command = new GenerateDescriptionCommand(
                    storeId, requestedBy, UserRole.OWNER,
                    productId, null, "설명", Tone.FRIENDLY, null
            );

            // when & then
            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.PRODUCT_NOT_FOUND));
        }

        @Test
        @DisplayName("소프트 삭제된 상품이면 PRODUCT_NOT_FOUND")
        void shouldThrowWhenProductIsSoftDeleted() {
            // given
            Store mockStore = mockStore(requestedBy);

            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));
            // 포트 구현체가 soft delete 필터링 후 empty를 반환
            given(productForAiPort.findActive(productId, storeId)).willReturn(Optional.empty());

            GenerateDescriptionCommand command = new GenerateDescriptionCommand(
                    storeId, requestedBy, UserRole.OWNER,
                    productId, null, "설명", Tone.FRIENDLY, null
            );

            // when & then
            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.PRODUCT_NOT_FOUND));
        }
    }

    // ──────────────────────────────────────────────────
    // 예외 케이스 — GeminiClient 실패
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("GeminiClient 실패 처리")
    class GeminiClientFailureTests {

        @Test
        @DisplayName("Gemini 호출 실패 시 실패 로그를 저장하고 예외를 다시 던진다")
        void shouldSaveFailureLogAndRethrowOnGeminiFailure() {
            // given
            Store mockStore = mockStore(requestedBy);
            BusinessException geminiException = new BusinessException(AiErrorCode.EXTERNAL_AI_CALL_FAILED);

            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));
            given(aiPromptPolicy.buildFinalPrompt(any(), any(), any(), any())).willReturn("최종프롬프트");
            given(geminiClient.generate(any())).willThrow(geminiException);
            given(aiLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            GenerateDescriptionCommand command = new GenerateDescriptionCommand(
                    storeId, requestedBy, UserRole.OWNER,
                    null, "치킨", "설명", Tone.FRIENDLY, null
            );

            // when & then
            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.EXTERNAL_AI_CALL_FAILED));

            // 실패 로그가 저장되어야 함
            then(aiLogRepository).should().save(any(AiLogEntity.class));
        }
    }

    // ──────────────────────────────────────────────────
    // 헬퍼 메서드
    // ──────────────────────────────────────────────────

    private Store mockStore(String ownerUsername) {
        User mockUser = mock(User.class);
        // MASTER 역할 테스트에서는 소유권 체크가 없어 호출되지 않을 수 있으므로 lenient 사용
        lenient().when(mockUser.getUsername()).thenReturn(ownerUsername);

        Store mockStore = mock(Store.class);
        lenient().when(mockStore.getUser()).thenReturn(mockUser);

        return mockStore;
    }

}

