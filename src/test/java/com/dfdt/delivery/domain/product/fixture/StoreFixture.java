package com.dfdt.delivery.domain.product.fixture;


import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.domain.region.domain.entity.Region;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.enums.StoreStatus;
import com.dfdt.delivery.domain.user.domain.entity.User;

import java.util.UUID;

public class StoreFixture {

    public static Store createStore(User user, Region region) {
        return Store.builder()
                .storeId(UUID.randomUUID())
                .name("테스트 가게")
                .phone("010-1234-5678")
                .description("테스트 설명")
                .addressText("광화문")
                .isOpen(true)
                .status(StoreStatus.APPROVED)
                .user(user)
                .region(region)
                .createAudit(CreateAudit.now("테스트 유저"))
                .build();
    }

    public static Store createNoIdStore(User user, Region region) {
        return Store.builder()
                .name("테스트 가게")
                .phone("010-1234-5678")
                .description("테스트 설명")
                .addressText("광화문")
                .isOpen(true)
                .status(StoreStatus.APPROVED)
                .user(user)
                .region(region)
                .createAudit(CreateAudit.now("테스트 유저"))
                .build();
    }
}