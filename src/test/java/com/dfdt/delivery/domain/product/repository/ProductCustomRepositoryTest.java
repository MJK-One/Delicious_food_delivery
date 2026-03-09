package com.dfdt.delivery.domain.product.repository;

import com.dfdt.delivery.domain.product.domain.entity.Product;
import com.dfdt.delivery.domain.product.domain.repository.ProductCustomRepository;
import com.dfdt.delivery.domain.product.domain.repository.ProductRepository;
import com.dfdt.delivery.domain.product.fixture.ProductFixture;
import com.dfdt.delivery.domain.product.fixture.RegionFixture;
import com.dfdt.delivery.domain.product.fixture.StoreFixture;
import com.dfdt.delivery.domain.product.fixture.UserFixture;
import com.dfdt.delivery.domain.product.infrastructure.persistence.repository.ProductCustomRepositoryImpl;
import com.dfdt.delivery.domain.product.presentation.dto.response.ProductAdminResDto;
import com.dfdt.delivery.domain.product.presentation.dto.response.ProductResDto;
import com.dfdt.delivery.domain.region.domain.entity.Region;
import com.dfdt.delivery.domain.store.domain.entity.Store;
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
@Import(ProductCustomRepositoryImpl.class)
class ProductCustomRepositoryTest {

    @Autowired
    private ProductCustomRepository productCustomRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager em;

    private Store store;
    private User owner;
    private Region region;

    private Product product0;
    private Product product1;
    private Product product2;
    private Product product3;

    @TestConfiguration
    static class QuerydslTestConfig {
        @PersistenceContext
        private EntityManager em;

        @Bean
        public JPAQueryFactory jpaQueryFactory() {
            return new JPAQueryFactory(em);
        }
    }

    @BeforeEach
    void setUp() {
        owner = UserFixture.createRepoUser();
        region = RegionFixture.createNoIdRegion();

        em.persist(owner);
        em.persist(region);
        em.flush();

        store = StoreFixture.createNoIdStore(owner, region);
        product0 = ProductFixture.createNoIdProduct(owner, store, "치킨마요", 0);
        product1 = ProductFixture.createNoIdProduct(owner, store, "제육덮밥", 1);
        product2 = ProductFixture.createNoIdProduct(owner, store, "치즈돈까스", 2);
        product3 = ProductFixture.createNoIdProduct(owner, store, "참치김밥", 3);

        em.persist(store);
        em.persist(product0);
        em.persist(product1);
        em.persist(product2);
        em.persist(product3);

        em.flush();
    }

    // searchProducts 페이징, 필터링 목록 조회
    @Test
    @DisplayName("특정 가게의 상품 목록을 조회")
    void success() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "displayOrder"));

        // when
        Page<ProductResDto> result = productCustomRepository.searchProducts(pageable, store.getStoreId(), null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getContent()).hasSize(4);
    }

    @Test
    @DisplayName("키워드로 상품명을 검색")
    void successKeyword() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "displayOrder"));

        // when
        Page<ProductResDto> result = productCustomRepository.searchProducts(pageable, store.getStoreId(), "치킨");

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).contains("치킨마요");
    }

    @Test
    @DisplayName("페이징이 적용")
    void successPaging() {
        // given
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "displayOrder"));

        // when
        Page<ProductResDto> result = productCustomRepository.searchProducts(pageable, store.getStoreId(), null);

        // then
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("정렬이 적용")
    void successSort() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "displayOrder"));

        // when
        Page<ProductResDto> result = productCustomRepository.searchProducts(pageable, store.getStoreId(), null);

        // then
        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getContent().get(0).getDisplayOrder()).isEqualTo(3);
        assertThat(result.getContent().get(1).getDisplayOrder()).isEqualTo(2);
        assertThat(result.getContent().get(2).getDisplayOrder()).isEqualTo(1);
        assertThat(result.getContent().get(3).getDisplayOrder()).isEqualTo(0);
    }

    @Test
    @DisplayName("삭제된 상품은 일반 조회에서 제외")
    void successExcludeDeleted() {
        // given
        Product deletedProduct = productRepository.findByProductIdAndStoreId(product3.getProductId(), store.getStoreId()).orElseThrow();
        deletedProduct.delete(owner.getUsername());
        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "displayOrder"));

        // when
        Page<ProductResDto> result = productCustomRepository.searchProducts(pageable, store.getStoreId(), null);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3);
    }

    // searchAdminProducts 페이징, 필터링 목록 조회(관리자용)
    @Test
    @DisplayName("삭제되지 않은 상품만 조회")
    void successNotDeletedOnly() {
        // given
        Product deletedProduct = productRepository.findByProductIdAndStoreId(product3.getProductId(), store.getStoreId()).orElseThrow();
        deletedProduct.delete(owner.getUsername());
        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "displayOrder"));

        // when
        Page<ProductAdminResDto> result = productCustomRepository.searchAdminProducts(pageable, store.getStoreId(), null, false);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("삭제된 상품만 조회")
    void successDeletedOnly() {
        // given
        Product deletedProduct = productRepository.findByProductIdAndStoreId(product3.getProductId(), store.getStoreId()).orElseThrow();
        deletedProduct.delete(owner.getUsername());
        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "displayOrder"));

        // when
        Page<ProductAdminResDto> result = productCustomRepository.searchAdminProducts(pageable, store.getStoreId(), null, true);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("참치김밥");
    }

    @Test
    @DisplayName("키워드 검색로 검색")
    void successKeywordAdmin() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "displayOrder"));

        // when
        Page<ProductAdminResDto> result = productCustomRepository.searchAdminProducts(pageable, store.getStoreId(), "치킨", false);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).contains("치킨마요");
    }

    @Test
    @DisplayName("다른 가게 상품은 관리자 조회에서도 제외")
    void successOnlyTargetStore() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<ProductAdminResDto> result = productCustomRepository.searchAdminProducts(pageable, store.getStoreId(), "버거", false);

        // then
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }
}