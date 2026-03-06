package com.dfdt.delivery.domain.product.service;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.product.application.service.ProductQueryServiceImpl;
import com.dfdt.delivery.domain.product.domain.entity.Product;
import com.dfdt.delivery.domain.product.domain.enums.ProductErrorCode;
import com.dfdt.delivery.domain.product.domain.repository.JpaProductRepository;
import com.dfdt.delivery.domain.product.domain.repository.ProductCustomRepository;
import com.dfdt.delivery.domain.product.fixture.ProductFixture;
import com.dfdt.delivery.domain.product.fixture.RegionFixture;
import com.dfdt.delivery.domain.product.fixture.StoreFixture;
import com.dfdt.delivery.domain.product.fixture.UserFixture;
import com.dfdt.delivery.domain.product.presentation.dto.response.ProductAdminResDto;
import com.dfdt.delivery.domain.product.presentation.dto.response.ProductResDto;
import com.dfdt.delivery.domain.region.domain.entity.Region;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.enums.StoreErrorCode;
import com.dfdt.delivery.domain.store.domain.repository.JpaStoreRepository;
import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.infrastructure.persistence.repository.JpaUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductQueryService 테스트")
public class ProductQueryServiceImplTest {

    @InjectMocks
    private ProductQueryServiceImpl productService;

    @Mock
    private JpaProductRepository productRepository;

    @Mock
    private ProductCustomRepository productCustomRepository;

    @Mock
    private JpaStoreRepository storeRepository;

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private CustomUserDetails userDetails;

    private Store store;
    private Product product;
    private Region region;
    private User user;

    @BeforeEach
    public void setUp() {
        user = UserFixture.createUser();
        userDetails = new CustomUserDetails(user);
        region = RegionFixture.createOrderEnabledRegion();
        store = StoreFixture.createStore(user, region);
        product = ProductFixture.createProduct(user, store);
    }

    @Nested
    @DisplayName("메뉴 단건 조회")
    class GetProductTest {
        @Test
        @DisplayName("성공: 가게와 메뉴가 존재하면 메뉴 정보를 반환한다")
        void success() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = product.getProductId();

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.of(store));
            when(productRepository.findByProductIdAndStoreId(productId, storeId)).thenReturn(Optional.of(product));

            // when
            ProductResDto result = productService.getProduct(storeId, productId);

            // then
            assertNotNull(result);
            assertEquals(result.getProductId(), productId);
            assertEquals(result.getName(), product.getName());
            verify(storeRepository, times(1)).findByStoreIdAndNotDeleted(storeId);
            verify(productRepository, times(1)).findByProductIdAndStoreId(productId, storeId);
        }

        @Test
        @DisplayName("실패: 가게가 존재하지 않으면 예외가 발생한다")
        void failStoreNotFound() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = product.getProductId();

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class, () -> productService.getProduct(storeId, productId));

            // then
            assertEquals(StoreErrorCode.NOT_FOUND_STORE, exception.getErrorCode());
            verify(storeRepository, times(1)).findByStoreIdAndNotDeleted(storeId);
            verify(productRepository, never()).findByProductIdAndStoreId(any(), any());
        }

        @Test
        @DisplayName("실패: 메뉴가 존재하지 않으면 예외가 발생한다")
        void failProductNotFound() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = product.getProductId();

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.of(store));
            when(productRepository.findByProductIdAndStoreId(productId, storeId)).thenReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class, () -> productService.getProduct(storeId, productId));

            // then
            assertEquals(ProductErrorCode.NOT_FOUND_PRODUCT, exception.getErrorCode());
            verify(storeRepository, times(1)).findByStoreIdAndNotDeleted(storeId);
            verify(productRepository, times(1)).findByProductIdAndStoreId(productId, storeId);
        }
    }

    @Nested
    @DisplayName("메뉴 목록 조회")
    class GetProductsTest {

        @Test
        @DisplayName("성공: 조건에 맞는 메뉴 목록이 있으면 페이지 형태로 반환한다")
        void success() {
            // given
            UUID storeId = store.getStoreId();
            int page = 0;
            int size = 10;
            String sortBy = "createdAt";
            boolean isAsc = true;
            String keyword = product.getName();

            Pageable pageable = PageRequest.of(page, size, Sort.by(isAsc ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy));

            ProductResDto dto = mock(ProductResDto.class);
            Page<ProductResDto> productPage = new PageImpl<>(List.of(dto), pageable, 1);

            when(productCustomRepository.searchProducts(pageable, storeId, keyword)).thenReturn(productPage);

            // when
            Page<ProductResDto> result = productService.getProducts(storeId, page, size, sortBy, isAsc, keyword);

            // then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(1, result.getContent().size());

            verify(productCustomRepository, times(1)).searchProducts(pageable, storeId, keyword);
        }

        @Test
        @DisplayName("실패: 조회된 메뉴 목록이 없으면 예외가 발생한다")
        void failNotFoundProducts() {
            // given
            UUID storeId = store.getStoreId();
            when(productCustomRepository.searchProducts(any(Pageable.class), any(), any())).thenReturn(Page.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> productService.getProducts(storeId, 0, 10, "createdAt", true, product.getName()));

            // then
            assertEquals(ProductErrorCode.NOT_FOUND_PRODUCTS, exception.getErrorCode());
            verify(productCustomRepository, times(1)).searchProducts(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("관리자 메뉴 목록 조회")
    class GetProductsAdminTest {
        @Test
        @DisplayName("성공: 관리자용 메뉴 목록을 조회한다")
        void success() {
            // given
            UUID storeId = store.getStoreId();
            int page = 0;
            int size = 10;
            String sortBy = "createdAt";
            boolean isAsc = true;
            String keyword = product.getName();
            Boolean isDeleted = false;

            Pageable pageable = PageRequest.of(page, size, Sort.by(isAsc ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy));
            Page<ProductAdminResDto> productPage = new PageImpl<>(List.of(mock(ProductAdminResDto.class)), pageable, 1);

            when(productCustomRepository.searchAdminProducts(pageable, storeId, keyword, isDeleted)).thenReturn(productPage);

            // when
            Page<ProductAdminResDto> result = productService.getProductsAdmin(storeId, page, size, sortBy, isAsc, keyword, isDeleted);

            // then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());

            verify(productCustomRepository).searchAdminProducts(
                    argThat(p ->
                            p.getPageNumber() == 0 &&
                            p.getPageSize() == 10 &&
                            p.getSort().getOrderFor("createdAt") != null &&
                            p.getSort().getOrderFor("createdAt").isAscending()
                    ),
                    eq(storeId),
                    eq(keyword),
                    eq(isDeleted)
            );
        }

        @Test
        @DisplayName("실패: 조회된 관리자용 메뉴 목록이 없으면 예외가 발생한다")
        void failNotFoundProducts() {
            // given
            UUID storeId = store.getStoreId();

            when(productCustomRepository.searchAdminProducts(any(Pageable.class), any(), any(), any())).thenReturn(Page.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> productService.getProductsAdmin(storeId, 0, 10, "createdAt", true, product.getName(), true));

            // then
            assertEquals(ProductErrorCode.NOT_FOUND_PRODUCTS, exception.getErrorCode());
            verify(productCustomRepository, times(1)).searchAdminProducts(any(), any(), any(), any());
        }
    }
}
