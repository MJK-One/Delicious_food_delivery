package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.domain.ai.application.dto.RetryDescriptionCommand;
import com.dfdt.delivery.domain.ai.application.dto.RetryDescriptionResult;
import com.dfdt.delivery.domain.ai.domain.client.GeminiClient;
import com.dfdt.delivery.domain.ai.domain.entity.AiLogEntity;
import com.dfdt.delivery.domain.ai.domain.entity.enums.AiRequestType;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
import com.dfdt.delivery.domain.ai.domain.policy.AiPromptPolicy;
import com.dfdt.delivery.domain.ai.domain.repository.AiLogRepository;
import com.dfdt.delivery.domain.product.domain.entity.Product;
import com.dfdt.delivery.domain.product.domain.repository.ProductRepository;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("RetryDescriptionUseCaseImpl 테스트")
class RetryDescriptionUseCaseImplTest {

    @Mock
    private AiLogRepository aiLogRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private AiPromptPolicy aiPromptPolicy;

    @InjectMocks
    private RetryDescriptionUseCaseImpl sut;

    // 공통 픽스처
    private UUID storeId;
    private UUID productId;
    private UUID sourceAiLogId;
    private String requestedBy;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
        productId = UUID.randomUUID();
        sourceAiLogId = UUID.randomUUID();
        requestedBy = "owner123";
    }

    // ──────────────────────────────────────────────────
    // 정상 케이스
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("정상 실행")
    class SuccessTests {

        @Test
        @DisplayName("원본 inputPrompt를 그대로 사용하여 재실행 성공")
        void shouldSucceedUsingOriginalInputPrompt() {
            // given
            Store mockStore = mockStore(requestedBy);
            AiLogEntity sourceLog = mockSourceLog(AiRequestType.PRODUCT_DESCRIPTION,
                    "원본 프롬프트", "FRIENDLY", "바삭,촉촉", null);

            given(aiLogRepository.findById(sourceAiLogId)).willReturn(Optional.of(sourceLog));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));
            given(aiPromptPolicy.buildFinalPrompt(isNull(), eq("원본 프롬프트"), any(), any()))
                    .willReturn("최종프롬프트");
            given(geminiClient.generate("최종프롬프트")).willReturn("바삭한 치킨입니다!");
            given(aiPromptPolicy.trimResponse("바삭한 치킨입니다!")).willReturn("바삭한 치킨입니다!");
            given(aiLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            RetryDescriptionCommand command = new RetryDescriptionCommand(
                    storeId, sourceAiLogId, requestedBy, UserRole.OWNER, null
            );

            // when
            RetryDescriptionResult result = sut.execute(command);

            // then
            assertThat(result.responseText()).isEqualTo("바삭한 치킨입니다!");
            assertThat(result.sourceAiLogId()).isEqualTo(sourceAiLogId);
            then(aiLogRepository).should().save(any(AiLogEntity.class));
        }

        @Test
        @DisplayName("overrideInputPrompt가 있으면 원본 프롬프트 대신 사용한다")
        void shouldUseOverrideInputPromptWhenProvided() {
            // given
            Store mockStore = mockStore(requestedBy);
            AiLogEntity sourceLog = mockSourceLog(AiRequestType.PRODUCT_DESCRIPTION,
                    "원본 프롬프트", "SALESY", null, null);

            given(aiLogRepository.findById(sourceAiLogId)).willReturn(Optional.of(sourceLog));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));
            given(aiPromptPolicy.buildFinalPrompt(isNull(), eq("변경된 프롬프트"), any(), any()))
                    .willReturn("최종프롬프트");
            given(geminiClient.generate(any())).willReturn("맛있는 치킨!");
            given(aiPromptPolicy.trimResponse(any())).willReturn("맛있는 치킨!");
            given(aiLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            RetryDescriptionCommand command = new RetryDescriptionCommand(
                    storeId, sourceAiLogId, requestedBy, UserRole.OWNER, "변경된 프롬프트"
            );

            // when
            sut.execute(command);

            // then: override 프롬프트가 buildFinalPrompt에 전달되었는지 확인
            then(aiPromptPolicy).should().buildFinalPrompt(isNull(), eq("변경된 프롬프트"), any(), any());
        }

        @Test
        @DisplayName("productId가 있으면 product.getName()을 프롬프트에 사용한다")
        void shouldUseProductNameWhenProductIdExists() {
            // given
            Store mockStore = mockStore(requestedBy);
            AiLogEntity sourceLog = mockSourceLog(AiRequestType.PRODUCT_DESCRIPTION,
                    "원본 프롬프트", "FRIENDLY", null, productId);
            Product mockProduct = mockProduct("황금 바삭치킨", false);

            given(aiLogRepository.findById(sourceAiLogId)).willReturn(Optional.of(sourceLog));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));
            given(productRepository.findByProductIdAndStoreId(productId, storeId))
                    .willReturn(Optional.of(mockProduct));
            given(aiPromptPolicy.buildFinalPrompt(eq("황금 바삭치킨"), any(), any(), any()))
                    .willReturn("최종프롬프트");
            given(geminiClient.generate(any())).willReturn("황금 바삭치킨!");
            given(aiPromptPolicy.trimResponse(any())).willReturn("황금 바삭치킨!");
            given(aiLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            RetryDescriptionCommand command = new RetryDescriptionCommand(
                    storeId, sourceAiLogId, requestedBy, UserRole.OWNER, null
            );

            // when
            sut.execute(command);

            // then
            then(aiPromptPolicy).should().buildFinalPrompt(eq("황금 바삭치킨"), any(), any(), any());
        }

        @Test
        @DisplayName("MASTER 역할은 가게 소유권 체크 없이 통과한다")
        void masterRoleShouldSkipOwnershipCheck() {
            // given
            AiLogEntity sourceLog = mockSourceLog(AiRequestType.PRODUCT_DESCRIPTION,
                    "원본 프롬프트", "INFORMATIVE", null, null);

            given(aiLogRepository.findById(sourceAiLogId)).willReturn(Optional.of(sourceLog));
            given(aiPromptPolicy.buildFinalPrompt(any(), any(), any(), any())).willReturn("최종프롬프트");
            given(geminiClient.generate(any())).willReturn("치킨 맛있어요");
            given(aiPromptPolicy.trimResponse(any())).willReturn("치킨 맛있어요");
            given(aiLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            RetryDescriptionCommand command = new RetryDescriptionCommand(
                    storeId, sourceAiLogId, "masterUser", UserRole.MASTER, null
            );

            // when & then (예외 없이 통과, storeRepository 호출 없음)
            assertThatCode(() -> sut.execute(command)).doesNotThrowAnyException();
            then(storeRepository).shouldHaveNoInteractions();
        }
    }

    // ──────────────────────────────────────────────────
    // 예외 케이스 — AI 로그
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("AI 로그 관련 예외")
    class AiLogExceptionTests {

        @Test
        @DisplayName("존재하지 않는 sourceAiLogId → AI_LOG_NOT_FOUND")
        void shouldThrowWhenSourceLogNotFound() {
            // given
            given(aiLogRepository.findById(sourceAiLogId)).willReturn(Optional.empty());

            RetryDescriptionCommand command = new RetryDescriptionCommand(
                    storeId, sourceAiLogId, requestedBy, UserRole.OWNER, null
            );

            // when & then
            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.AI_LOG_NOT_FOUND));
        }

        @Test
        @DisplayName("원본 로그의 storeId와 URL의 storeId가 다르면 STORE_NOT_FOUND")
        void shouldThrowWhenStoreIdMismatch() {
            // given
            UUID differentStoreId = UUID.randomUUID();
            AiLogEntity sourceLog = mockSourceLog(AiRequestType.PRODUCT_DESCRIPTION,
                    "프롬프트", "FRIENDLY", null, null);
            // 원본 로그는 storeId를, command는 differentStoreId를 사용
            given(aiLogRepository.findById(sourceAiLogId)).willReturn(Optional.of(sourceLog));

            RetryDescriptionCommand command = new RetryDescriptionCommand(
                    differentStoreId, sourceAiLogId, requestedBy, UserRole.OWNER, null
            );

            // when & then
            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.STORE_NOT_FOUND));
        }

        @Test
        @DisplayName("FOOD_IMAGE_GENERATION 타입은 재실행 불가 → RETRY_NOT_SUPPORTED_TYPE")
        void shouldThrowWhenRequestTypeIsNotProductDescription() {
            // given
            Store mockStore = mockStore(requestedBy);
            AiLogEntity sourceLog = mockSourceLog(AiRequestType.FOOD_IMAGE_GENERATION,
                    "프롬프트", "FRIENDLY", null, null);

            given(aiLogRepository.findById(sourceAiLogId)).willReturn(Optional.of(sourceLog));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));

            RetryDescriptionCommand command = new RetryDescriptionCommand(
                    storeId, sourceAiLogId, requestedBy, UserRole.OWNER, null
            );

            // when & then
            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.RETRY_NOT_SUPPORTED_TYPE));
        }
    }

    // ──────────────────────────────────────────────────
    // 예외 케이스 — Store / 소유권
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("Store 소유권 예외")
    class StoreOwnershipExceptionTests {

        @Test
        @DisplayName("OWNER가 본인 소유가 아닌 가게 접근 → STORE_ACCESS_DENIED")
        void shouldThrowWhenOwnerAccessesDifferentStore() {
            // given
            Store mockStore = mockStore("anotherOwner");
            AiLogEntity sourceLog = mockSourceLog(AiRequestType.PRODUCT_DESCRIPTION,
                    "프롬프트", "FRIENDLY", null, null);

            given(aiLogRepository.findById(sourceAiLogId)).willReturn(Optional.of(sourceLog));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));

            RetryDescriptionCommand command = new RetryDescriptionCommand(
                    storeId, sourceAiLogId, requestedBy, UserRole.OWNER, null
            );

            // when & then
            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.STORE_ACCESS_DENIED));
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
            AiLogEntity sourceLog = mockSourceLog(AiRequestType.PRODUCT_DESCRIPTION,
                    "프롬프트", "FRIENDLY", null, null);
            BusinessException geminiException = new BusinessException(AiErrorCode.EXTERNAL_AI_CALL_FAILED);

            given(aiLogRepository.findById(sourceAiLogId)).willReturn(Optional.of(sourceLog));
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));
            given(aiPromptPolicy.buildFinalPrompt(any(), any(), any(), any())).willReturn("최종프롬프트");
            given(geminiClient.generate(any())).willThrow(geminiException);
            given(aiLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            RetryDescriptionCommand command = new RetryDescriptionCommand(
                    storeId, sourceAiLogId, requestedBy, UserRole.OWNER, null
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
        lenient().when(mockUser.getUsername()).thenReturn(ownerUsername);

        Store mockStore = mock(Store.class);
        lenient().when(mockStore.getUser()).thenReturn(mockUser);

        return mockStore;
    }

    private AiLogEntity mockSourceLog(AiRequestType requestType, String inputPrompt,
                                      String tone, String keywordsSnapshot, UUID productIdValue) {
        AiLogEntity log = mock(AiLogEntity.class);
        lenient().when(log.getStoreId()).thenReturn(storeId);
        lenient().when(log.getProductId()).thenReturn(productIdValue);
        lenient().when(log.getRequestType()).thenReturn(requestType);
        lenient().when(log.getInputPrompt()).thenReturn(inputPrompt);
        lenient().when(log.getTone()).thenReturn(tone);
        lenient().when(log.getKeywordsSnapshot()).thenReturn(keywordsSnapshot);
        return log;
    }

    private Product mockProduct(String productName, boolean isDeleted) {
        SoftDeleteAudit softDeleteAudit = mock(SoftDeleteAudit.class);
        given(softDeleteAudit.isDeleted()).willReturn(isDeleted);

        Product mockProduct = mock(Product.class);
        given(mockProduct.getSoftDeleteAudit()).willReturn(softDeleteAudit);
        if (!isDeleted) {
            given(mockProduct.getName()).willReturn(productName);
        }

        return mockProduct;
    }
}
