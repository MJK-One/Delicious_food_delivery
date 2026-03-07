package com.dfdt.delivery.domain.order.infrastructure.persistence.repository;

import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.order.presentation.dto.OrderResDto;
import com.querydsl.core.BooleanBuilder;

import java.util.List;

public interface QueryDslOrderRepository {
    List<Order> findAllByBuilder(int pageSize, BooleanBuilder builder);
    OrderResDto.OrderSummaryCount countOrdersByStatus(BooleanBuilder builder);
}
