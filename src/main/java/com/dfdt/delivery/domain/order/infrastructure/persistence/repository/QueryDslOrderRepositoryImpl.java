package com.dfdt.delivery.domain.order.infrastructure.persistence.repository;

import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.order.domain.enums.OrderStatus;
import com.dfdt.delivery.domain.order.presentation.dto.OrderResDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dfdt.delivery.domain.order.domain.entity.QOrder.order;

@Repository
@RequiredArgsConstructor
public class QueryDslOrderRepositoryImpl implements QueryDslOrderRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Order> findAllByBuilder(int pageSize, BooleanBuilder builder) {

        return queryFactory
                .selectFrom(order)
                .where(builder)
                .orderBy(
                        order.createdAudit.createdAt.desc()
                        ,order.orderId.desc()) // 최신순 정렬
                .limit(pageSize + 1) // nextCursor를 위해 하나 더 조회
                .fetch();
    }
    @Override
    public OrderResDto.OrderSummaryCount countOrdersByStatus(BooleanBuilder builder) {
        return queryFactory
                .select(Projections.constructor(OrderResDto.OrderSummaryCount.class,

                        // 1. newOrderCount: 결제 완료
                        new CaseBuilder()
                                .when(order.status.eq(OrderStatus.PAID))
                                .then(1).otherwise(0).sum(),

                        // 2. cookingCount: 수락됨
                        new CaseBuilder()
                                .when(order.status.eq(OrderStatus.ACCEPTED))
                                .then(1).otherwise(0).sum(),

                        // 3. deliveryCount: 조리완료 or 배달중
                        new CaseBuilder()
                                .when(order.status.in(OrderStatus.COOKING_DONE, OrderStatus.DELIVERING))
                                .then(1).otherwise(0).sum(),

                        // 4. completedCount: 배달완료 or 확정
                        new CaseBuilder()
                                .when(order.status.in(OrderStatus.DELIVERED, OrderStatus.COMPLETED))
                                .then(1).otherwise(0).sum(),

                        // 5. abortedCount: 거절 or 취소
                        new CaseBuilder()
                                .when(order.status.in(OrderStatus.REJECTED, OrderStatus.CANCELED))
                                .then(1).otherwise(0).sum()
                ))
                .from(order)
                .where(builder)
                .fetchOne();
    }
}
