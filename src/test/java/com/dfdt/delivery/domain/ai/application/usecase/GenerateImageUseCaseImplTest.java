package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.application.dto.GenerateImageCommand;
import com.dfdt.delivery.domain.ai.application.dto.GenerateImageResult;
import com.dfdt.delivery.domain.ai.domain.client.GeneratedImageData;
import com.dfdt.delivery.domain.ai.domain.client.ImageGenerationClient;
import com.dfdt.delivery.domain.ai.domain.entity.AiLogEntity;
import com.dfdt.delivery.domain.ai.domain.entity.enums.AspectRatio;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateImageUseCaseImpl 테스트")
class GenerateImageUseCaseImplTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private ProductForAiPort productForAiPort;

    @Mock
    private AiLogRepository aiLogRepository;

    @Mock
    private ImageGenerationClient imageGenerationClient;

    @InjectMocks
    private GenerateImageUseCaseImpl sut;

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
        @DisplayName("productName만으로 이미지 생성 성공")
        void shouldSucceedWithProductNameOnly() {
            // given
            Store mockStore = mockStore(requestedBy);
            GeneratedImageData imageData = new GeneratedImageData("base64abc==", "image/png");

            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));
            given(imageGenerationClient.generate(any(), any())).willReturn(imageData);
            given(aiLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            GenerateImageCommand command = new GenerateImageCommand(
                    storeId, requestedBy, UserRole.OWNER,
                    null, "황금 바삭치킨",
                    "따뜻한 식당 배경으로 맛있게", null, null, false, null
            );

            // when
            GenerateImageResult result = sut.execute(command);

            // then
            assertThat(result.imageData()).isEqualTo("base64abc==");
            assertThat(result.mimeType()).isEqualTo("image/png");
            then(aiLogRepository).should().save(any(AiLogEntity.class));
        }

        @Test
        @DisplayName("includeText=true이면 text가 프롬프트에 포함된다")
        void shouldIncludeTextInPromptWhenRequested() {
            // given
            Store mockStore = mockStore(requestedBy);
            GeneratedImageData imageData = new GeneratedImageData("imagedata==", "image/png");

            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));
            given(imageGenerationClient.generate(contains("치킨"), any())).willReturn(imageData);
            given(aiLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            GenerateImageCommand command = new GenerateImageCommand(
                    storeId, requestedBy, UserRole.OWNER,
                    null, "치킨",
                    "배경은 노란색", AspectRatio.LANDSCAPE, "수채화", true, "치킨"
            );

            // when & then
            assertThatCode(() -> sut.execute(command)).doesNotThrowAnyException();
            then(imageGenerationClient).should().generate(contains("치킨"), any());
        }

        @Test
        @DisplayName("productId가 있으면 product.getName()을 프롬프트에 사용한다")
        void shouldUseProductNameFromProductEntity() {
            // given
            Store mockStore = mockStore(requestedBy);
            GeneratedImageData imageData = new GeneratedImageData("imgdata==", "image/jpeg");

            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));
            given(productForAiPort.findActive(productId, storeId)).willReturn(Optional.of(new ProductInfo(productId, "황금 바삭치킨")));
            given(imageGenerationClient.generate(contains("황금 바삭치킨"), any())).willReturn(imageData);
            given(aiLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            GenerateImageCommand command = new GenerateImageCommand(
                    storeId, requestedBy, UserRole.OWNER,
                    productId, null,
                    "바삭하게 보이도록", AspectRatio.SQUARE, null, false, null
            );

            // when
            sut.execute(command);

            // then: 상품명이 프롬프트에 포함되어 API 호출됨
            then(imageGenerationClient).should().generate(contains("황금 바삭치킨"), any());
        }

        @Test
        @DisplayName("MASTER 역할은 소유권 체크 없이 통과한다")
        void masterRoleShouldSkipOwnershipCheck() {
            // given
            Store mockStore = mockStore("otherOwner");
            GeneratedImageData imageData = new GeneratedImageData("data==", "image/png");

            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));
            given(imageGenerationClient.generate(any(), any())).willReturn(imageData);
            given(aiLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            GenerateImageCommand command = new GenerateImageCommand(
                    storeId, "masterUser", UserRole.MASTER,
                    null, "치킨",
                    "맛있게", null, null, false, null
            );

            // when & then
            assertThatCode(() -> sut.execute(command)).doesNotThrowAnyException();
        }
    }

    // ──────────────────────────────────────────────────
    // 입력 검증 예외
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("입력 검증 예외")
    class ValidationExceptionTests {

        @Test
        @DisplayName("productId와 productName 모두 없으면 PRODUCT_IDENTIFIER_REQUIRED")
        void shouldThrowWhenBothProductIdentifiersMissing() {
            // given
            Store mockStore = mockStore(requestedBy);
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));

            GenerateImageCommand command = new GenerateImageCommand(
                    storeId, requestedBy, UserRole.OWNER,
                    null, null, "맛있게", null, null, false, null  // productId, productName 모두 null
            );

            // when & then
            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.PRODUCT_IDENTIFIER_REQUIRED));
        }

        @Test
        @DisplayName("includeText=true인데 text가 없으면 INCLUDE_TEXT_REQUIRED")
        void shouldThrowWhenTextMissingWithIncludeTextTrue() {
            // given
            Store mockStore = mockStore(requestedBy);
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));

            GenerateImageCommand command = new GenerateImageCommand(
                    storeId, requestedBy, UserRole.OWNER,
                    null, "치킨",
                    "맛있게", null, null, true, null  // includeText=true but text=null
            );

            // when & then
            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.INCLUDE_TEXT_REQUIRED));
        }
    }

    // ──────────────────────────────────────────────────
    // Store / 소유권 예외
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("Store 관련 예외")
    class StoreExceptionTests {

        @Test
        @DisplayName("존재하지 않는 storeId → STORE_NOT_FOUND")
        void shouldThrowWhenStoreNotFound() {
            // given
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.empty());

            GenerateImageCommand command = new GenerateImageCommand(
                    storeId, requestedBy, UserRole.OWNER,
                    null, "치킨", "맛있게", null, null, false, null
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
            Store mockStore = mockStore("anotherOwner");
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));

            GenerateImageCommand command = new GenerateImageCommand(
                    storeId, requestedBy, UserRole.OWNER,
                    null, "치킨", "맛있게", null, null, false, null
            );

            // when & then
            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.STORE_ACCESS_DENIED));
        }
    }

    // ──────────────────────────────────────────────────
    // 이미지 생성 API 실패
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("ImageGenerationClient 실패 처리")
    class ImageClientFailureTests {

        @Test
        @DisplayName("이미지 생성 실패 시 실패 로그를 저장하고 예외를 다시 던진다")
        void shouldSaveFailureLogAndRethrowOnClientFailure() {
            // given
            Store mockStore = mockStore(requestedBy);
            BusinessException clientException = new BusinessException(AiErrorCode.EXTERNAL_AI_CALL_FAILED);

            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(mockStore));
            given(imageGenerationClient.generate(any(), any())).willThrow(clientException);
            given(aiLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            GenerateImageCommand command = new GenerateImageCommand(
                    storeId, requestedBy, UserRole.OWNER,
                    null, "치킨", "맛있게", null, null, false, null
            );

            // when & then
            assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.EXTERNAL_AI_CALL_FAILED));

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

}

