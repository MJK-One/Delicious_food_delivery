package com.dfdt.delivery.domain.store.application.fixture;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.domain.region.domain.entity.Region;

import java.util.UUID;

public class RegionFixture {

    public static Region createOrderEnabledRegion() {
        return new Region(
                UUID.randomUUID(),
                "서울",
                (short) 1,
                "1101101",
                true,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static Region createNoIdRegion() {
        return Region.builder()
                .name("서울")
                .level((short) 1)
                .code("1101000")
                .isOrderEnabled(true)
                .isOrderEnabled(true)
                .createAudit(CreateAudit.now("test"))
                .build();
    }

}

