package com.dfdt.delivery.domain.store.application.repository;

import com.dfdt.delivery.domain.category.domain.entity.Category;
import com.dfdt.delivery.domain.region.domain.entity.Region;
import com.dfdt.delivery.domain.store.application.fixture.CategoryFixture;
import com.dfdt.delivery.domain.store.application.fixture.RegionFixture;
import com.dfdt.delivery.domain.store.application.fixture.StoreFixture;
import com.dfdt.delivery.domain.store.application.fixture.UserFixture;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.enums.StoreStatus;
import com.dfdt.delivery.domain.store.domain.repository.StoreCustomRepository;
import com.dfdt.delivery.domain.store.infrastructure.persistence.repository.StoreCustomRepositoryImpl;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreAdminResDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreResDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreStatusRequestResDto;
import com.dfdt.delivery.domain.user.domain.entity.User;
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
@Import(StoreCustomRepositoryImpl.class)
public class StoreCustomRepositoryTest {

    @Autowired
    private StoreCustomRepository storeCustomRepository;

    @Autowired
    private TestEntityManager em;

    private Category category1;
    private Category category2;
    private User owner;
    private User anotherOwner;
    private Region region;

    private Store store1;
    private Store store2;
    private Store store3;


    @BeforeEach
    void setUp() {
        owner = UserFixture.createRepoUser();
        anotherOwner = UserFixture.createAnotherRepoUser();
        region = RegionFixture.createNoIdRegion();

        category1 = CategoryFixture.repoCategory("한식", 1);
        category2 = CategoryFixture.repoCategory("양식", 2);

        em.persist(owner);
        em.persist(anotherOwner);
        em.persist(region);
        em.persist(category1);
        em.persist(category2);
        em.flush();

        store1 = StoreFixture.createRepoStore(owner, region, "가게1", StoreStatus.APPROVED);
        store2 = StoreFixture.createRepoStore(owner, region, "가게2", StoreStatus.REQUESTED);
        store3 = StoreFixture.createRepoStore(anotherOwner, region, "가게3", StoreStatus.APPROVED);

        em.persist(store1);
        em.persist(store2);
        em.persist(store3);
        em.flush();

        store1.addCategory(category1, owner.getUsername());
        store2.addCategory(category2, owner.getUsername());
        store3.addCategory(category2, owner.getUsername());

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

    // searchStores
    @Test
    @DisplayName("삭제되지 않은 승인된 전체 가게를 조회")
    void successAll() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));

        // when
        Page<StoreResDto> result = storeCustomRepository.searchStores(pageable, null, null);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("카테고리로 가게를 조회")
    void successCategory() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));

        // when
        Page<StoreResDto> result = storeCustomRepository.searchStores(pageable, category2.getCategoryId(), null);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("가게3");
    }

    @Test
    @DisplayName("이름으로 가게를 검색")
    void successName() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));

        // when
        Page<StoreResDto> result = storeCustomRepository.searchStores(pageable, null, "1");

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).contains("가게1");
    }

    @Test
    @DisplayName("카테고리와 이름 조건을 함께 적용")
    void successCategoryAndName() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));

        // when
        Page<StoreResDto> result = storeCustomRepository.searchStores(pageable, category2.getCategoryId(), "3");

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("가게3");
    }

    @Test
    @DisplayName("삭제된 가게는 일반 조회에서 제외")
    void successExcludeDeleted() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<StoreResDto> result = storeCustomRepository.searchStores(pageable, category2.getCategoryId(), null);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
                .extracting(StoreResDto::getName)
                .doesNotContain("삭제된 양식집");
    }

    @Test
    @DisplayName("페이징 적용")
    void successPaging() {
        // given
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "name"));

        // when
        Page<StoreResDto> result = storeCustomRepository.searchStores(pageable, null, null);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.hasNext()).isTrue();
    }

    // searchStoresAdmin 테스트
    @Test
    @DisplayName("삭제되지 않은 가게만 조회")
    void successNotDeletedOnly() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));

        // when
        Page<StoreAdminResDto> result = storeCustomRepository.searchStoresAdmin(pageable, null, null, false);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("삭제된 가게만 조회")
    void successDeletedOnly() {
        // given
        store3.delete(owner.getUsername());
        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));

        // when
        Page<StoreAdminResDto> result = storeCustomRepository.searchStoresAdmin(pageable, null, null, true);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("가게3");
    }

    @Test
    @DisplayName("관리자 조회에서 카테고리 필터가 적용")
    void successCategoryAdmin() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<StoreAdminResDto> result = storeCustomRepository.searchStoresAdmin(pageable, category1.getCategoryId(), null, false);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("관리자 조회에서 이름 검색이 적용")
    void successNameAdmin() {
        // given
        Pageable pageable = PageRequest.of(0, 2);

        // when
        Page<StoreAdminResDto> result = storeCustomRepository.searchStoresAdmin(pageable, null, "가게", false);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("관리자 조회에서 삭제 여부와 이름 조건을 함께 적용")
    void successNameAndDeleted() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<StoreAdminResDto> result = storeCustomRepository.searchStoresAdmin(pageable, null, "가게", false);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("가게1");
    }

    // searchRequestStores 테스트
    @Test
    @DisplayName("요청 상태의 가게를 조회")
    void successRequested() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));

        // when
        Page<StoreStatusRequestResDto> result = storeCustomRepository.searchRequestStores(pageable, StoreStatus.REQUESTED);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("다른 상태의 가게는 조회 X")
    void successOnlyRequestedStatus() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<StoreStatusRequestResDto> result = storeCustomRepository.searchRequestStores(pageable, StoreStatus.REQUESTED);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("가게2");
    }

    @Test
    @DisplayName("삭제된 가게는 요청 상태 조회에서 제외")
    void successRequestedExcludeDeleted() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<StoreStatusRequestResDto> result = storeCustomRepository.searchRequestStores(pageable, StoreStatus.APPROVED);

        // then
        assertThat(result.getContent())
                .extracting(StoreStatusRequestResDto::getName)
                .doesNotContain("삭제된 가게1");
    }

    @Test
    @DisplayName("페이징 적용")
    void successRequestedPaging() {
        // given
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "name"));

        // when
        Page<StoreStatusRequestResDto> result = storeCustomRepository.searchRequestStores(pageable, StoreStatus.REQUESTED);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
    }
}
