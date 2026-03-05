package com.dfdt.delivery.domain.product.domain.repository;

import com.dfdt.delivery.domain.product.domain.entity.Product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository{
    Optional<Product> findByProductIdAndStoreId(UUID productId, UUID storeId);

    Optional<Integer> findMaxDisplayOrder(UUID storeId);

    void shiftDisplayOrdersUp(UUID storeId, int start, int end);

    void shiftDisplayOrdersDown(UUID storeId, int start, int end);

    void decreaseDisplayOrder(UUID storeId, int deletedOrder);
}

