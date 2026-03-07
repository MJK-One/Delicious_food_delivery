package com.dfdt.delivery.domain.product.service;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.product.application.service.ProductCommandServiceImpl;
import com.dfdt.delivery.domain.product.domain.entity.Product;
import com.dfdt.delivery.domain.product.domain.enums.ProductErrorCode;
import com.dfdt.delivery.domain.product.domain.repository.JpaProductRepository;
import com.dfdt.delivery.domain.product.fixture.ProductFixture;
import com.dfdt.delivery.domain.product.fixture.RegionFixture;
import com.dfdt.delivery.domain.product.fixture.StoreFixture;
import com.dfdt.delivery.domain.product.fixture.UserFixture;
import com.dfdt.delivery.domain.product.presentation.dto.request.ProductCreateReqDto;
import com.dfdt.delivery.domain.product.presentation.dto.request.ProductUpdateReqDto;
import com.dfdt.delivery.domain.product.presentation.dto.response.ProductResDto;
import com.dfdt.delivery.domain.product.presentation.dto.response.ProductUpdateResDto;
import com.dfdt.delivery.domain.region.domain.entity.Region;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.enums.StoreErrorCode;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCommandService 테스트")
public class ProductCommandServiceImplTest {

    @InjectMocks
    private ProductCommandServiceImpl productService;

    @Mock
    private JpaProductRepository productRepository;

    @Mock
    private JpaStoreRepository storeRepository;

    @Mock
    private CustomUserDetails userDetails;

    @Mock
    private CustomUserDetails ownerUserDetails;

    @Mock
    private CustomUserDetails anotherOwnerDetails;

    private Store store;
    private Product product;
    private Region region;
    private User user;
    private User owner;
    private User anotherOwner;

    @BeforeEach
    public void setUp() {
        user = UserFixture.createUser();
        owner = UserFixture.createOwnerUser();
        anotherOwner = UserFixture.createAnotherUser();
        userDetails = new CustomUserDetails(user);
        ownerUserDetails = new CustomUserDetails(owner);
        anotherOwnerDetails = new CustomUserDetails(anotherOwner);
        region = RegionFixture.createOrderEnabledRegion();
        store = StoreFixture.createStore(owner, region);
        product = ProductFixture.createProduct(user, store);
    }

    @Nested
    @DisplayName("메뉴 생성")
    class CreateProductTest {
        @Test
        @DisplayName("성공: 본인 가게에 메뉴를 생성한다")
        void success() {
            // given
            UUID storeId = store.getStoreId();

            ProductCreateReqDto request = new ProductCreateReqDto();
            request.setName("김치찌개");
            request.setPrice(9000);
            request.setDescription("맛있는 김치찌개");
            request.setIsHidden(false);

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.of(store));
            when(productRepository.findMaxDisplayOrder(storeId)).thenReturn(Optional.of(0));

            // when
            ProductResDto result = productService.createProduct(storeId, request, ownerUserDetails);

            // then
            assertNotNull(result);
            assertEquals(result.getName(), request.getName());
            assertEquals(result.getPrice(), request.getPrice());

            verify(storeRepository, times(1)).findByStoreIdAndNotDeleted(storeId);
            verify(productRepository, times(1)).findMaxDisplayOrder(storeId);
            verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("성공: MASTER 권한이면 타인 가게에도 메뉴를 생성할 수 있다")
        void successByMaster() {
            // given
            UUID storeId = store.getStoreId();

            ProductCreateReqDto request = new ProductCreateReqDto();
            request.setName("김치찌개");
            request.setPrice(9000);
            request.setDescription("맛있는 김치찌개");
            request.setIsHidden(false);

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.of(store));
            when(productRepository.findMaxDisplayOrder(storeId)).thenReturn(Optional.of(0));

            // when
            ProductResDto result = productService.createProduct(storeId, request, userDetails);

            // then
            assertNotNull(result);
            assertEquals(result.getName(), request.getName());
            assertEquals(result.getPrice(), request.getPrice());

            verify(productRepository).findMaxDisplayOrder(storeId);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("실패: 가게가 존재하지 않으면 예외가 발생한다")
        void failStoreNotFound() {
            // given
            UUID storeId = store.getStoreId();
            ProductCreateReqDto request = mock(ProductCreateReqDto.class);

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class, () -> productService.createProduct(storeId, request, ownerUserDetails));

            // then
            assertEquals(StoreErrorCode.NOT_FOUND_STORE, exception.getErrorCode());
            verify(productRepository, never()).findMaxDisplayOrder(any());
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("실패: 본인 가게가 아니고 MASTER도 아니면 예외가 발생한다")
        void failNotMyStore() {
            // given
            User anotherUser = UserFixture.createAnotherUser();
            CustomUserDetails anotherUserDetails = new CustomUserDetails(anotherUser);
            UUID storeId = store.getStoreId();

            ProductCreateReqDto request = mock(ProductCreateReqDto.class);

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.of(store));

            // when
            BusinessException exception = assertThrows(BusinessException.class, () -> productService.createProduct(storeId, request, anotherUserDetails));

            // then
            assertEquals(StoreErrorCode.NOT_MY_STORE, exception.getErrorCode());
            verify(productRepository, never()).findMaxDisplayOrder(any());
            verify(productRepository, never()).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("메뉴 수정")
    class UpdateProductTest {
        @Test
        @DisplayName("성공: displayOrder 변경이 없으면 순서 조정 없이 수정된다")
        void success() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = product.getProductId();

            ProductUpdateReqDto request = new ProductUpdateReqDto();
            request.setName("수정 상품명");
            request.setPrice(5000);
            request.setDescription("수정 상품 설명");
            request.setIsHidden(false);
            request.setDisplayOrder(1);

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.of(store));
            when(productRepository.findByProductIdAndStoreId(productId, storeId)).thenReturn(Optional.of(product));

            // when
            ProductUpdateResDto result = productService.updateProduct(storeId, productId, request, ownerUserDetails);

            // then
            assertNotNull(result);
            assertEquals(result.getName(), request.getName());
            assertEquals(result.getPrice(), request.getPrice());
            verify(productRepository, never()).shiftDisplayOrdersUp(any(), anyInt(), anyInt());
            verify(productRepository, never()).shiftDisplayOrdersDown(any(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("성공: displayOrder를 위로 이동하면 shiftDisplayOrdersUp이 호출된다")
        void successShiftUp() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = product.getProductId();

            ProductUpdateReqDto request = new ProductUpdateReqDto();
            request.setDisplayOrder(0);

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.of(store));
            when(productRepository.findByProductIdAndStoreId(productId, storeId)).thenReturn(Optional.of(product));

            // when
            ProductUpdateResDto result = productService.updateProduct(storeId, productId, request, ownerUserDetails);

            // then
            assertNotNull(result);
            assertEquals(result.getDisplayOrder(), request.getDisplayOrder());
            verify(productRepository, times(1)).shiftDisplayOrdersUp(storeId, request.getDisplayOrder(), product.getDisplayOrder());
            verify(productRepository, never()).shiftDisplayOrdersDown(any(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("성공: displayOrder를 아래로 이동하면 shiftDisplayOrdersDown이 호출된다")
        void successShiftDown() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = product.getProductId();

            ProductUpdateReqDto request = new ProductUpdateReqDto();
            request.setDisplayOrder(2);

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.of(store));
            when(productRepository.findByProductIdAndStoreId(productId, storeId)).thenReturn(Optional.of(product));

            // when
            ProductUpdateResDto result = productService.updateProduct(storeId, productId, request, ownerUserDetails);

            // then
            assertNotNull(result);
            assertEquals(result.getDisplayOrder(), request.getDisplayOrder());
            verify(productRepository, never()).shiftDisplayOrdersUp(any(), anyInt(), anyInt());
            verify(productRepository, times(1)).shiftDisplayOrdersDown(storeId, request.getDisplayOrder(),product.getDisplayOrder());
        }

        @Test
        @DisplayName("실패: 가게가 존재하지 않으면 예외가 발생한다")
        void failStoreNotFound() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = product.getProductId();

            ProductUpdateReqDto request = mock(ProductUpdateReqDto.class);

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.empty());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> productService.updateProduct(storeId, productId, request, ownerUserDetails));

            assertEquals(StoreErrorCode.NOT_FOUND_STORE, exception.getErrorCode());
            verify(productRepository, never()).findByProductIdAndStoreId(any(), any());
            verify(productRepository, never()).shiftDisplayOrdersUp(any(), anyInt(), anyInt());
            verify(productRepository, never()).shiftDisplayOrdersDown(any(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("실패: 메뉴가 존재하지 않으면 예외가 발생한다")
        void failProductNotFound() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = product.getProductId();

            Store store = mock(Store.class);

            ProductUpdateReqDto request = mock(ProductUpdateReqDto.class);

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.of(store));
            when(productRepository.findByProductIdAndStoreId(productId, storeId)).thenReturn(Optional.empty());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> productService.updateProduct(storeId, productId, request, ownerUserDetails));

            assertEquals(ProductErrorCode.NOT_FOUND_PRODUCT, exception.getErrorCode());
            verify(productRepository, never()).shiftDisplayOrdersUp(any(), anyInt(), anyInt());
            verify(productRepository, never()).shiftDisplayOrdersDown(any(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("실패: 본인 가게가 아니고 MASTER도 아니면 예외가 발생한다")
        void failNotMyStore() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = product.getProductId();

            ProductUpdateReqDto request = mock(ProductUpdateReqDto.class);

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.of(store));
            when(productRepository.findByProductIdAndStoreId(productId, storeId)).thenReturn(Optional.of(product));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> productService.updateProduct(storeId, productId, request, anotherOwnerDetails));

            assertEquals(StoreErrorCode.NOT_MY_STORE, exception.getErrorCode());
            verify(productRepository, never()).shiftDisplayOrdersUp(any(), anyInt(), anyInt());
            verify(productRepository, never()).shiftDisplayOrdersDown(any(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("실패: 삭제된 메뉴이면 예외가 발생한다")
        void failDeletedProduct() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = product.getProductId();

            ProductUpdateReqDto request = new ProductUpdateReqDto();
            request.setDisplayOrder(3);

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.of(store));
            when(productRepository.findByProductIdAndStoreId(productId, storeId)).thenReturn(Optional.of(product));

            product.delete(owner.getUsername());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> productService.updateProduct(storeId, productId, request, userDetails));

            assertEquals(ProductErrorCode.NOT_MODIFIED, exception.getErrorCode());
            verify(productRepository, never()).shiftDisplayOrdersUp(any(), anyInt(), anyInt());
            verify(productRepository, never()).shiftDisplayOrdersDown(any(), anyInt(), anyInt());
        }
    }

    @Nested
    @DisplayName("메뉴 삭제")
    class DeleteProductTest {
        @Test
        @DisplayName("성공: 본인 가게 메뉴를 삭제하고 이후 displayOrder를 조정한다")
        void success() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = product.getProductId();

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.of(store));
            when(productRepository.findByProductIdAndStoreId(productId, storeId)).thenReturn(Optional.of(product));

            assertNull(product.getSoftDeleteAudit());

            // when
            productService.deleteProduct(storeId, productId, ownerUserDetails);

            // then
            assertNotNull(product.getSoftDeleteAudit());
            verify(productRepository, times(1)).decreaseDisplayOrder(storeId, product.getDisplayOrder());
        }

        @Test
        @DisplayName("실패: 가게가 존재하지 않으면 예외가 발생한다")
        void failStoreNotFound() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = product.getProductId();

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class, () -> productService.deleteProduct(storeId, productId, ownerUserDetails));

            // then
            assertEquals(StoreErrorCode.NOT_FOUND_STORE, exception.getErrorCode());
            verify(productRepository, never()).findByProductIdAndStoreId(any(), any());
            verify(productRepository, never()).decreaseDisplayOrder(any(), anyInt());
        }

        @Test
        @DisplayName("실패: 메뉴가 존재하지 않으면 예외가 발생한다")
        void failProductNotFound() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = UUID.randomUUID();

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.of(store));
            when(productRepository.findByProductIdAndStoreId(productId, storeId)).thenReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class, () -> productService.deleteProduct(storeId, productId, ownerUserDetails));

            // then
            assertEquals(ProductErrorCode.NOT_FOUND_PRODUCT, exception.getErrorCode());
            verify(productRepository, never()).decreaseDisplayOrder(any(), anyInt());
        }

        @Test
        @DisplayName("실패: 본인 가게가 아니고 MASTER도 아니면 예외가 발생한다")
        void failNotMyStore() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = product.getProductId();

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.of(store));
            when(productRepository.findByProductIdAndStoreId(productId, storeId)).thenReturn(Optional.of(product));

            // when
            BusinessException exception = assertThrows(BusinessException.class, () -> productService.deleteProduct(storeId, productId, anotherOwnerDetails));

            // then
            assertEquals(StoreErrorCode.NOT_MY_STORE, exception.getErrorCode());
            verify(productRepository, never()).decreaseDisplayOrder(any(), anyInt());
        }

        @Test
        @DisplayName("실패: 이미 삭제된 메뉴이면 예외가 발생한다")
        void failAlreadyDeleted() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = product.getProductId();

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.of(store));
            when(productRepository.findByProductIdAndStoreId(productId, storeId)).thenReturn(Optional.of(product));

            product.delete(owner.getUsername());

            // when
            BusinessException exception = assertThrows(BusinessException.class, () -> productService.deleteProduct(storeId, productId, ownerUserDetails));

            // then
            assertEquals(ProductErrorCode.ALREADY_DELETED, exception.getErrorCode());
            verify(productRepository, never()).decreaseDisplayOrder(any(), anyInt());
        }
    }

    @Nested
    @DisplayName("메뉴 품절 처리")
    class SoleOutTest {
        @Test
        @DisplayName("성공: 삭제되지 않은 메뉴를 품절 처리한다")
        void success() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = product.getProductId();
            Boolean isHidden = product.getIsHidden();

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.of(store));
            when(productRepository.findByProductIdAndStoreId(productId, storeId)).thenReturn(Optional.of(product));

            // when
            productService.soleOut(storeId, productId, userDetails);

            // then
            assertNotEquals(isHidden, product.getIsHidden());
        }

        @Test
        @DisplayName("실패: 가게가 존재하지 않으면 예외가 발생한다")
        void failStoreNotFound() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = product.getProductId();
            Boolean isHidden = product.getIsHidden();

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class, () -> productService.soleOut(storeId, productId, userDetails));

            // then
            assertEquals(StoreErrorCode.NOT_FOUND_STORE, exception.getErrorCode());
            assertEquals(isHidden, product.getIsHidden());
            verify(productRepository, never()).findByProductIdAndStoreId(any(), any());
        }

        @Test
        @DisplayName("실패: 메뉴가 존재하지 않으면 예외가 발생한다")
        void failProductNotFound() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = product.getProductId();
            Boolean isHidden = product.getIsHidden();

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.of(store));
            when(productRepository.findByProductIdAndStoreId(productId, storeId)).thenReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class, () -> productService.soleOut(storeId, productId, userDetails));

            // then
            assertEquals(ProductErrorCode.NOT_FOUND_PRODUCT, exception.getErrorCode());
            assertEquals(isHidden, product.getIsHidden());
        }

        @Test
        @DisplayName("실패: 삭제된 메뉴면 예외가 발생한다")
        void failDeletedProduct() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = product.getProductId();
            Boolean isHidden = product.getIsHidden();

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.of(store));
            when(productRepository.findByProductIdAndStoreId(productId, storeId)).thenReturn(Optional.of(product));

            product.delete(owner.getUsername());

            // when
            BusinessException exception = assertThrows(BusinessException.class, () -> productService.soleOut(storeId, productId, userDetails));

            // then
            assertEquals(ProductErrorCode.NOT_MODIFIED, exception.getErrorCode());
            assertEquals(isHidden, product.getIsHidden());
        }
    }

    @Nested
    @DisplayName("메뉴 복구")
    class RestoreProductTest {
        @Test
        @DisplayName("성공: 삭제된 메뉴를 복구한다")
        void success() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = product.getProductId();
            Integer displayOrder = product.getDisplayOrder();

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.of(store));
            when(productRepository.findByProductIdAndStoreId(productId, storeId)).thenReturn(Optional.of(product));
            when(productRepository.findMaxDisplayOrder(storeId)).thenReturn(Optional.ofNullable(displayOrder));

            product.delete(owner.getUsername());

            // when
            productService.restoreProduct(storeId, productId, userDetails);

            // then
            assertEquals(product.getDisplayOrder(), displayOrder + 1);
            verify(productRepository, times(1)).findMaxDisplayOrder(storeId);
        }

        @Test
        @DisplayName("실패: 가게가 존재하지 않으면 예외가 발생한다")
        void failStoreNotFound() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = product.getProductId();

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class, () -> productService.restoreProduct(storeId, productId, userDetails));

            // then
            assertEquals(StoreErrorCode.NOT_FOUND_STORE, exception.getErrorCode());
            verify(productRepository, never()).findByProductIdAndStoreId(any(), any());
            verify(productRepository, never()).findMaxDisplayOrder(any());
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
            BusinessException exception = assertThrows(BusinessException.class, () -> productService.restoreProduct(storeId, productId, userDetails));

            // then
            assertEquals(ProductErrorCode.NOT_FOUND_PRODUCT, exception.getErrorCode());
            verify(productRepository, never()).findMaxDisplayOrder(any());
        }

        @Test
        @DisplayName("실패: 삭제되지 않은 메뉴이면 예외가 발생한다")
        void failNotDeleted() {
            // given
            UUID storeId = store.getStoreId();
            UUID productId = product.getProductId();

            when(storeRepository.findByStoreIdAndNotDeleted(storeId)).thenReturn(Optional.of(store));
            when(productRepository.findByProductIdAndStoreId(productId, storeId)).thenReturn(Optional.of(product));

            // when
            BusinessException exception = assertThrows(BusinessException.class, () -> productService.restoreProduct(storeId, productId, userDetails));

            // then
            assertEquals(ProductErrorCode.NOT_DELETED, exception.getErrorCode());
            verify(productRepository, never()).findMaxDisplayOrder(any());
        }
    }
}
