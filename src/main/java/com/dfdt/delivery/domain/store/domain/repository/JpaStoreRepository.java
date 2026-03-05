package com.dfdt.delivery.domain.store.domain.repository;

import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.enums.StoreStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaStoreRepository extends JpaRepository<Store, UUID>, StoreRepository {
    List<Store> findByUser_UsernameOrderByCreateAuditAsc(String username);

    @Query("SELECT s FROM Store s WHERE s.storeId = :storeId AND s.softDeleteAudit.deletedAt IS NULL")
    Optional<Store> findByStoreIdAndNotDeleted(UUID storeId);

    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM Store s JOIN s.categories sc
        WHERE sc.category.categoryId = :categoryId
          AND s.softDeleteAudit.deletedAt IS NULL
    """)
    boolean existsByCategoryIdAndNotDeleted(UUID categoryId);

    @Query("""
        SELECT s
        FROM Store s
        WHERE s.status = :status
          AND s.softDeleteAudit.deletedAt IS NULL
    """)
    List<Store> findStoresByStatusNotDeleted(StoreStatus status);
}
