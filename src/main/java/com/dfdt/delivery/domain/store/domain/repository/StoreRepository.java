package com.dfdt.delivery.domain.store.domain.repository;

import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.enums.StoreStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository {
    List<Store> findByUser_UsernameOrderByCreateAuditAsc(String username);

    Optional<Store> findByStoreIdAndNotDeleted(UUID storeId);

    boolean existsByCategoryIdAndNotDeleted(UUID categoryId);

    List<Store> findStoresByStatusNotDeleted(StoreStatus status);
}
