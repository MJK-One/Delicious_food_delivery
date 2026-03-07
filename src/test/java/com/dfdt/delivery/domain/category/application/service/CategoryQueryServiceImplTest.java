package com.dfdt.delivery.domain.category.application.service;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.category.application.fixture.CategoryFixture;
import com.dfdt.delivery.domain.category.domain.entity.Category;
import com.dfdt.delivery.domain.category.domain.enums.CategoryErrorCode;
import com.dfdt.delivery.domain.category.domain.repository.CategoryCustomRepository;
import com.dfdt.delivery.domain.category.domain.repository.JpaCategoryRepository;
import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryAdminResDto;
import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryResDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreQueryService 테스트")
public class CategoryQueryServiceImplTest {

    @InjectMocks
    private CategoryQueryServiceImpl categoryService;

    @Mock
    private JpaCategoryRepository categoryRepository;

    @Mock
    private CategoryCustomRepository categoryCustomRepository;

    @Mock
    private Category category;

    @BeforeEach
    void setUp() {
        category = CategoryFixture.createCategory();
    }

    @Nested
    @DisplayName("카테고리 단일 조회")
    class GetCategoryTest {
        @Test
        @DisplayName("성공: 카테고리 조회 시 DTO로 반환한다")
        void success() {
            // given
            UUID categoryId = UUID.randomUUID();

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

            CategoryResDto resDto = mock(CategoryResDto.class);

            try (MockedStatic<CategoryResDto> mocked = mockStatic(CategoryResDto.class)) {
                mocked.when(() -> CategoryResDto.from(category)).thenReturn(resDto);

                // when
                CategoryResDto result = categoryService.getCategory(categoryId);

                // then
                assertNotNull(result);
                assertSame(resDto, result);

                verify(categoryRepository, times(1)).findById(categoryId);
                mocked.verify(() -> CategoryResDto.from(category), times(1));
            }
        }

        @Test
        @DisplayName("실패: 카테고리가 없으면 NOT_FOUND_CATEGORY 예외가 발생한다")
        void failNotFound() {
            // given
            UUID categoryId = UUID.randomUUID();
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

            // when & then
            BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.getCategory(categoryId));

            assertEquals(CategoryErrorCode.NOT_FOUND_CATEGORY, ex.getErrorCode());
            verify(categoryRepository, times(1)).findById(categoryId);
        }
    }

    @Nested
    @DisplayName("카테고리 전체 조회")
    class GetCategoriesTest {
        @Test
        @DisplayName("성공: 활성 카테고리를 정렬 순서대로 DTO 리스트로 반환한다")
        void success() {
            // given
            Category category1 = mock(Category.class);
            Category category2 = mock(Category.class);

            when(categoryRepository.findActiveCategoriesOrderBySortOrder()).thenReturn(List.of(category1, category2));

            CategoryResDto dto1 = mock(CategoryResDto.class);
            CategoryResDto dto2 = mock(CategoryResDto.class);

            try (MockedStatic<CategoryResDto> mocked = mockStatic(CategoryResDto.class)) {
                mocked.when(() -> CategoryResDto.from(category1)).thenReturn(dto1);
                mocked.when(() -> CategoryResDto.from(category2)).thenReturn(dto2);

                // when
                List<CategoryResDto> result = categoryService.getCategories();

                // then
                assertNotNull(result);
                assertEquals(2, result.size());
                assertSame(dto1, result.get(0));
                assertSame(dto2, result.get(1));

                verify(categoryRepository, times(1)).findActiveCategoriesOrderBySortOrder();
            }
        }

        @Test
        @DisplayName("실패: 등록된 카테고리가 없으면 예외가 발생한다")
        void failNotFoundCategories() {
            // given
            when(categoryRepository.findActiveCategoriesOrderBySortOrder()).thenReturn(List.of());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> categoryService.getCategories());

            assertEquals(CategoryErrorCode.NOT_FOUND_CATEGORIES, exception.getErrorCode());
            verify(categoryRepository, times(1)).findActiveCategoriesOrderBySortOrder();
        }
    }

    @Nested
    @DisplayName("관리자 카테고리 목록 조회")
    class GetCategoriesAdminTest {

        @Test
        @DisplayName("성공: 조건에 맞는 카테고리를 페이징 조회한다")
        void success() {
            // given
            int page = 0;
            int size = 10;
            String sortBy = "sortOrder";
            boolean isAsc = true;
            String name = "한";
            boolean isDeleted = false;

            Page<CategoryAdminResDto> response = new PageImpl<>(List.of(mock(CategoryAdminResDto.class)), PageRequest.of(page, size), 1);

            when(categoryCustomRepository.searchCategoriesAdmin(any(Pageable.class), eq(name), eq(isDeleted)))
                    .thenReturn(response);

            // when
            Page<CategoryAdminResDto> result = categoryService.getCategoriesAdmin(page, size, sortBy, isAsc, name, isDeleted);

            // then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(categoryCustomRepository, times(1)).searchCategoriesAdmin(pageableCaptor.capture(), eq(name), eq(isDeleted));

            Pageable pageableArg = pageableCaptor.getValue();
            assertEquals(page, pageableArg.getPageNumber());
            assertEquals(size, pageableArg.getPageSize());
            assertEquals(Sort.Direction.ASC, pageableArg.getSort().getOrderFor(sortBy).getDirection());
        }

        @Test
        @DisplayName("실패: 조회 결과가 없으면 NOT_FOUND_CATEGORIES 예외가 발생한다")
        void failNotFoundCategories() {
            // given
            when(categoryCustomRepository.searchCategoriesAdmin(any(Pageable.class), any(), any())).thenReturn(Page.empty());

            // when & then
            BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.getCategoriesAdmin(0, 10, "createdAt", true, null, false));

            assertEquals(CategoryErrorCode.NOT_FOUND_CATEGORIES, ex.getErrorCode());
        }
    }
}