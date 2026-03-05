package com.dfdt.delivery.domain.product.domain.repository;

import com.dfdt.delivery.domain.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface JpaProductRepository extends JpaRepository<Product, UUID>, ProductRepository {
    @Query("""
        SELECT p 
        FROM Product p
        WHERE p.productId = :productId
          AND p.store.storeId = :storeId
    """)
    Optional<Product> findByProductIdAndStoreId(UUID productId, UUID storeId);

    @Query("""
        select coalesce(max(p.displayOrder), 0)
        from Product p
        where p.store.storeId = :storeId
          and p.softDeleteAudit.deletedAt is null
    """)
    Optional<Integer> findMaxDisplayOrder(UUID storeId);

    // 위로 이동: 새 위치 ~ 기존 위치-1 까지 +1
    @Modifying
    @Query("""
        UPDATE Product p
        SET p.displayOrder = p.displayOrder + 1
        WHERE p.store.storeId = :storeId
        AND p.displayOrder BETWEEN :start AND :end
        AND p.softDeleteAudit.deletedAt IS NULL
    """)
    void shiftDisplayOrdersUp(UUID storeId, int start, int end);

    // 아래로 이동: 기존 위치+1 ~ 새 위치 까지 -1
    @Modifying
    @Query("""
        UPDATE Product p
        SET p.displayOrder = p.displayOrder - 1
        WHERE p.store.storeId = :storeId
        AND p.displayOrder BETWEEN :start AND :end
        AND p.softDeleteAudit.deletedAt IS NULL
    """)
    void shiftDisplayOrdersDown(UUID storeId, int start, int end);

    // 삭제 시 displayOrder -1 처리 (deletedOrder 이후)
    @Modifying
    @Query("""
        update Product p
        set p.displayOrder = p.displayOrder - 1
        where p.store.storeId = :storeId
          and p.displayOrder > :deletedOrder
          and p.softDeleteAudit is null
    """)
    void decreaseDisplayOrder(UUID storeId, int deletedOrder);
}

