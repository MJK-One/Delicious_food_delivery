package com.dfdt.delivery.domain.category.domain.repository;

import com.dfdt.delivery.domain.category.domain.entity.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    boolean existsByNameAndCategoryIdNot(String name, UUID categoryId);

    Optional<Integer> findMaxSortOrder();

    List<Category> findActiveCategoriesOrderBySortOrder();

    Optional<Category> findActiveCategoryById(UUID categoryId);

    void shiftSortOrdersUp(int start, int end);

    void shiftSortOrdersDown(int start, int end);

    void decrementSortOrdersAfter(int deletedOrder);

    boolean existsByNameAndDeletedAtIsNull(String name);
}
