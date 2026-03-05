package com.dfdt.delivery.domain.product.domain.repository;

import com.dfdt.delivery.domain.product.presentation.dto.response.ProductAdminResDto;
import com.dfdt.delivery.domain.product.presentation.dto.response.ProductResDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductCustomRepository {
    Page<ProductResDto> searchProducts(Pageable pageable, UUID storeId, String keyword);

    Page<ProductAdminResDto> searchAdminProducts(Pageable pageable, UUID storeId, String keyword, Boolean isDeleted);
}

