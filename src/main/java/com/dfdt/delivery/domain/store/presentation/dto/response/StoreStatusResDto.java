package com.dfdt.delivery.domain.store.presentation.dto.response;

import com.dfdt.delivery.domain.store.domain.entity.Store;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreStatusResDto {

    private UUID storeId;
    private String ownerName;
    private UUID regionId;
    private String name;
    private String address;
    private String phone;
    private String description;
    private boolean isOpen;
    private String status;
    private OffsetDateTime updatedAt;

    public static StoreStatusResDto from(Store store) {
        return new StoreStatusResDto(
                store.getStoreId(),
                store.getUser().getName(),
                store.getRegion().getRegionId(),
                store.getName(),
                store.getAddressText(),
                store.getPhone(),
                store.getDescription(),
                store.getIsOpen(),
                store.getStatus().name(),
                store.getUpdatedAt()
        );
    }
}
