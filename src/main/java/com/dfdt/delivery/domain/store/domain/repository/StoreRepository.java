package com.dfdt.delivery.domain.store.domain.repository;

import com.dfdt.delivery.domain.store.domain.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {
    Optional<Store> findByStoreIdAndDeletedAtIsNull(UUID id);

    List<Store> findByUsername(String username);

    boolean existsByCategoryIdAndDeletedAtIsNull(UUID categoryId);
}
