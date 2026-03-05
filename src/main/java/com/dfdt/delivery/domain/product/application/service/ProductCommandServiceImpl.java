package com.dfdt.delivery.domain.product.application.service;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.product.application.command.ProductCommandService;
import com.dfdt.delivery.domain.product.domain.entity.Product;
import com.dfdt.delivery.domain.product.domain.enums.ProductErrorCode;
import com.dfdt.delivery.domain.product.domain.repository.ProductRepository;
import com.dfdt.delivery.domain.product.presentation.dto.request.ProductCreateReqDto;
import com.dfdt.delivery.domain.product.presentation.dto.request.ProductUpdateReqDto;
import com.dfdt.delivery.domain.product.presentation.dto.response.ProductResDto;
import com.dfdt.delivery.domain.product.presentation.dto.response.ProductUpdateResDto;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.enums.StoreErrorCode;
import com.dfdt.delivery.domain.store.domain.repository.StoreRepository;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductCommandServiceImpl implements ProductCommandService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    public ProductResDto createProduct(UUID storeId, ProductCreateReqDto request, CustomUserDetails userDetails) {
        Store store = checkExistStore(storeId);
        checkMyStore(userDetails, store);

        // 존재하는 displayOrder 값 중 (마지막 번호 + 1) 주입
        int maxDisplayOrder = productRepository.findMaxDisplayOrder(storeId).orElse(0);
        Product product = Product.create(request, store, maxDisplayOrder + 1, userDetails.getUsername());
        productRepository.save(product);

        return ProductResDto.from(product);
    }

    public ProductUpdateResDto updateProduct(UUID storeId, UUID productId, ProductUpdateReqDto request, CustomUserDetails userDetails) {
        Store store = checkExistStore(storeId);
        Product product = checkExistProduct(storeId, productId);
        checkMyStore(userDetails, store);
        checkProductDeleted(product);

        // displayOrder 변경 시 다른 상품 순서 조정
        int oldDisplayOrder = product.getDisplayOrder();
        int newDisplayOrder = request.getDisplayOrder();

        if (oldDisplayOrder != newDisplayOrder) {
            if (newDisplayOrder < oldDisplayOrder) {
                // 위로 이동: 새 위치 ~ 기존 위치-1 까지 +1
                productRepository.shiftDisplayOrdersUp(storeId, newDisplayOrder, oldDisplayOrder - 1);
            } else {
                // 아래로 이동: 기존 위치+1 ~ 새 위치 까지 -1
                productRepository.shiftDisplayOrdersDown(storeId, oldDisplayOrder + 1, newDisplayOrder);
            }
        }
        product.update(request, userDetails.getUsername());

        return ProductUpdateResDto.from(product);
    }

    public void deleteProduct(UUID storeId, UUID productId, CustomUserDetails userDetails) {
        Store store = checkExistStore(storeId);
        Product product = checkExistProduct(storeId, productId);
        checkMyStore(userDetails, store);

        // 삭제된 메뉴인지 확인
        if (product.getSoftDeleteAudit() != null) {
            throw new BusinessException(ProductErrorCode.ALREADY_DELETED);
        }

        product.delete(userDetails.getUsername());
        productRepository.decreaseDisplayOrder(storeId, product.getDisplayOrder());
    }

    public void soleOut(UUID storeId, UUID productId, CustomUserDetails userDetails) {
        checkExistStore(storeId);
        Product product = checkExistProduct(storeId, productId);
        checkProductDeleted(product);

        product.soldOut(userDetails.getUsername());
    }

    public void restoreProduct(UUID storeId, UUID productId, CustomUserDetails userDetails) {
        checkExistStore(storeId);
        Product product = checkExistProduct(storeId, productId);

        if (product.getSoftDeleteAudit() == null) {
            throw new BusinessException(ProductErrorCode.NOT_DELETED);
        }

        // 복구 시 기존 displayOrder 값 중 (마지막 번호 + 1) 주입
        int maxDisplayOrder = productRepository.findMaxDisplayOrder(storeId).orElse(0);
        product.restore(maxDisplayOrder, userDetails.getUsername());
    }

    // 해당 가게가 존재하는지 확인
    private Store checkExistStore(UUID storeId) {
        return storeRepository.findByStoreIdAndNotDeleted(storeId)
                .orElseThrow(() -> new BusinessException(StoreErrorCode.NOT_FOUND_STORE));
    }

    // 해당 메뉴가 존재하는지 확인
    private Product checkExistProduct(UUID storeId, UUID productId) {
        return productRepository.findByProductIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.NOT_FOUND_PRODUCT));
    }

    // 본인 소유의 가게만 정보 변경 가능
    private static void checkMyStore(CustomUserDetails userDetails, Store store) {
        if (!store.getUser().getUsername().equals(userDetails.getUsername()) && !userDetails.getRole().equals(UserRole.MASTER)) {
            throw new BusinessException(StoreErrorCode.NOT_MY_STORE);
        }
    }

    // 삭제된 메뉴인지 확인
    private static void checkProductDeleted(Product product) {
        if (product.getSoftDeleteAudit() != null) {
            throw new BusinessException(ProductErrorCode.NOT_MODIFIED);
        }
    }
}

