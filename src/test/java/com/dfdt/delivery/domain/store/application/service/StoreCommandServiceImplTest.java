package com.dfdt.delivery.domain.store.application.service;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.category.domain.entity.Category;
import com.dfdt.delivery.domain.category.domain.enums.CategoryErrorCode;
import com.dfdt.delivery.domain.category.domain.repository.CategoryRepository;
import com.dfdt.delivery.domain.category.domain.repository.JpaCategoryRepository;
import com.dfdt.delivery.domain.category.presentation.dto.request.CategoryCreateReqDto;
import com.dfdt.delivery.domain.region.domain.entity.Region;
import com.dfdt.delivery.domain.region.domain.enums.RegionErrorCode;
import com.dfdt.delivery.domain.region.domain.repository.RegionRepository;
import com.dfdt.delivery.domain.store.application.fixture.RegionFixture;
import com.dfdt.delivery.domain.store.application.fixture.StoreFixture;
import com.dfdt.delivery.domain.store.application.fixture.UserFixture;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.entity.StoreCategory;
import com.dfdt.delivery.domain.store.domain.enums.StoreErrorCode;
import com.dfdt.delivery.domain.store.domain.enums.StoreStatus;
import com.dfdt.delivery.domain.store.domain.repository.JpaStoreRepository;
import com.dfdt.delivery.domain.store.domain.repository.StoreCategoryRepository;
import com.dfdt.delivery.domain.store.domain.repository.StoreCustomRepository;
import com.dfdt.delivery.domain.store.domain.repository.StoreRatingRepository;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreCreateReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreStatusReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreUpdateReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.*;
import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import com.dfdt.delivery.domain.user.domain.exception.error.enums.UserErrorCode;
import com.dfdt.delivery.domain.user.domain.repository.UserRepository;
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
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreCommandService 테스트")
class StoreCommandServiceImplTest {

    @InjectMocks
    private StoreCommandServiceImpl storeService;

    @Mock
    private JpaStoreRepository storeRepository;

    @Mock
    private StoreCategoryRepository storeCategoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private JpaCategoryRepository categoryRepository;

    @Mock
    private CustomUserDetails userDetails;

    @Mock
    private User user;
    @Mock
    private Region region;
    @Mock
    private Store store;

    @BeforeEach
    void setUp() {
        region = RegionFixture.createOrderEnabledRegion();
        user = UserFixture.createUser();
        store = StoreFixture.createStore(user, region);
    }

    @Nested
    @DisplayName("createStore - 가게 생성")
    class CreateStoreTest {
        @Test
        @DisplayName("성공: 가게 생성")
        void success() {
            // given
            StoreCreateReqDto requestDto = new StoreCreateReqDto(
                    region.getRegionId(), "분식집", "010-1234-5678", "가게 설명", "서울시 강남구...", List.of(UUID.randomUUID()));

            Category mockCategory = mock(Category.class);

            when(categoryRepository.findAllById(requestDto.getCategoryIds())).thenReturn(List.of(mockCategory));
            when(userDetails.getUsername()).thenReturn(user.getUsername());
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            when(regionRepository.findById(region.getRegionId())).thenReturn(Optional.of(region));
            when(storeRepository.save(any(Store.class))).thenReturn(store);

            // when
            StoreCreateResDto result = storeService.createStore(requestDto, userDetails);

            // then
            assertNotNull(result);
            verify(storeRepository, times(1)).save(any(Store.class));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자 정보로 요청 시 예외 발생")
        void failUserNotFound() {
            // given
            StoreCreateReqDto requestDto = new StoreCreateReqDto(
                    region.getRegionId(), "분식집", "010-1234-5678", "가게 설명", "서울시 강남구...", List.of(UUID.randomUUID()));

            Category mockCategory = mock(Category.class);

            when(categoryRepository.findAllById(requestDto.getCategoryIds())).thenReturn(List.of(mockCategory));
            when(userDetails.getUsername()).thenReturn("unknown_user");
            when(userRepository.findByUsername("unknown_user")).thenReturn(Optional.empty());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> storeService.createStore(requestDto, userDetails));

            assertEquals(UserErrorCode.USER_NOT_FOUND, exception.getErrorCode());
            verify(storeRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 지역 ID로 요청 시 예외 발생")
        void failRegionNotFound() {
            // given
            StoreCreateReqDto requestDto = new StoreCreateReqDto(
                    region.getRegionId(), "분식집", "010-1234-5678", "가게 설명", "서울시 강남구...", List.of(UUID.randomUUID()));

            Category mockCategory = mock(Category.class);

            when(categoryRepository.findAllById(requestDto.getCategoryIds())).thenReturn(List.of(mockCategory));
            when(userDetails.getUsername()).thenReturn(user.getUsername());
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            when(regionRepository.findById(requestDto.getRegionId())).thenReturn(Optional.empty());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> storeService.createStore(requestDto, userDetails));

            assertEquals(RegionErrorCode.NOT_FOUND_REGION, exception.getErrorCode());
            verify(storeRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 요청한 카테고리 중 일부가 존재하지 않을 때 예외 발생")
        void failCategoryMismatch() {
            // given
            List<UUID> categoryIds = List.of(UUID.randomUUID(), UUID.randomUUID());
            StoreCreateReqDto requestDto = new StoreCreateReqDto(region.getRegionId(), "분식집", "010-1234-5678", "설명", "주소", categoryIds);

            Category mockCategory = mock(Category.class);
            when(categoryRepository.findAllById(categoryIds)).thenReturn(List.of(mockCategory));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> {storeService.createStore(requestDto, userDetails);});

            assertEquals(CategoryErrorCode.NOT_FOUND_CATEGORY, exception.getErrorCode());
            verify(storeRepository, never()).save(any(Store.class));
        }
    }

    @Nested
    @DisplayName("updateStore - 가게 수정")
    class UpdateStoreTest {
        @Test
        @DisplayName("성공: 본인의 가게이고 존재하는 경우 수정에 성공한다")
        void success() {
            // given
            UUID storeId = store.getStoreId();
            List<UUID> categoryIds = List.of(UUID.randomUUID());
            StoreUpdateReqDto updateDto = new StoreUpdateReqDto(
                    "수정된 가게이름",
                    "02-123-4567",
                    "가게 설명 수정",
                    "수정 가게 주소",
                    false,
                    categoryIds
            );

            Category category = mock(Category.class);
            StoreCategory storeCategory = mock(StoreCategory.class);

            when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
            when(userDetails.getUsername()).thenReturn(user.getUsername());
            when(categoryRepository.findAllById(categoryIds)).thenReturn(List.of(category));
            when(storeCategoryRepository.findByStore(store)).thenReturn(List.of(storeCategory));
            when(storeCategory.getCategory()).thenReturn(category);

            // when
            storeService.updateStore(storeId, updateDto, userDetails);

            // then
            assertEquals("수정된 가게이름", store.getName());
            assertEquals("02-123-4567", store.getPhone());
            assertEquals("가게 설명 수정", store.getDescription());
            assertEquals("수정 가게 주소", store.getAddressText());
            assertFalse(store.getIsOpen());
        }

        @Test
        @DisplayName("실패: 내 소유의 가게가 아닐 경우 예외 발생")
        void failNotMyStore() {
            // given
            UUID storeId = store.getStoreId();
            List<UUID> categoryIds = List.of(UUID.randomUUID());
            StoreUpdateReqDto updateDto = new StoreUpdateReqDto(
                    "수정된 가게이름",
                    "02-123-4567",
                    "가게 설명 수정",
                    "수정 가게 주소",
                    false,
                    categoryIds
            );

            Category category = mock(Category.class);
            CustomUserDetails mockUserDetails = mock(CustomUserDetails.class);

            when(categoryRepository.findAllById(categoryIds)).thenReturn(List.of(category));
            when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
            when(mockUserDetails.getUsername()).thenReturn("tester2");
            when(mockUserDetails.getRole()).thenReturn(UserRole.OWNER);

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> {storeService.updateStore(storeId, updateDto, mockUserDetails);});

            assertEquals(StoreErrorCode.NOT_MY_STORE, exception.getErrorCode());
        }

        @Test
        @DisplayName("실패: 이미 삭제 처리된 가게는 수정할 수 없다")
        void failAlreadyDeleted() {
            // given
            UUID storeId = store.getStoreId();
            List<UUID> categoryIds = List.of(UUID.randomUUID());
            StoreUpdateReqDto updateDto = new StoreUpdateReqDto(
                    "수정된 가게이름",
                    "02-123-4567",
                    "가게 설명 수정",
                    "수정 가게 주소",
                    false,
                    categoryIds
            );

            store.delete(user.getUsername());

            assertNotNull(store.getSoftDeleteAudit());
            assertTrue(store.getSoftDeleteAudit().isDeleted());

            when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
            when(userDetails.getUsername()).thenReturn(user.getUsername());
            when(categoryRepository.findAllById(anyList())).thenReturn(List.of(mock(Category.class)));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> storeService.updateStore(storeId, updateDto, userDetails));

            assertEquals(StoreErrorCode.NOT_MODIFIED, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("deleteStore - 가게 삭제")
    class DeleteStoreTest {
        @Test
        @DisplayName("성공: 본인 소유의 삭제되지 않은 가게를 삭제 처리한다")
        void success() {
            // given
            UUID storeId = store.getStoreId();

            when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
            when(userDetails.getUsername()).thenReturn(user.getUsername());

            assertNull(store.getSoftDeleteAudit());

            // when
            storeService.deleteStore(storeId, userDetails);

            // then
            assertNotNull(store.getSoftDeleteAudit());
            assertTrue(store.getSoftDeleteAudit().isDeleted());
            assertNotNull(store.getSoftDeleteAudit().getDeletedAt());
            assertEquals(user.getUsername(), store.getSoftDeleteAudit().getDeletedBy());
        }

        @Test
        @DisplayName("실패: 내 소유의 가게가 아닐 경우 예외 발생")
        void failNotMyStore() {
            // given
            UUID storeId = store.getStoreId();

            when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
            when(userDetails.getUsername()).thenReturn("tester2");
            when(userDetails.getRole()).thenReturn(UserRole.OWNER);

            assertNull(store.getSoftDeleteAudit());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> storeService.deleteStore(storeId, userDetails));

            assertEquals(StoreErrorCode.NOT_MY_STORE, exception.getErrorCode());
        }

        @Test
        @DisplayName("실패: 이미 삭제된 가게를 다시 삭제 요청할 경우 예외 발생")
        void failAlreadyDeleted() {
            // given
            UUID storeId = store.getStoreId();

            store.delete(user.getUsername());

            when(userDetails.getUsername()).thenReturn(user.getUsername());
            when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> storeService.deleteStore(storeId, userDetails));

            assertEquals(StoreErrorCode.ALREADY_DELETED, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("changeIsOpen - 영업 상태 변경")
    class ChangeIsOpenTest {
        @Test
        @DisplayName("성공: 본인 가게이고 삭제되지 않은 경우 영업 상태가 변경된다")
        void success() {
            // given
            UUID storeId = store.getStoreId();

            when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
            when(userDetails.getUsername()).thenReturn(user.getUsername());

            assertNull(store.getSoftDeleteAudit());

            boolean before = store.getIsOpen();

            // when
            storeService.changeIsOpen(storeId, userDetails);

            // then
            assertNotEquals(before, store.getIsOpen());
            assertEquals(user.getUsername(), store.getUpdateAudit().getUpdatedBy());
        }

        @Test
        @DisplayName("실패: 내 가게가 아니면 예외가 발생한다")
        void failNotMyStore() {
            // given
            UUID storeId = store.getStoreId();

            when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
            when(userDetails.getUsername()).thenReturn("tester2");
            when(userDetails.getRole()).thenReturn(UserRole.OWNER);

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> storeService.changeIsOpen(storeId, userDetails));

            assertEquals(StoreErrorCode.NOT_MY_STORE, exception.getErrorCode());
        }

        @Test
        @DisplayName("실패: 삭제된 가게는 영업 상태를 변경할 수 없다")
        void failDeletedStore() {
            // given
            UUID storeId = store.getStoreId();

            when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
            when(userDetails.getUsername()).thenReturn(user.getUsername());

            store.delete(user.getUsername());

            assertNotNull(store.getSoftDeleteAudit());
            assertTrue(store.getSoftDeleteAudit().isDeleted());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> storeService.changeIsOpen(storeId, userDetails));

            assertEquals(StoreErrorCode.NOT_MODIFIED, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("restoreStore - 가게 복구")
    class RestoreStoreTest {

        @Test
        @DisplayName("성공: SUSPENDED 상태인 가게는 복구된다")
        void success() {
            // given
            UUID storeId = store.getStoreId();

            when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
            when(userDetails.getUsername()).thenReturn(user.getUsername());

            // when
            store.delete(user.getUsername());
            storeService.restoreStore(storeId, userDetails);

            // then
            assertThat(store.getStatus()).isEqualTo(StoreStatus.APPROVED);
            assertNotNull(store);
        }

        @Test
        @DisplayName("실패: SUSPENDED가 아니면 NOT_SUSPENDED 예외 발생")
        void failNotSuspended() {
            // given
            UUID storeId = store.getStoreId();

            when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> storeService.restoreStore(storeId, userDetails));

            assertEquals(StoreErrorCode.NOT_SUSPENDED, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("changeStatus - 가게 상태 변경")
    class ChangeStatusTest {
        @Test
        @DisplayName("성공: 상태 변경 후 StoreStatusResDto를 반환한다")
        void success() {
            // given
            UUID storeId = store.getStoreId();
            StoreStatusReqDto req = mock(StoreStatusReqDto.class);
            StoreStatusResDto resDto = mock(StoreStatusResDto.class);

            when(req.getStatus()).thenReturn(StoreStatus.SUSPENDED.name());
            when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
            when(userDetails.getUsername()).thenReturn(user.getUsername());

            try (org.mockito.MockedStatic<StoreStatusResDto> mocked = mockStatic(StoreStatusResDto.class)) {
                mocked.when(() -> StoreStatusResDto.from(store)).thenReturn(resDto);

                // when
                StoreStatusResDto result = storeService.changeStatus(storeId, req, userDetails);

                // then
                assertNotNull(result);
                assertSame(resDto, result);
                assertEquals(StoreStatus.SUSPENDED, store.getStatus());
            }
        }
    }
}