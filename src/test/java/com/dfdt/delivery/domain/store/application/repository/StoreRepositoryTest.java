package com.dfdt.delivery.domain.store.application.repository;

import com.dfdt.delivery.common.config.QueryDslConfig;
import com.dfdt.delivery.domain.category.domain.entity.Category;
import com.dfdt.delivery.domain.category.domain.repository.JpaCategoryRepository;
import com.dfdt.delivery.domain.region.domain.entity.Region;
import com.dfdt.delivery.domain.store.application.fixture.CategoryFixture;
import com.dfdt.delivery.domain.store.application.fixture.RegionFixture;
import com.dfdt.delivery.domain.store.application.fixture.StoreFixture;
import com.dfdt.delivery.domain.store.application.fixture.UserFixture;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.enums.StoreStatus;
import com.dfdt.delivery.domain.store.domain.repository.JpaStoreRepository;
import com.dfdt.delivery.domain.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
class StoreRepositoryTest {

    @Autowired
    private JpaStoreRepository storeRepository;

    @Autowired
    private JpaCategoryRepository categoryRepository;

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
        store3.addCategory(category1, owner.getUsername());
        store3.addCategory(category2, owner.getUsername());

        em.flush();
    }

    @Nested
    @DisplayName("findByUser_UsernameOrderByCreateAuditAsc")
    class FindByUserUsernameOrderByCreateAuditAscTest {

        @Test
        @DisplayName("사용자의 가게 목록을 생성일 오름차순으로 조회")
        void success() {
            // when
            List<Store> result = storeRepository.findByUser_UsernameOrderByCreateAuditAsc(owner.getUsername());

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(store -> store.getUser().getUsername()).containsOnly(owner.getUsername());
        }

        @Test
        @DisplayName("해당 사용자의 가게가 없으면 빈 리스트를 반환한다")
        void empty() {
            // when
            List<Store> result = storeRepository.findByUser_UsernameOrderByCreateAuditAsc("noUser");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByStoreIdAndNotDeleted")
    class FindByStoreIdAndNotDeletedTest {

        @Test
        @DisplayName("삭제되지 않은 가게를 조회")
        void success() {
            // when
            Optional<Store> result = storeRepository.findByStoreIdAndNotDeleted(store1.getStoreId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo(store1.getName());
        }

        @Test
        @DisplayName("삭제된 가게는 조회 X")
        void deletedStoreNotFound() {
            // given
            store1.delete(owner.getUsername());
            em.flush();
            em.clear();

            // when
            Optional<Store> result = storeRepository.findByStoreIdAndNotDeleted(store1.getStoreId());

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByCategoryIdAndNotDeleted")
    class ExistsByCategoryIdAndNotDeletedTest {

        @Test
        @DisplayName("해당 카테고리를 사용하는 삭제되지 않은 가게가 있으면 true를 반환")
        void successTrue() {
            // when
            boolean result = storeRepository.existsByCategoryIdAndNotDeleted(category1.getCategoryId());

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("해당 카테고리를 사용하는 삭제되지 않은 가게가 없으면 false를 반환")
        void successFalse() {
            // given
            Category category3 = CategoryFixture.repoCategory("분식", 3);
            categoryRepository.save(category3);
            em.flush();
            em.clear();

            // when
            boolean result = storeRepository.existsByCategoryIdAndNotDeleted(category3.getCategoryId());

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("findStoresByStatusNotDeleted")
    class FindStoresByStatusNotDeletedTest {

        @Test
        @DisplayName("상태값이 일치하고 삭제되지 않은 가게를 조회")
        void success() {
            // when
            List<Store> result = storeRepository.findStoresByStatusNotDeleted(StoreStatus.APPROVED);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Store::getStatus).containsOnly(StoreStatus.APPROVED);
        }

        @Test
        @DisplayName("삭제된 가게는 조회에서 제외")
        void excludeDeletedStore() {
            // given
            store1.delete(owner.getUsername());
            em.flush();
            em.clear();

            // when
            List<Store> result = storeRepository.findStoresByStatusNotDeleted(StoreStatus.APPROVED);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getStoreId()).isEqualTo(store3.getStoreId());
        }

        @Test
        @DisplayName("상태값이 일치하는 가게가 없으면 빈 리스트를 반환")
        void empty() {
            // when
            List<Store> result = storeRepository.findStoresByStatusNotDeleted(StoreStatus.REJECTED);

            // then
            assertThat(result).isEmpty();
        }
    }
}