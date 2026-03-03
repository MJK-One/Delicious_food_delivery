package com.dfdt.delivery.domain.order.domain.repository;

import com.dfdt.delivery.domain.order.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}