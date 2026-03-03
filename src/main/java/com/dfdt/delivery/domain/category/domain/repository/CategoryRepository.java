package com.dfdt.delivery.domain.category.domain.repository;

import com.dfdt.delivery.domain.category.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findAllByDeletedAtIsNull();

    Optional<Category> findByCategoryIdAndDeletedAtIsNull(UUID categoryId);
}
