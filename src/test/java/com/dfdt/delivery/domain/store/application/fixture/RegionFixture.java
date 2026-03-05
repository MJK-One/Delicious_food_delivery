package com.dfdt.delivery.domain.store.application.fixture;

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

}

