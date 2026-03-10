package com.dfdt.delivery.domain.ai.infrastructure.persistence;

import com.dfdt.delivery.domain.ai.application.dto.AiLogSummaryResult;
import com.dfdt.delivery.domain.ai.domain.repository.AiLogCustomRepository;
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

import static com.dfdt.delivery.domain.ai.domain.entity.QAiLogEntity.aiLogEntity;

@Repository
@RequiredArgsConstructor
public class AiLogCustomRepositoryImpl implements AiLogCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AiLogSummaryResult> searchAiLogs(
            UUID storeId,
            UUID productId,
            Boolean isApplied,
            Boolean isSuccess,
            Pageable pageable
    ) {
        List<AiLogSummaryResult> content = queryFactory
                .select(Projections.constructor(AiLogSummaryResult.class,
                        aiLogEntity.aiLogId,
                        aiLogEntity.productId,
                        aiLogEntity.requestedBy,
                        aiLogEntity.requestType,
                        aiLogEntity.tone,
                        aiLogEntity.isSuccess,
                        aiLogEntity.isApplied,
                        aiLogEntity.appliedAt,
                        aiLogEntity.responseText,
                        aiLogEntity.createAudit.createdAt
                ))
                .from(aiLogEntity)
                .where(
                        aiLogEntity.storeId.eq(storeId),
                        aiLogEntity.softDeleteAudit.deletedAt.isNull(),
                        productIdEq(productId),
                        isAppliedEq(isApplied),
                        isSuccessEq(isSuccess)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(buildOrderSpecifiers(pageable))
                .fetch();

        Long total = queryFactory
                .select(aiLogEntity.count())
                .from(aiLogEntity)
                .where(
                        aiLogEntity.storeId.eq(storeId),
                        aiLogEntity.softDeleteAudit.deletedAt.isNull(),
                        productIdEq(productId),
                        isAppliedEq(isApplied),
                        isSuccessEq(isSuccess)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression productIdEq(UUID productId) {
        return productId == null ? null : aiLogEntity.productId.eq(productId);
    }

    private BooleanExpression isAppliedEq(Boolean isApplied) {
        return isApplied == null ? null : aiLogEntity.isApplied.eq(isApplied);
    }

    private BooleanExpression isSuccessEq(Boolean isSuccess) {
        return isSuccess == null ? null : aiLogEntity.isSuccess.eq(isSuccess);
    }

    private OrderSpecifier<?>[] buildOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (pageable.getSort() != null) {
            for (Sort.Order sortOrder : pageable.getSort()) {
                Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;
                switch (sortOrder.getProperty()) {
                    case "appliedAt":
                        orders.add(new OrderSpecifier<>(direction, aiLogEntity.appliedAt));
                        break;
                    default:
                        orders.add(new OrderSpecifier<>(direction, aiLogEntity.createAudit.createdAt));
                        break;
                }
            }
        }

        if (orders.isEmpty()) {
            orders.add(new OrderSpecifier<>(Order.DESC, aiLogEntity.createAudit.createdAt));
        }

        return orders.toArray(new OrderSpecifier[0]);
    }
}