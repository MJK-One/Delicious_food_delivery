package com.dfdt.delivery.domain.store.infrastructure.persistence.repository;

import com.dfdt.delivery.domain.store.domain.repository.StoreCustomRepository;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreResDto;
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

import static com.dfdt.delivery.domain.category.domain.entity.QCategory.category;
import static com.dfdt.delivery.domain.store.domain.entity.QStore.store;
import static com.dfdt.delivery.domain.store.domain.entity.QStoreCategory.storeCategory;
import static com.dfdt.delivery.domain.store.domain.entity.QStoreRating.storeRating;

@Repository
@RequiredArgsConstructor
public class StoreCustomRepositoryImpl implements StoreCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<StoreResDto> searchStores(Pageable pageable, UUID categoryId, String name) {

        List<StoreResDto> fetch = queryFactory
                .select(Projections.constructor(StoreResDto.class,
                                store.storeId,
                                null,   // 가게 소유자는 search 시 제외
                                store.region.regionId,
                                store.name,
                                store.addressText,
                                store.phone,
                                store.description,
                                store.isOpen,
                                storeRating.ratingAvg,
                                storeRating.ratingCount
                        )
                )
                .from(store)
                .leftJoin(store.categories, storeCategory)
                .leftJoin(storeCategory.category, category)
                .leftJoin(storeRating).on(storeRating.store.eq(store))
                .where(
                        nameContains(name),
                        categoryIdEq(categoryId),
                        store.deletedAt.isNull()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getAllOrderSpecifiers(pageable).toArray(new OrderSpecifier[0]))
                .fetch();

        Long total = queryFactory
                .select(store.count())
                .from(store)
                .leftJoin(store.categories, storeCategory)
                .leftJoin(storeCategory.category, category)
                .where(
                        nameContains(name),
                        categoryIdEq(categoryId),
                        store.deletedAt.isNull()
                )
                .fetchOne();

        return new PageImpl<>(fetch, pageable, total);
    }

    private BooleanExpression nameContains(String name) {
        return (name == null || name.isBlank()) ? null
                : store.name.containsIgnoreCase(name);
    }

    private BooleanExpression categoryIdEq(UUID categoryId) {
        return categoryId == null ? null : category.categoryId.eq(categoryId);
    }

    private List<OrderSpecifier<?>> getAllOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (pageable.getSort() != null) {
            for (Sort.Order sortOrder : pageable.getSort()) {
                Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;

                switch (sortOrder.getProperty()) {
                    case "createdAt":
                         orders.add(new OrderSpecifier<>(direction, store.createdAt));
                        break;
                    case "name":
                        orders.add(new OrderSpecifier<>(direction, store.name));
                        break;
                    default:
                        break;
                }
            }
        }
        return orders;
    }
}