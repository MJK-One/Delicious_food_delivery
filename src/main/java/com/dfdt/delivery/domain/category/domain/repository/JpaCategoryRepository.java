package com.dfdt.delivery.domain.category.domain.repository;

import com.dfdt.delivery.domain.category.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCategoryRepository extends JpaRepository<Category, UUID>, CategoryRepository {
    boolean existsByNameAndCategoryIdNot(String name, UUID categoryId);

    // 최대 sortOrder 조회
    @Query("SELECT COALESCE(MAX(c.sortOrder), 0) FROM Category c WHERE c.softDeleteAudit.deletedAt IS NULL")
    Optional<Integer> findMaxSortOrder();

    @Query("""
        SELECT c
        FROM Category c
        WHERE c.softDeleteAudit.deletedAt IS NULL
        ORDER BY c.sortOrder
    """)
    List<Category> findActiveCategoriesOrderBySortOrder();

    // categoryId로 삭제되지 않은 단일 카테고리 조회
    @Query("""
        SELECT c
        FROM Category c
        WHERE c.categoryId = :categoryId
          AND c.softDeleteAudit.deletedAt IS NULL
    """)
    Optional<Category> findActiveCategoryById(UUID categoryId);

    // 위로 이동: 새 위치 ~ 기존 위치-1 까지 +1
    @Modifying
    @Query("UPDATE Category c " +
            "SET c.sortOrder = c.sortOrder + 1 " +
            "WHERE c.sortOrder BETWEEN :start AND :end " +
            "AND c.softDeleteAudit.deletedAt IS NULL")
    void shiftSortOrdersUp(int start, int end);

    // 아래로 이동: 기존 위치+1 ~ 새 위치 까지 -1
    @Modifying
    @Query("UPDATE Category c " +
            "SET c.sortOrder = c.sortOrder - 1 " +
            "WHERE c.sortOrder BETWEEN :start AND :end " +
            "AND c.softDeleteAudit.deletedAt IS NULL")
    void shiftSortOrdersDown(int start, int end);

    // 삭제 시 sortOrder -1 처리 (deletedOrder 이후)
    @Modifying
    @Query("UPDATE Category c SET c.sortOrder = c.sortOrder - 1 " +
            "WHERE c.sortOrder > :deletedOrder AND c.softDeleteAudit.deletedAt IS NULL")
    void decrementSortOrdersAfter(int deletedOrder);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Category c " +
            "WHERE c.name = :name AND c.softDeleteAudit.deletedAt IS NULL")
    boolean existsByNameAndDeletedAtIsNull(String name);
}
