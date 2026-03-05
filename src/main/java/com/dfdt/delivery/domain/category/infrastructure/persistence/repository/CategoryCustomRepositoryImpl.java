package com.dfdt.delivery.domain.category.infrastructure.persistence.repository;

import com.dfdt.delivery.domain.category.domain.repository.CategoryCustomRepository;
import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryAdminResDto;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.dfdt.delivery.domain.category.domain.entity.QCategory.category;

@Repository
@RequiredArgsConstructor
public class CategoryCustomRepositoryImpl implements CategoryCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CategoryAdminResDto> searchCategoriesAdmin(Pageable pageable, String name, Boolean isDeleted) {
        List<CategoryAdminResDto> fetch = queryFactory
                .select(Projections.fields(CategoryAdminResDto.class,
                                category.categoryId,
                                category.name,
                                category.description,
                                category.sortOrder,
                                category.isActive,
                                category.createAudit.createdAt,
                                category.updateAudit.updatedAt,
                                category.softDeleteAudit.deletedAt
                        )
                )
                .from(category)
                .where(
                    nameContains(name),
                        isDeletedEq(isDeleted)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getAllOrderSpecifiers(pageable).toArray(new OrderSpecifier[0]))
                .fetch();

        Long total = queryFactory
                .select(category.count())
                .from(category)
                .fetchOne();

        return new PageImpl<>(fetch, pageable, total);
    }

    private BooleanExpression nameContains(String name) {
        return (name == null || name.isBlank()) ? null : category.name.containsIgnoreCase(name);
    }

    private BooleanExpression isDeletedEq(Boolean isDeleted) {
        return isDeleted ? category.softDeleteAudit.deletedAt.isNotNull() : category.softDeleteAudit.deletedAt.isNull();
    }

    private List<OrderSpecifier<?>> getAllOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (pageable.getSort() != null) {
            for (Sort.Order sortOrder : pageable.getSort()) {
                Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;

                switch (sortOrder.getProperty()) {
                    case "createdAt":
                        orders.add(new OrderSpecifier<>(direction, category.createAudit.createdAt));
                        break;
                    case "name":
                        orders.add(new OrderSpecifier<>(direction, category.name));
                        break;
                    default:
                        orders.add(new OrderSpecifier<>(direction, category.sortOrder));
                        break;
                }
            }
        }
        return orders;
    }
}
