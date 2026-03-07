package com.dfdt.delivery.domain.category.application.repository;

import com.dfdt.delivery.domain.category.application.fixture.CategoryFixture;
import com.dfdt.delivery.domain.category.domain.entity.Category;
import com.dfdt.delivery.domain.category.domain.repository.CategoryCustomRepository;
import com.dfdt.delivery.domain.category.domain.repository.CategoryRepository;
import com.dfdt.delivery.domain.category.infrastructure.persistence.repository.CategoryCustomRepositoryImpl;
import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryAdminResDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(CategoryCustomRepositoryImpl.class)
public class CategoryCustomRepositoryTest {

    @Autowired
    private CategoryCustomRepository categoryCustomRepository;

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

    @TestConfiguration
    static class QuerydslConfig {

        @PersistenceContext
        private EntityManager em;

        @Bean
        JPAQueryFactory jpaQueryFactory() {
            return new JPAQueryFactory(em);
        }
    }

    @Test
    @DisplayName("이름으로 검색")
    void successSearchByName() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "sortOrder"));

        // when
        Page<CategoryAdminResDto> result = categoryCustomRepository.searchCategoriesAdmin(pageable, "한식", false);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("한식");
    }

    @Test
    @DisplayName("삭제되지 않은 카테고리만 조회")
    void successNotDeletedOnly() {
        // given
        category2.delete("tester");
        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "sortOrder"));

        // when
        Page<CategoryAdminResDto> result = categoryCustomRepository.searchCategoriesAdmin(pageable, null, false);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(CategoryAdminResDto::getName)
                .containsExactly("한식", "일식");
    }

    @Test
    @DisplayName("삭제된 카테고리만 조회")
    void successDeletedOnly() {
        // given
        category2.delete("tester");
        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "sortOrder"));

        // when
        Page<CategoryAdminResDto> result = categoryCustomRepository.searchCategoriesAdmin(pageable, null, true);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("양식");
    }

    @Test
    @DisplayName("삭제 여부와 이름 조건을 함께 적용")
    void successNameAndDeletedCondition() {
        // given
        category2.delete("tester");
        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "sortOrder"));

        // when
        Page<CategoryAdminResDto> result = categoryCustomRepository.searchCategoriesAdmin(pageable, "양식", true);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("양식");
    }

    @Test
    @DisplayName("페이징 적용")
    void successPaging() {
        // given
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "sortOrder"));

        // when
        Page<CategoryAdminResDto> result = categoryCustomRepository.searchCategoriesAdmin(pageable, null, false);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("정렬 적용")
    void successSorting() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "sortOrder"));

        // when
        Page<CategoryAdminResDto> result = categoryCustomRepository.searchCategoriesAdmin(pageable, null, false);

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).getSortOrder()).isEqualTo(2);
        assertThat(result.getContent().get(1).getSortOrder()).isEqualTo(1);
        assertThat(result.getContent().get(2).getSortOrder()).isEqualTo(0);
    }
}
