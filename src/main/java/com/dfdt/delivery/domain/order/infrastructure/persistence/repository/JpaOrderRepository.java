package com.dfdt.delivery.domain.order.infrastructure.persistence.repository;

import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.order.domain.repository.OrderRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaOrderRepository extends JpaRepository<Order, UUID> , OrderRepository {
}
