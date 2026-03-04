package com.dfdt.delivery.domain.product.domain.repository;

import com.dfdt.delivery.domain.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByProductIdAndStoreStoreIdAndDeletedAtIsNull(UUID productId, UUID storeId);

    @Query("""
        select coalesce(max(p.displayOrder), 0)
        from Product p
        where p.store.storeId = :storeId
          and p.deletedAt is null
    """)
    Integer findMaxDisplayOrderByStoreId(UUID storeId);
}

