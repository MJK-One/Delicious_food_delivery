package com.dfdt.delivery.domain.store.infrastructure.persistence.repository;

import com.dfdt.delivery.domain.store.domain.enums.StoreStatus;
import com.dfdt.delivery.domain.store.domain.repository.StoreCustomRepository;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreAdminResDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreResDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreStatusRequestResDto;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.dfdt.delivery.domain.store.domain.entity.QStore.store;
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
                                store.user.name,
                                store.region.regionId,
                                store.name,
                                store.addressText,
                                store.phone,
                                store.description,
                                store.isOpen,
                                store.status.stringValue(),
                                storeRating.ratingAvg.coalesce(BigDecimal.ZERO),
                                storeRating.ratingCount.coalesce(0),
                                store.createAudit.createdAt
                        )
                )
                .from(store)
                .leftJoin(storeRating).on(storeRating.store.eq(store))
                .where(
                        nameContains(name),
                        categoryIdIn(categoryId),
                        store.softDeleteAudit.deletedAt.isNull(),
                        store.region.isOrderEnabled,
                        store.status.eq(StoreStatus.APPROVED)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getAllOrderSpecifiers(pageable).toArray(new OrderSpecifier[0]))
                .fetch();

        Long total = queryFactory
                .select(store.count())
                .from(store)
                .where(
                        nameContains(name),
                        categoryIdIn(categoryId),
                        store.softDeleteAudit.deletedAt.isNull(),
                        store.region.isOrderEnabled,
                        store.status.eq(StoreStatus.APPROVED)

                )
                .fetchOne();

        return new PageImpl<>(fetch, pageable, total);
    }

    @Override
    public Page<StoreAdminResDto> searchStoresAdmin(Pageable pageable, UUID categoryId, String name, Boolean isDeleted) {

        List<StoreAdminResDto> fetch = queryFactory
                .select(Projections.constructor(StoreAdminResDto.class,
                                store.storeId,
                                store.user.name,
                                store.region.regionId,
                                store.name,
                                store.addressText,
                                store.phone,
                                store.description,
                                store.isOpen,
                                store.status.stringValue(),
                                storeRating.ratingAvg.coalesce(BigDecimal.ZERO),
                                storeRating.ratingCount.coalesce(0),
                                store.createAudit.createdAt,
                                store.updateAudit.updatedAt,
                                store.softDeleteAudit.deletedAt
                        )
                )
                .from(store)
                .leftJoin(storeRating).on(storeRating.store.eq(store))
                .where(
                        nameContains(name),
                        categoryIdIn(categoryId),
                        isDeletedEq(isDeleted)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getAllOrderSpecifiers(pageable).toArray(new OrderSpecifier[0]))
                .fetch();

        Long total = queryFactory
                .select(store.count())
                .from(store)
                .leftJoin(storeRating).on(storeRating.store.eq(store))
                .where(
                        nameContains(name),
                        categoryIdIn(categoryId),
                        isDeletedEq(isDeleted)
                )
                .fetchOne();

        return new PageImpl<>(fetch, pageable, total);
    }

    @Override
    public Page<StoreStatusRequestResDto> searchRequestStores(Pageable pageable, StoreStatus requested) {

        List<StoreStatusRequestResDto> fetch = queryFactory
                .select(Projections.fields(StoreStatusRequestResDto.class,
                                store.storeId,
                                store.region.regionId,
                                store.user.name.as("ownerName"),
                                store.name,
                                store.description,
                                store.phone,
                                store.addressText,
                                store.isOpen,
                                store.status.stringValue().as("status"),
                                store.createAudit.createdAt
                        )
                )
                .from(store)
                .where(store.status.eq(StoreStatus.REQUESTED))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getAllOrderSpecifiers(pageable).toArray(new OrderSpecifier[0]))
                .fetch();

        Long total = queryFactory
                .select(store.count())
                .from(store)
                .where(store.status.eq(StoreStatus.REQUESTED))
                .fetchOne();

        return new PageImpl<>(fetch, pageable, total);
    }

    private BooleanExpression nameContains(String name) {
        return (name == null || name.isBlank()) ? null : store.name.containsIgnoreCase(name);
    }

    private BooleanExpression categoryIdIn(UUID categoryId) {
        return categoryId == null ? null : store.categories.any().category.categoryId.eq(categoryId);
    }

    private BooleanExpression isDeletedEq(Boolean isDeleted) {
        return isDeleted ? store.softDeleteAudit.deletedAt.isNotNull() : store.softDeleteAudit.deletedAt.isNull();
    }

    private List<OrderSpecifier<?>> getAllOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (pageable.getSort() != null) {
            for (Sort.Order sortOrder : pageable.getSort()) {
                Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;

                switch (sortOrder.getProperty()) {
                    case "name":
                        orders.add(new OrderSpecifier<>(direction, store.name));
                        break;
                    default:
                        orders.add(new OrderSpecifier<>(direction, store.createAudit.createdAt));
                        break;
                }
            }
        }
        return orders;
    }
}