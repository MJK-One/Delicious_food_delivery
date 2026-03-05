package com.dfdt.delivery.domain.product.application.query;

import com.dfdt.delivery.domain.product.presentation.dto.response.ProductAdminResDto;
import com.dfdt.delivery.domain.product.presentation.dto.response.ProductResDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ProductQueryService {
    ProductResDto getProduct(UUID storeId, UUID productId);

    Page<ProductResDto> getProducts(UUID storeId, int page, int size, String sortBy, boolean isAsc, String keyword);

    Page<ProductAdminResDto> getProductsAdmin(UUID storeId, int page, int size, String sortBy, boolean isAsc, String keyword, Boolean isDeleted);
}
