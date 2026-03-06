package com.dfdt.delivery.domain.payment.infrastructure.persistence.repository;

import com.dfdt.delivery.domain.payment.domain.entity.Payment;
import com.dfdt.delivery.domain.payment.domain.enums.PaymentStatus;
import com.dfdt.delivery.domain.payment.domain.repository.PaymentCustomRepository;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentHistorySearchReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentListSearchReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentHistoryResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentListItemResDto;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.dfdt.delivery.domain.payment.domain.entity.QPayment.payment;
import static com.dfdt.delivery.domain.payment.domain.entity.QPaymentStatusHistory.paymentStatusHistory;
import static com.dfdt.delivery.domain.order.domain.entity.QOrder.order;

@Repository
@RequiredArgsConstructor
public class PaymentCustomRepositoryImpl implements PaymentCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Payment> findByIdWithRoleCheck(UUID paymentId, String username, UserRole role) {
        var query = queryFactory.selectFrom(payment)
                .join(order).on(payment.orderId.eq(order.orderId));

        if (role == UserRole.OWNER) {
            query.join(order.store)
                    .join(order.store.user)
                    .where(
                            payment.paymentId.eq(paymentId),
                            order.store.user.username.eq(username)
                    );
        } else if (role == UserRole.CUSTOMER) {
            query.join(order.user)
                    .where(
                            payment.paymentId.eq(paymentId),
                            order.user.username.eq(username)
                    );
        } else { // MASTER
            query.where(payment.paymentId.eq(paymentId));
        }

        return Optional.ofNullable(query.fetchOne());
    }

    @Override
    public Page<PaymentListItemResDto> searchPayments(
            PaymentListSearchReqDto reqDto,
            Pageable pageable,
            String username,
            UserRole role
    ) {

        var contentQuery = queryFactory
                .select(Projections.constructor(PaymentListItemResDto.class,
                        payment.paymentId,
                        payment.orderId,
                        payment.paymentStatus,
                        payment.paidAt
                                .coalesce(payment.failedAt)
                                .coalesce(payment.canceledAt)
                                .coalesce(payment.createAudit.createdAt),
                        payment.amount.longValue(),
                        payment.hiddenAt.isNotNull()
                ))
                .from(payment)
                .join(order).on(payment.orderId.eq(order.orderId));

        var countQuery = queryFactory
                .select(payment.count())
                .from(payment)
                .join(order).on(payment.orderId.eq(order.orderId));

        // 권한별 join + filter
        if (role == UserRole.CUSTOMER) {

            contentQuery.join(order.user)
                    .where(order.user.username.eq(username));

            countQuery.join(order.user)
                    .where(order.user.username.eq(username));

        } else if (role == UserRole.OWNER) {

            contentQuery.join(order.store)
                    .join(order.store.user)
                    .where(order.store.user.username.eq(username));

            countQuery.join(order.store)
                    .join(order.store.user)
                    .where(order.store.user.username.eq(username));
        }

        List<PaymentListItemResDto> content = contentQuery
                .where(
                        orderIdEq(reqDto.getOrderId()),
                        statusEq(reqDto.getPaymentStatus()),
                        betweenDates(reqDto.getFrom(), reqDto.getTo()),
                        hiddenFilter(role, reqDto.getIncludeHidden()),
                        isDeletedFilter(reqDto.getIsDeleted())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(payment.createAudit.createdAt.desc())
                .fetch();

        Long total = countQuery
                .where(
                        orderIdEq(reqDto.getOrderId()),
                        statusEq(reqDto.getPaymentStatus()),
                        betweenDates(reqDto.getFrom(), reqDto.getTo()),
                        hiddenFilter(role, reqDto.getIncludeHidden()),
                        isDeletedFilter(reqDto.getIsDeleted())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    @Override
    public Page<PaymentHistoryResDto> searchPaymentHistory(PaymentHistorySearchReqDto reqDto, Pageable pageable) {

        List<PaymentHistoryResDto> content = queryFactory
                .select(Projections.constructor(PaymentHistoryResDto.class,
                        paymentStatusHistory.paymentStatusHistoryId,
                        paymentStatusHistory.paymentId,
                        paymentStatusHistory.orderId,
                        paymentStatusHistory.changedBy,
                        paymentStatusHistory.fromStatus,
                        paymentStatusHistory.toStatus,
                        paymentStatusHistory.changeReason,
                        paymentStatusHistory.createAudit.createdAt
                ))
                .from(paymentStatusHistory)
                .where(
                        historyPaymentIdEq(reqDto.getPaymentId()),
                        historyOrderIdEq(reqDto.getOrderId()),
                        historyChangedByEq(reqDto.getChangedBy())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(paymentStatusHistory.createAudit.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(paymentStatusHistory.count())
                .from(paymentStatusHistory)
                .where(
                        historyPaymentIdEq(reqDto.getPaymentId()),
                        historyOrderIdEq(reqDto.getOrderId()),
                        historyChangedByEq(reqDto.getChangedBy())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression hiddenFilter(UserRole role, Boolean includeHidden) {
        if (role == UserRole.MASTER && Boolean.TRUE.equals(includeHidden)) return null;
        if (role == UserRole.CUSTOMER) return payment.hiddenAt.isNull(); 
        return null; 
    }

    private BooleanExpression orderIdEq(UUID orderId) {
        return orderId != null ? payment.orderId.eq(orderId) : null;
    }

    private BooleanExpression statusEq(PaymentStatus status) {
        return status != null ? payment.paymentStatus.eq(status) : null;
    }

    private BooleanExpression betweenDates(OffsetDateTime from, OffsetDateTime to) {
        if (from == null && to == null) return null;
        if (from != null && to != null) return payment.createAudit.createdAt.between(from, to);
        if (from != null) return payment.createAudit.createdAt.goe(from);
        return payment.createAudit.createdAt.loe(to);
    }

    private BooleanExpression isDeletedFilter(Boolean isDeleted) {
        return Boolean.TRUE.equals(isDeleted) ? payment.softDeleteAudit.deletedAt.isNotNull() : payment.softDeleteAudit.deletedAt.isNull();
    }

    private BooleanExpression historyPaymentIdEq(UUID paymentId) {
        return paymentId != null ? paymentStatusHistory.paymentId.eq(paymentId) : null;
    }

    private BooleanExpression historyOrderIdEq(UUID orderId) {
        return orderId != null ? paymentStatusHistory.orderId.eq(orderId) : null;
    }

    private BooleanExpression historyChangedByEq(String changedBy) {
        return (changedBy != null && !changedBy.isBlank()) ? paymentStatusHistory.changedBy.eq(changedBy) : null;
    }
}
