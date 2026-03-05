package com.dfdt.delivery.domain.store.application.service;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.region.domain.entity.Region;
import com.dfdt.delivery.domain.store.application.fixture.RegionFixture;
import com.dfdt.delivery.domain.store.application.fixture.StoreFixture;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.enums.StoreErrorCode;
import com.dfdt.delivery.domain.store.domain.enums.StoreStatus;
import com.dfdt.delivery.domain.store.domain.repository.JpaStoreRepository;
import com.dfdt.delivery.domain.store.domain.repository.StoreCustomRepository;
import com.dfdt.delivery.domain.store.domain.repository.StoreRatingRepository;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreAdminResDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreResDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreStatusRequestResDto;
import com.dfdt.delivery.domain.user.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreQueryService 테스트")
class StoreQueryServiceImplTest {

    @InjectMocks
    private StoreQueryServiceImpl storeService;

    @Mock
    private JpaStoreRepository storeRepository;

    @Mock
    private StoreCustomRepository storeCustomRepository;

    @Mock
    private StoreRatingRepository storeRatingRepository;

    @Mock
    private User user;

    @Nested
    @DisplayName("getStore - 가게 단일 조회")
    class GetStoreTest {
        @Test
        @DisplayName("성공: 가게 조회 성공")
        void success() {
            // given
            Region region = RegionFixture.createOrderEnabledRegion();
            Store store = StoreFixture.createStore(user, region);
            UUID storeId = store.getStoreId();

            // 실제 구현체(StoreQueryServiceImpl)에서 findStoreById는 storeRepository.findById를 사용함
            when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
            when(storeRatingRepository.findById(storeId)).thenReturn(Optional.empty());

            // when
            StoreResDto result = storeService.getStore(storeId);

            // then
            assertNotNull(result);
            verify(storeRepository).findById(storeId);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 가게")
        void notFound() {
            // given
            Region region = RegionFixture.createOrderEnabledRegion();
            Store store = StoreFixture.createStore(user, region);
            UUID storeId = store.getStoreId();

            when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> storeService.getStore(storeId));
            assertEquals(StoreErrorCode.NOT_FOUND_STORE, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("getStores - 가게 목록 검색")
    class GetStoresTest {
        @Test
        @DisplayName("성공: 페이징된 가게 목록 반환")
        void success() {
            // given
            Page<StoreResDto> page = new PageImpl<>(List.of(mock(StoreResDto.class)));
            when(storeCustomRepository.searchStores(any(Pageable.class), any(), any())).thenReturn(page);

            // when
            Page<StoreResDto> result = storeService.getStores(0, 10, "createdAt", true, UUID.randomUUID(), "한식");

            // then
            assertFalse(result.isEmpty());
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("실패: 검색 결과가 0건이면 NOT_FOUND_STORES 예외가 발생")
        void failIfEmpty() {
            // given
            when(storeCustomRepository.searchStores(any(Pageable.class), any(), any())).thenReturn(Page.empty());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> storeService.getStores(0, 10, "createdAt", true, null, null));
            assertEquals(StoreErrorCode.NOT_FOUND_STORES, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("getStoresAdmin - 가게 목록 검색(관리자용)")
    class GetStoresAdminTest {
        @Test
        @DisplayName("성공: 삭제된 가게를 포함한 목록")
        void success() {
            // given
            Page<StoreAdminResDto> page = new PageImpl<>(List.of(mock(StoreAdminResDto.class)));
            when(storeCustomRepository.searchStoresAdmin(any(Pageable.class), any(), any(), any())).thenReturn(page);

            // when
            Page<StoreAdminResDto> result = storeService.getStoresAdmin(0, 10, "createdAt", false, null, null, true);

            // then
            assertThat(result.getContent()).hasSize(1);
            verify(storeCustomRepository).searchStoresAdmin(any(Pageable.class), any(), any(), eq(true));
        }

        @Test
        @DisplayName("실패: 검색 결과가 0건이면 NOT_FOUND_STORES 예외가 발생")
        void failIfEmpty() {
            // given
            when(storeCustomRepository.searchStoresAdmin(any(Pageable.class), any(), any(), any())).thenReturn(Page.empty());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> storeService.getStoresAdmin(0, 10, "createdAt", true, null, null, true));
            assertEquals(StoreErrorCode.NOT_FOUND_STORES, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("getRequestedStores - 승인 대기 가게 목록 조회")
    class GetRequestedStoresTest {
        @Test
        @DisplayName("성공: REQUESTED 상태인 가게들만 필터링하여 반환")
        void success() {
            // given
            Page<StoreStatusRequestResDto> page = new PageImpl<>(List.of(mock(StoreStatusRequestResDto.class)));
            when(storeCustomRepository.searchRequestStores(any(Pageable.class), eq(StoreStatus.REQUESTED))).thenReturn(page);

            // when
            Page<StoreStatusRequestResDto> result = storeService.getRequestedStores(0, 10, "createdAt", true);

            // then
            assertThat(result.getContent()).hasSize(1);
            verify(storeCustomRepository).searchRequestStores(any(Pageable.class), eq(StoreStatus.REQUESTED));
        }
    }
}