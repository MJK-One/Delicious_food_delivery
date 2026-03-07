package com.dfdt.delivery.domain.order.domain.repository;

import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.order.infrastructure.persistence.repository.JpaOrderRepository;
import com.dfdt.delivery.domain.order.infrastructure.persistence.repository.QueryDslOrderRepository;
import com.dfdt.delivery.domain.order.presentation.dto.OrderResDto;
import com.dfdt.delivery.domain.payment.domain.entity.Payment;
import com.querydsl.core.BooleanBuilder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaOrderRepository, QueryDslOrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID orderId);
    Optional<Order> findByIdWithLock(UUID orderId);
    Optional<Payment> findPaymentOrderId(UUID orderId);
    List<Order> findAllByBuilder(int pageSize, BooleanBuilder builder);
    OrderResDto.OrderSummaryCount countOrdersByStatus(BooleanBuilder builder);
}