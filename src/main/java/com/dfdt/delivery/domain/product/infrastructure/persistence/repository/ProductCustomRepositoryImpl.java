package com.dfdt.delivery.domain.product.infrastructure.persistence.repository;

import com.dfdt.delivery.domain.product.domain.repository.ProductCustomRepository;
import com.dfdt.delivery.domain.product.presentation.dto.response.ProductResDto;
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
import java.util.UUID;

import static com.dfdt.delivery.domain.product.entity.QProduct.product;

@Repository
@RequiredArgsConstructor
public class ProductCustomRepositoryImpl implements ProductCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductResDto> searchProducts(Pageable pageable, UUID storeId, String keyword) {
        List<ProductResDto> content = queryFactory
                .select(Projections.constructor(ProductResDto.class,
                        product.productId,
                        product.name,
                        product.description,
                        product.price
                ))
                .from(product)
                .where(
                        storeIdEq(storeId),
                        nameContains(keyword),
                        product.deletedAt.isNull(),
                        product.isHidden.isFalse()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getAllOrderSpecifiers(pageable).toArray(new OrderSpecifier[0]))
                .fetch();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(
                        storeIdEq(storeId),
                        nameContains(keyword),
                        product.deletedAt.isNull(),
                        product.isHidden.isFalse()
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression storeIdEq(UUID storeId) {
        return storeId == null ? null : product.store.storeId.eq(storeId);
    }

    private BooleanExpression nameContains(String keyword) {
        return (keyword == null || keyword.isBlank()) ? null : product.name.containsIgnoreCase(keyword);
    }

    private List<OrderSpecifier<?>> getAllOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (pageable.getSort() != null) {
            for (Sort.Order sortOrder : pageable.getSort()) {
                Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;

                switch (sortOrder.getProperty()) {
                    case "createdAt":
                        orders.add(new OrderSpecifier<>(direction, product.createdAt));
                        break;
                    case "name":
                        orders.add(new OrderSpecifier<>(direction, product.name));
                        break;
                    case "price":
                        orders.add(new OrderSpecifier<>(direction, product.price));
                        break;
                    case "displayOrder":
                        orders.add(new OrderSpecifier<>(direction, product.displayOrder));
                        break;
                    default:
                        break;
                }
            }
        }

        return orders;
    }
}

