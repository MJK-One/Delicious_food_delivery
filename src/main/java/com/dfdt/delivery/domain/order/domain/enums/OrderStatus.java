package com.dfdt.delivery.domain.order.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING, PAID, ACCEPTED,
    COOKING_DONE, DELIVERING, DELIVERED,
    COMPLETED, REJECTED, CANCELED, HIDDEN;
}