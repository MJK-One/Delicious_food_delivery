package com.dfdt.delivery.domain.product.repository;

import com.dfdt.delivery.domain.product.domain.entity.Product;
import com.dfdt.delivery.domain.product.domain.repository.JpaProductRepository;
import com.dfdt.delivery.domain.product.fixture.ProductFixture;
import com.dfdt.delivery.domain.product.fixture.RegionFixture;
import com.dfdt.delivery.domain.product.fixture.StoreFixture;
import com.dfdt.delivery.domain.product.fixture.UserFixture;
import com.dfdt.delivery.domain.region.domain.entity.Region;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private JpaProductRepository productRepository;

    @Autowired
    private TestEntityManager em;

    private Store store;
    private User owner;
    private Region region;

    private Product product0;
    private Product product1;
    private Product product2;
    private Product product3;

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

    @Nested
    @DisplayName("findByProductIdAndStoreId")
    class FindByProductIdAndStoreIdTest {

        @Test
        @DisplayName("상품 ID와 가게 ID로 상품을 조회")
        void success() {
            // when
            Optional<Product> result = productRepository.findByProductIdAndStoreId(product0.getProductId(), store.getStoreId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getProductId()).isEqualTo(product0.getProductId());
        }

        @Test
        @DisplayName("존재하지 않으면 empty를 반환")
        void empty() {
            // when
            Optional<Product> result = productRepository.findByProductIdAndStoreId(UUID.randomUUID(), store.getStoreId());

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findMaxDisplayOrder")
    class FindMaxDisplayOrderTest {

        @Test
        @DisplayName("해당 가게의 최대 displayOrder를 조회")
        void success() {
            // when
            Optional<Integer> result = productRepository.findMaxDisplayOrder(store.getStoreId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("삭제된 상품은 최대 displayOrder 계산에서 제외")
        void ignoreDeletedProduct() {
            // given
            product3.delete(owner.getUsername());
            em.flush();

            // when
            Optional<Integer> result = productRepository.findMaxDisplayOrder(store.getStoreId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("shiftDisplayOrdersUp")
    class ShiftDisplayOrdersUpTest {

        @Test
        @DisplayName("지정 범위의 displayOrder를 1씩 증가")
        void success() {
            // when
            productRepository.shiftDisplayOrdersUp(store.getStoreId(), 0, 2);
            em.flush();
            em.clear();

            // then
            Product result0 = productRepository.findByProductIdAndStoreId(product0.getProductId(), store.getStoreId()).orElseThrow();
            Product result1 = productRepository.findByProductIdAndStoreId(product1.getProductId(), store.getStoreId()).orElseThrow();
            Product result2 = productRepository.findByProductIdAndStoreId(product2.getProductId(), store.getStoreId()).orElseThrow();

            assertThat(result0.getDisplayOrder()).isEqualTo(1);
            assertThat(result1.getDisplayOrder()).isEqualTo(2);
            assertThat(result2.getDisplayOrder()).isEqualTo(3);
        }

        @Test
        @DisplayName("삭제된 상품은 증가 대상에서 제외")
        void ignoreDeletedProduct() {
            // given
            product1.delete(owner.getUsername());
            em.flush();

            // when
            productRepository.shiftDisplayOrdersUp(store.getStoreId(), 0, 2);
            em.flush();
            em.clear();

            // then
            Product result0 = productRepository.findByProductIdAndStoreId(product0.getProductId(), store.getStoreId()).orElseThrow();
            Product result1 = em.find(Product.class, product1.getProductId());
            Product result2 = productRepository.findByProductIdAndStoreId(product2.getProductId(), store.getStoreId()).orElseThrow();

            assertThat(result0.getDisplayOrder()).isEqualTo(1);
            assertThat(result1.getDisplayOrder()).isEqualTo(1); // 삭제된 상품은 그대로
            assertThat(result2.getDisplayOrder()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("shiftDisplayOrdersDown")
    class ShiftDisplayOrdersDownTest {

        @Test
        @DisplayName("지정 범위의 displayOrder를 1씩 감소")
        void success() {
            // when
            productRepository.shiftDisplayOrdersDown(store.getStoreId(), 1, 3);
            em.flush();
            em.clear();

            // then
            Product result1 = productRepository.findByProductIdAndStoreId(product1.getProductId(), store.getStoreId()).orElseThrow();
            Product result2 = productRepository.findByProductIdAndStoreId(product2.getProductId(), store.getStoreId()).orElseThrow();
            Product result3 = productRepository.findByProductIdAndStoreId(product3.getProductId(), store.getStoreId()).orElseThrow();

            assertThat(result1.getDisplayOrder()).isEqualTo(0);
            assertThat(result2.getDisplayOrder()).isEqualTo(1);
            assertThat(result3.getDisplayOrder()).isEqualTo(2);
        }

        @Test
        @DisplayName("삭제된 상품은 감소 대상에서 제외")
        void ignoreDeletedProduct() {
            // given
            product2.delete(owner.getUsername());
            em.flush();

            // when
            productRepository.shiftDisplayOrdersDown(store.getStoreId(), 1, 3);
            em.flush();
            em.clear();

            // then
            Product result1 = productRepository.findByProductIdAndStoreId(product1.getProductId(), store.getStoreId()).orElseThrow();
            Product result2 = em.find(Product.class, product2.getProductId());
            Product result3 = productRepository.findByProductIdAndStoreId(product3.getProductId(), store.getStoreId()).orElseThrow();

            assertThat(result1.getDisplayOrder()).isEqualTo(0);
            assertThat(result2.getDisplayOrder()).isEqualTo(2); // 삭제된 상품은 그대로
            assertThat(result3.getDisplayOrder()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("decreaseDisplayOrder")
    class DecreaseDisplayOrderTest {

        @Test
        @DisplayName("삭제된 displayOrder보다 큰 상품들의 displayOrder를 1씩 감소")
        void success() {
            // given
            product0.delete(owner.getUsername());
            em.flush();

            // when
            productRepository.decreaseDisplayOrder(store.getStoreId(), 0);
            em.flush();
            em.clear();

            // then
            Product result0 = em.find(Product.class, product0.getProductId());
            Product result1 = productRepository.findByProductIdAndStoreId(product1.getProductId(), store.getStoreId()).orElseThrow();
            Product result2 = productRepository.findByProductIdAndStoreId(product2.getProductId(), store.getStoreId()).orElseThrow();
            Product result3 = productRepository.findByProductIdAndStoreId(product3.getProductId(), store.getStoreId()).orElseThrow();

            assertThat(result0.getDisplayOrder()).isEqualTo(0);
            assertThat(result1.getDisplayOrder()).isEqualTo(0);
            assertThat(result2.getDisplayOrder()).isEqualTo(1);
            assertThat(result3.getDisplayOrder()).isEqualTo(2);
        }
    }
}
