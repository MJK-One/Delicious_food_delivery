package com.dfdt.delivery.domain.category.application.repository;

import com.dfdt.delivery.domain.category.application.fixture.CategoryFixture;
import com.dfdt.delivery.domain.category.domain.entity.Category;
import com.dfdt.delivery.domain.category.domain.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager em;

    private Category category0;
    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        category0 = CategoryFixture.repoCategory("한식", 0);
        category1 = CategoryFixture.repoCategory("일식", 1);
        category2 = CategoryFixture.repoCategory("양식", 2);

        em.persist(category0);
        em.persist(category1);
        em.persist(category2);

        em.flush();
    }

    @Nested
    @DisplayName("existsByNameAndCategoryIdNot")
    class ExistsByNameAndCategoryIdNotTest {

        @Test
        @DisplayName("같은 이름의 다른 카테고리가 있으면 true를 반환")
        void success_true() {
            // when
            boolean result = categoryRepository.existsByNameAndCategoryIdNot("한식", category1.getCategoryId());

            // then
            assertTrue(result);
        }

        @Test
        @DisplayName("같은 이름의 다른 카테고리가 없으면 false를 반환")
        void success_false() {
            // when
            boolean result = categoryRepository.existsByNameAndCategoryIdNot("한식", category0.getCategoryId());

            // then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("findMaxSortOrder")
    class FindMaxSortOrderTest {

        @Test
        @DisplayName("삭제되지 않은 카테고리의 최대 sortOrder를 조회")
        void success() {
            // when
            Optional<Integer> result = categoryRepository.findMaxSortOrder();

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("삭제된 카테고리는 최대 sortOrder 계산에서 제외")
        void ignoreDeletedCategory() {
            // given
            category2.delete("tester");
            em.flush();

            // when
            Optional<Integer> result = categoryRepository.findMaxSortOrder();

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("findActiveCategoriesOrderBySortOrder")
    class FindActiveCategoriesOrderBySortOrderTest {

        @Test
        @DisplayName("삭제되지 않은 카테고리를 sortOrder 오름차순으로 조회")
        void success() {
            // when
            List<Category> result = categoryRepository.findActiveCategoriesOrderBySortOrder();

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getSortOrder()).isEqualTo(0);
            assertThat(result.get(1).getSortOrder()).isEqualTo(1);
            assertThat(result.get(2).getSortOrder()).isEqualTo(2);
        }

        @Test
        @DisplayName("삭제된 카테고리는 조회에서 제외")
        void excludeDeleted() {
            // given
            category0.delete("tester");
            em.flush();
            em.clear();

            // when
            List<Category> result = categoryRepository.findActiveCategoriesOrderBySortOrder();

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Category::getName).containsExactly("일식", "양식");
        }
    }

    @Nested
    @DisplayName("findActiveCategoryById")
    class FindActiveCategoryByIdTest {

        @Test
        @DisplayName("삭제되지 않은 카테고리를 ID로 조회")
        void success() {
            // when
            Optional<Category> result = categoryRepository.findActiveCategoryById(category0.getCategoryId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("한식");
        }

        @Test
        @DisplayName("삭제된 카테고리는 조회 X")
        void deletedCategoryNotFound() {
            // given
            category0.delete("tester");
            em.flush();
            em.clear();

            // when
            Optional<Category> result = categoryRepository.findActiveCategoryById(category0.getCategoryId());

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("shiftSortOrdersUp")
    class ShiftSortOrdersUpTest {

        @Test
        @DisplayName("지정 범위의 sortOrder를 1씩 증가")
        void success() {
            // when
            categoryRepository.shiftSortOrdersUp(0, 1);
            em.flush();
            em.clear();

            // then
            Category result0 = categoryRepository.findActiveCategoryById(category0.getCategoryId()).orElseThrow();
            Category result1 = categoryRepository.findActiveCategoryById(category1.getCategoryId()).orElseThrow();

            assertThat(result0.getSortOrder()).isEqualTo(1);
            assertThat(result1.getSortOrder()).isEqualTo(2);
        }

        @Test
        @DisplayName("삭제된 카테고리는 증가 대상에서 제외")
        void ignoreDeleted() {
            // given
            category1.delete("tester");
            em.flush();

            // when
            categoryRepository.shiftSortOrdersUp(0, 1);
            em.flush();
            em.clear();

            // then
            Category result0 = categoryRepository.findActiveCategoryById(category0.getCategoryId()).orElseThrow();
            Category result1 = em.find(Category.class, category1.getCategoryId());
            Category result2 = categoryRepository.findActiveCategoryById(category2.getCategoryId()).orElseThrow();

            assertThat(result0.getSortOrder()).isEqualTo(1);
            assertThat(result1.getSortOrder()).isEqualTo(1);
            assertThat(result2.getSortOrder()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("shiftSortOrdersDown")
    class ShiftSortOrdersDownTest {

        @Test
        @DisplayName("지정 범위의 sortOrder를 1씩 감소")
        void success() {
            // when
            categoryRepository.shiftSortOrdersDown(1, 2);
            em.flush();
            em.clear();

            // then
            Category result0 = categoryRepository.findActiveCategoryById(category0.getCategoryId()).orElseThrow();
            Category result1 = categoryRepository.findActiveCategoryById(category1.getCategoryId()).orElseThrow();
            Category result2 = categoryRepository.findActiveCategoryById(category2.getCategoryId()).orElseThrow();

            assertThat(result0.getSortOrder()).isEqualTo(0);
            assertThat(result1.getSortOrder()).isEqualTo(0);
            assertThat(result2.getSortOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("삭제된 카테고리는 감소 대상에서 제외")
        void ignoreDeleted() {
            // given
            category1.delete("tester");
            em.flush();

            // when
            categoryRepository.shiftSortOrdersDown(1, 2);
            em.flush();
            em.clear();

            // then
            Category result0 = categoryRepository.findActiveCategoryById(category0.getCategoryId()).orElseThrow();
            Category result1 = em.find(Category.class, category1.getCategoryId());
            Category result2 = categoryRepository.findActiveCategoryById(category2.getCategoryId()).orElseThrow();

            assertThat(result0.getSortOrder()).isEqualTo(0);
            assertThat(result1.getSortOrder()).isEqualTo(1);
            assertThat(result2.getSortOrder()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("decrementSortOrdersAfter")
    class DecrementSortOrdersAfterTest {

        @Test
        @DisplayName("deletedOrder보다 큰 sortOrder를 1씩 감소")
        void success() {
            // when
            categoryRepository.decrementSortOrdersAfter(0);
            em.flush();
            em.clear();

            // then
            Category result1 = categoryRepository.findActiveCategoryById(category1.getCategoryId()).orElseThrow();
            Category result2 = categoryRepository.findActiveCategoryById(category2.getCategoryId()).orElseThrow();

            assertThat(result1.getSortOrder()).isEqualTo(0);
            assertThat(result2.getSortOrder()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("existsByNameAndDeletedAtIsNull")
    class ExistsByNameAndDeletedAtIsNullTest {

        @Test
        @DisplayName("삭제되지 않은 같은 이름의 카테고리가 있으면 true를 반환")
        void successTrue() {
            // when
            boolean result = categoryRepository.existsByNameAndDeletedAtIsNull("한식");

            // then
            assertTrue(result);
        }

        @Test
        @DisplayName("삭제된 카테고리만 있으면 false를 반환")
        void successFalseWhenDeleted() {
            // given
            category1.delete("tester");
            em.flush();

            // when
            boolean result = categoryRepository.existsByNameAndDeletedAtIsNull("한식1");

            // then
            assertFalse(result);
        }

        @Test
        @DisplayName("같은 이름의 카테고리가 없으면 false를 반환한다")
        void successFalse() {
            // when
            boolean result = categoryRepository.existsByNameAndDeletedAtIsNull("분식");

            // then
            assertThat(result).isFalse();
        }
    }
}