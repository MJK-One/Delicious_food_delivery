package com.dfdt.delivery.domain.category.application.service;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.category.application.fixture.CategoryFixture;
import com.dfdt.delivery.domain.category.application.fixture.UserFixture;
import com.dfdt.delivery.domain.category.domain.entity.Category;
import com.dfdt.delivery.domain.category.domain.enums.CategoryErrorCode;
import com.dfdt.delivery.domain.category.domain.repository.JpaCategoryRepository;
import com.dfdt.delivery.domain.category.presentation.dto.request.CategoryCreateReqDto;
import com.dfdt.delivery.domain.category.presentation.dto.request.CategoryUpdateReqDto;
import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryResDto;
import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryUpdateResDto;
import com.dfdt.delivery.domain.store.domain.repository.JpaStoreRepository;
import com.dfdt.delivery.domain.user.domain.entity.User;
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

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreCommandService 테스트")
public class CategoryCommandServiceImplTest {

    @InjectMocks
    private CategoryCommandServiceImpl categoryService;

    @Mock
    private JpaCategoryRepository categoryRepository;

    @Mock
    private JpaStoreRepository storeRepository;

    @Mock
    private User user;

    @Mock
    private CustomUserDetails userDetails;

    @Mock
    private Category category;

    @BeforeEach
    void setUp() {
        user = UserFixture.createUser();
        category = CategoryFixture.createCategory();
    }

    @Nested
    @DisplayName("카테고리 생성")
    class CreateCategoryTest {

        @Test
        @DisplayName("성공: 중복이 아니면 maxSortOrder+1로 생성 후 저장하고 DTO를 반환")
        void success() {
            // given
            CategoryCreateReqDto request = new CategoryCreateReqDto();
            request.setName("한식");

            when(userDetails.getUsername()).thenReturn(user.getUsername());
            when(categoryRepository.existsByNameAndDeletedAtIsNull("한식")).thenReturn(false);
            when(categoryRepository.findMaxSortOrder()).thenReturn(Optional.of(0));
            when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            CategoryResDto result = categoryService.createCategory(request, userDetails);

            // then
            assertNotNull(result);
            assertEquals(1, result.getSortOrder());
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("실패: 카테고리명이 이미 존재하면 ALREADY_EXIST 예외가 발생")
        void fail_alreadyExist() {
            // given
            CategoryCreateReqDto request = new CategoryCreateReqDto();
            request.setName("한식");

            when(categoryRepository.existsByNameAndDeletedAtIsNull("한식")).thenReturn(true);

            // when & then
            BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.createCategory(request, userDetails));

            assertEquals(CategoryErrorCode.ALREADY_EXIST, ex.getErrorCode());
            verify(categoryRepository, never()).findMaxSortOrder();
            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    @Nested
    @DisplayName("카테고리 수정")
    class UpdateCategoryTest {
        @Test
        @DisplayName("성공: sortOrder를 아래(0 -> 1)로 이동하면 shiftSortOrdersDown이 호출")
        void successOrdersDown() {
            // given
            UUID categoryId = category.getCategoryId();
            CategoryUpdateReqDto request = new CategoryUpdateReqDto();
            request.setName("테스트 카테고리2");
            request.setSortOrder(1);
            request.setDescription("카테고리 설명2");

            when(categoryRepository.findActiveCategoryById(categoryId)).thenReturn(Optional.of(category));
            when(categoryRepository.existsByNameAndCategoryIdNot(anyString(), eq(categoryId))).thenReturn(false);
            when(userDetails.getUsername()).thenReturn(user.getUsername());

            // when
            CategoryUpdateResDto result = categoryService.updateCategory(categoryId, request, userDetails);

            // then
            assertNotNull(result);
            assertEquals(1, result.getSortOrder());
            verify(categoryRepository, never()).shiftSortOrdersUp(anyInt(), anyInt());
            verify(categoryRepository, times(1)).shiftSortOrdersDown(anyInt(), anyInt());
        }

        @Test
        @DisplayName("성공: sortOrder를 위(1 -> 0)로 이동하면 shiftSortOrdersUp이 호출")
        void successOrdersUp() {
            // given
            Category category = Category.builder()
                    .categoryId(UUID.randomUUID())
                    .name("테스트 카테고리")
                    .description("카테고리 설명")
                    .sortOrder(1)
                    .isActive(true)
                    .createAudit(CreateAudit.now("테스트 유저"))
                    .build();;
            UUID categoryId = category.getCategoryId();

            CategoryUpdateReqDto request = new CategoryUpdateReqDto();
            request.setName("테스트 카테고리2");
            request.setSortOrder(0);
            request.setDescription("카테고리 설명2");

            when(categoryRepository.findActiveCategoryById(categoryId)).thenReturn(Optional.of(category));
            when(categoryRepository.existsByNameAndCategoryIdNot(anyString(), eq(categoryId))).thenReturn(false);
            when(userDetails.getUsername()).thenReturn(user.getUsername());

            // when
            CategoryUpdateResDto result = categoryService.updateCategory(categoryId, request, userDetails);

            // then
            assertNotNull(result);
            assertEquals(0, result.getSortOrder());
            verify(categoryRepository, never()).shiftSortOrdersDown(anyInt(), anyInt());
            verify(categoryRepository, times(1)).shiftSortOrdersUp(anyInt(), anyInt());
        }

        @Test
        @DisplayName("실패: 수정 시 중복 카테고리명이면 ALREADY_EXIST 예외 발생")
        void failAlreadyExistName() {
            // given
            UUID categoryId = category.getCategoryId();
            Category mockCategory = mock(Category.class);
            CategoryUpdateReqDto request = mock(CategoryUpdateReqDto.class);

            when(categoryRepository.findActiveCategoryById(categoryId)).thenReturn(Optional.of(mockCategory));
            when(request.getName()).thenReturn("중복");
            when(categoryRepository.existsByNameAndCategoryIdNot("중복", categoryId)).thenReturn(true);

            // when & then
            BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.updateCategory(categoryId, request, userDetails));

            assertEquals(CategoryErrorCode.ALREADY_EXIST, ex.getErrorCode());
            verify(mockCategory, never()).update(any(), anyString());
        }

        @Test
        @DisplayName("실패: 삭제된 카테고리는 수정할 수 없다(NOT_MODIFIED)")
        void failDeletedCategoryNotModified() {
            // given
            UUID categoryId = category.getCategoryId();
            Category category = mock(Category.class);
            CategoryUpdateReqDto request = mock(CategoryUpdateReqDto.class);

            when(categoryRepository.findActiveCategoryById(categoryId)).thenReturn(Optional.of(category));
            when(request.getName()).thenReturn("한식");
            when(categoryRepository.existsByNameAndCategoryIdNot("한식", categoryId)).thenReturn(false);

            when(category.getSoftDeleteAudit()).thenReturn(mock(SoftDeleteAudit.class));

            // when & then
            BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.updateCategory(categoryId, request, userDetails));

            assertEquals(CategoryErrorCode.NOT_MODIFIED, ex.getErrorCode());
            verify(category, never()).update(any(), anyString());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 카테고리면 NOT_FOUND_CATEGORY 예외 발생")
        void fail_notFound() {
            // given
            UUID categoryId = category.getCategoryId();
            CategoryUpdateReqDto request = mock(CategoryUpdateReqDto.class);

            when(categoryRepository.findActiveCategoryById(categoryId)).thenReturn(Optional.empty());

            // when & then
            BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.updateCategory(categoryId, request, userDetails));

            assertEquals(CategoryErrorCode.NOT_FOUND_CATEGORY, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("카테고리 삭제")
    class DeleteCategoryTest {
        @Test
        @DisplayName("성공: 사용 중인 가게가 없으면 카테고리를 삭제하고 이후 sortOrder를 감소")
        void success() {
            // given
            UUID categoryId = category.getCategoryId();

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(storeRepository.existsByCategoryIdAndNotDeleted(categoryId)).thenReturn(false);
            when(userDetails.getUsername()).thenReturn(user.getUsername());

            // when
            categoryService.deleteCategory(categoryId, userDetails);

            // then
            verify(categoryRepository, times(1)).decrementSortOrdersAfter(category.getSortOrder());
        }

        @Test
        @DisplayName("실패: 이미 삭제된 카테고리이면 예외가 발생")
        void failAlreadyDeleted() {
            // given
            UUID categoryId = category.getCategoryId();

            category.delete(userDetails.getUsername());

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> categoryService.deleteCategory(categoryId, userDetails));

            assertEquals(CategoryErrorCode.ALREADY_DELETED, exception.getErrorCode());
            verify(storeRepository, never()).existsByCategoryIdAndNotDeleted(any());
            verify(categoryRepository, never()).decrementSortOrdersAfter(anyInt());
        }

        @Test
        @DisplayName("실패: 해당 카테고리를 사용 중인 가게가 있으면 예외가 발생")
        void failCategoryBeUsed() {
            // given
            UUID categoryId = category.getCategoryId();

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(storeRepository.existsByCategoryIdAndNotDeleted(categoryId)).thenReturn(true);

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> categoryService.deleteCategory(categoryId, userDetails));

            assertEquals(CategoryErrorCode.CATEGORY_BE_USED, exception.getErrorCode());
            verify(categoryRepository, never()).decrementSortOrdersAfter(anyInt());
        }
    }

    @Nested
    @DisplayName("카테고리 복구")
    class RestoreCategoryTest {
        @Test
        @DisplayName("성공: 삭제된 카테고리를 마지막 sortOrder + 1로 복구")
        void success() {
            // given
            UUID categoryId = category.getCategoryId();

            category.delete(userDetails.getUsername());

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(categoryRepository.existsByNameAndDeletedAtIsNull(category.getName())).thenReturn(false);
            when(categoryRepository.findMaxSortOrder()).thenReturn(Optional.of(0));
            when(userDetails.getUsername()).thenReturn(user.getUsername());

            // when
            categoryService.restoreCategory(categoryId, userDetails);

            // then
            assertEquals(1, category.getSortOrder());
            verify(categoryRepository, times(1)).findMaxSortOrder();
        }

        @Test
        @DisplayName("실패: 삭제되지 않은 카테고리이면 예외가 발생")
        void failNotDeleted() {
            // given
            UUID categoryId = category.getCategoryId();

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> categoryService.restoreCategory(categoryId, userDetails));

            assertEquals(CategoryErrorCode.NOT_DELETED, exception.getErrorCode());
            verify(categoryRepository, never()).findMaxSortOrder();
        }

        @Test
        @DisplayName("실패: 복구하려는 카테고리 이름이 이미 존재하면 예외가 발생")
        void failDuplicatedName() {
            // given
            UUID categoryId = category.getCategoryId();

            category.delete(userDetails.getUsername());

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(categoryRepository.existsByNameAndDeletedAtIsNull(category.getName())).thenReturn(true);

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> categoryService.restoreCategory(categoryId, userDetails));

            assertEquals(CategoryErrorCode.ALREADY_EXIST, exception.getErrorCode());
            verify(categoryRepository, never()).findMaxSortOrder();
        }
    }
}