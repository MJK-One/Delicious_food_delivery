package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.application.dto.AiLogSummaryResult;
import com.dfdt.delivery.domain.ai.application.dto.SearchProductAiLogsQuery;
import com.dfdt.delivery.domain.ai.domain.entity.enums.AiRequestType;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
import com.dfdt.delivery.domain.ai.domain.repository.AiLogCustomRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchProductAiLogsUseCaseImpl 테스트")
class SearchProductAiLogsUseCaseImplTest {

    @InjectMocks
    private SearchProductAiLogsUseCaseImpl useCase;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AiLogCustomRepository aiLogCustomRepository;

    private UUID storeId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
        productId = UUID.randomUUID();
    }

    // ──────────────────────────────────────────────────
    // 정상 케이스
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("정상 요청")
    class SuccessTests {

        @Test
        @DisplayName("OWNER - 소유권 검증 통과 후 상품 AI 로그 Page 반환")
        void ownerShouldReturnProductAiLogs() {
            // given
            Store store = mockStore("owner123");
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(store));
            given(productRepository.findByProductIdAndStoreId(productId, storeId))
                    .willReturn(Optional.of(mock(Product.class)));
            Page<AiLogSummaryResult> mockPage = mockPage(2);
            given(aiLogCustomRepository.searchAiLogs(any(), any(), any(), any(), any()))
                    .willReturn(mockPage);

            SearchProductAiLogsQuery query = ownerQuery("owner123", null, null);

            // when
            Page<AiLogSummaryResult> result = useCase.execute(query);

            // then
            assertThat(result.getTotalElements()).isEqualTo(2);
            verify(storeRepository).findByStoreIdAndNotDeleted(storeId);
            verify(productRepository).findByProductIdAndStoreId(productId, storeId);
            verify(aiLogCustomRepository).searchAiLogs(eq(storeId), eq(productId), isNull(), isNull(), any());
        }

        @Test
        @DisplayName("MASTER - 소유권 검증 없이 상품 AI 로그 Page 반환")
        void masterShouldSkipOwnershipCheck() {
            // given
            given(productRepository.findByProductIdAndStoreId(productId, storeId))
                    .willReturn(Optional.of(mock(Product.class)));
            given(aiLogCustomRepository.searchAiLogs(any(), any(), any(), any(), any()))
                    .willReturn(mockPage(3));

            SearchProductAiLogsQuery query = new SearchProductAiLogsQuery(
                    storeId, productId, "master", UserRole.MASTER,
                    null, null, 0, 10, "createdAt", false
            );

            // when
            Page<AiLogSummaryResult> result = useCase.execute(query);

            // then
            assertThat(result.getTotalElements()).isEqualTo(3);
            verify(storeRepository, never()).findByStoreIdAndNotDeleted(any());
        }

        @Test
        @DisplayName("OWNER - isApplied=true 필터가 CustomRepository로 전달된다")
        void ownerWithIsAppliedFilterPassedThrough() {
            // given
            Store store = mockStore("owner123");
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(store));
            given(productRepository.findByProductIdAndStoreId(productId, storeId))
                    .willReturn(Optional.of(mock(Product.class)));
            given(aiLogCustomRepository.searchAiLogs(any(), any(), any(), any(), any()))
                    .willReturn(mockPage(1));

            SearchProductAiLogsQuery query = ownerQuery("owner123", true, null);

            // when
            useCase.execute(query);

            // then
            verify(aiLogCustomRepository).searchAiLogs(eq(storeId), eq(productId), eq(true), isNull(), any());
        }
    }

    // ──────────────────────────────────────────────────
    // 소유권 / 가게 예외
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("소유권 / 가게 예외")
    class OwnershipFailureTests {

        @Test
        @DisplayName("OWNER - 가게가 존재하지 않으면 STORE_NOT_FOUND 예외 발생")
        void shouldThrowWhenStoreNotFound() {
            // given
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.empty());

            SearchProductAiLogsQuery query = ownerQuery("owner123", null, null);

            // when & then
            assertThatThrownBy(() -> useCase.execute(query))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.STORE_NOT_FOUND));

            verify(productRepository, never()).findByProductIdAndStoreId(any(), any());
            verify(aiLogCustomRepository, never()).searchAiLogs(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("OWNER - 본인 가게가 아니면 STORE_ACCESS_DENIED 예외 발생")
        void shouldThrowWhenNotOwner() {
            // given
            Store store = mockStore("realOwner");
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(store));

            SearchProductAiLogsQuery query = ownerQuery("intruder", null, null);

            // when & then
            assertThatThrownBy(() -> useCase.execute(query))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.STORE_ACCESS_DENIED));

            verify(productRepository, never()).findByProductIdAndStoreId(any(), any());
        }
    }

    // ──────────────────────────────────────────────────
    // 상품 예외
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("상품 예외")
    class ProductFailureTests {

        @Test
        @DisplayName("OWNER - 상품이 해당 가게에 존재하지 않으면 PRODUCT_NOT_FOUND 예외 발생")
        void shouldThrowWhenProductNotFound() {
            // given
            Store store = mockStore("owner123");
            given(storeRepository.findByStoreIdAndNotDeleted(storeId)).willReturn(Optional.of(store));
            given(productRepository.findByProductIdAndStoreId(productId, storeId))
                    .willReturn(Optional.empty());

            SearchProductAiLogsQuery query = ownerQuery("owner123", null, null);

            // when & then
            assertThatThrownBy(() -> useCase.execute(query))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.PRODUCT_NOT_FOUND));

            verify(aiLogCustomRepository, never()).searchAiLogs(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("MASTER - 상품이 해당 가게에 존재하지 않으면 PRODUCT_NOT_FOUND 예외 발생")
        void masterShouldThrowWhenProductNotFound() {
            // given
            given(productRepository.findByProductIdAndStoreId(productId, storeId))
                    .willReturn(Optional.empty());

            SearchProductAiLogsQuery query = new SearchProductAiLogsQuery(
                    storeId, productId, "master", UserRole.MASTER,
                    null, null, 0, 10, "createdAt", false
            );

            // when & then
            assertThatThrownBy(() -> useCase.execute(query))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(AiErrorCode.PRODUCT_NOT_FOUND));
        }
    }

    // ──────────────────────────────────────────────────
    // 헬퍼 메서드
    // ──────────────────────────────────────────────────

    private SearchProductAiLogsQuery ownerQuery(String username, Boolean isApplied, Boolean isSuccess) {
        return new SearchProductAiLogsQuery(
                storeId, productId, username, UserRole.OWNER,
                isApplied, isSuccess, 0, 10, "createdAt", false
        );
    }

    private Store mockStore(String ownerUsername) {
        User mockUser = mock(User.class);
        given(mockUser.getUsername()).willReturn(ownerUsername);
        Store mockStore = mock(Store.class);
        given(mockStore.getUser()).willReturn(mockUser);
        return mockStore;
    }

    private Page<AiLogSummaryResult> mockPage(int count) {
        List<AiLogSummaryResult> list = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(new AiLogSummaryResult(
                    UUID.randomUUID(), productId, "owner123",
                    AiRequestType.PRODUCT_DESCRIPTION, "FRIENDLY",
                    true, false, null, "테스트 응답", OffsetDateTime.now()
            ));
        }
        return new PageImpl<>(list, PageRequest.of(0, 10), count);
    }
}
