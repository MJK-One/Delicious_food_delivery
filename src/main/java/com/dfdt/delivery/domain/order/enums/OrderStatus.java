package com.dfdt.delivery.domain.order.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING, PAID, ACCEPTED, REJECTED,
    COOKING_DONE, DELIVERING, DELIVERED,
    COMPLETED, CANCELED
}