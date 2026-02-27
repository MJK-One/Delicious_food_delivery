package com.dfdt.delivery.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    CUSTOMER("고객"),
    OWNER("가게 주인"),
    MASTER("전체 관리자");

    private final String description;
}