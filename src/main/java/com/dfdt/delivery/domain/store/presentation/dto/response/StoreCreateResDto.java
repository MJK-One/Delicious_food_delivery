package com.dfdt.delivery.domain.store.presentation.dto.response;

import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.enums.StoreStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreCreateResDto {

    private UUID storeId;
    private String status;
    private OffsetDateTime createdAt;

    public static StoreCreateResDto from(Store store) {
        return new StoreCreateResDto(
                store.getStoreId(),
                StoreStatus.REQUESTED.name(),
                store.getCreatedAt()
        );
    }
}