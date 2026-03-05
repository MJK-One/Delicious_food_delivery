package com.dfdt.delivery.domain.product.application.command;

import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.product.presentation.dto.request.ProductCreateReqDto;
import com.dfdt.delivery.domain.product.presentation.dto.request.ProductUpdateReqDto;
import com.dfdt.delivery.domain.product.presentation.dto.response.ProductResDto;
import com.dfdt.delivery.domain.product.presentation.dto.response.ProductUpdateResDto;

import java.util.UUID;

public interface ProductCommandService {
    ProductResDto createProduct(UUID storeId, ProductCreateReqDto request, CustomUserDetails userDetails);

    ProductUpdateResDto updateProduct(UUID storeId, UUID productId, ProductUpdateReqDto request, CustomUserDetails userDetails);

    void deleteProduct(UUID storeId, UUID productId, CustomUserDetails userDetails);

    void soleOut(UUID storeId, UUID productId, CustomUserDetails userDetails);

    void restoreProduct(UUID storeId, UUID productId, CustomUserDetails userDetails);
}
