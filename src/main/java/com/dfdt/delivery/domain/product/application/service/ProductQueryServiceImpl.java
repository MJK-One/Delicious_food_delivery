package com.dfdt.delivery.domain.product.application.service;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.product.application.query.ProductQueryService;
import com.dfdt.delivery.domain.product.domain.entity.Product;
import com.dfdt.delivery.domain.product.domain.enums.ProductErrorCode;
import com.dfdt.delivery.domain.product.domain.repository.JpaProductRepository;
import com.dfdt.delivery.domain.product.domain.repository.ProductCustomRepository;
import com.dfdt.delivery.domain.product.presentation.dto.response.ProductAdminResDto;
import com.dfdt.delivery.domain.product.presentation.dto.response.ProductResDto;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.enums.StoreErrorCode;
import com.dfdt.delivery.domain.store.domain.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductQueryServiceImpl implements ProductQueryService {

    private final JpaProductRepository productRepository;
    private final ProductCustomRepository productCustomRepository;
    private final StoreRepository storeRepository;

    public ProductResDto getProduct(UUID storeId, UUID productId) {
        checkExistStore(storeId);
        Product product = checkExistProduct(storeId, productId);

        return ProductResDto.from(product);
    }

    public Page<ProductResDto> getProducts(UUID storeId, int page, int size, String sortBy, boolean isAsc, String keyword) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductResDto> productResDto = productCustomRepository.searchProducts(pageable, storeId, keyword);
        if (productResDto.getTotalElements() == 0) {
            throw new BusinessException(ProductErrorCode.NOT_FOUND_PRODUCTS);
        }

        return productResDto;
    }

    public Page<ProductAdminResDto> getProductsAdmin(UUID storeId, int page, int size, String sortBy, boolean isAsc, String keyword, Boolean isDeleted) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductAdminResDto> productResDto = productCustomRepository.searchAdminProducts(pageable, storeId, keyword, isDeleted);
        if (productResDto.getTotalElements() == 0) {
            throw new BusinessException(ProductErrorCode.NOT_FOUND_PRODUCTS);
        }

        return productResDto;
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

}

