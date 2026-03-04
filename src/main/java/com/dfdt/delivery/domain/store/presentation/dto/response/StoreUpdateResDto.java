package com.dfdt.delivery.domain.store.presentation.dto.response;

import com.dfdt.delivery.domain.store.domain.entity.Store;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreUpdateResDto {

    private UUID storeId;
    private String name;
    private String phone;
    private String description;
    private String addressText;
    private boolean isOpen;
    private String status;
    private OffsetDateTime updatedAt;

    public static StoreUpdateResDto from(Store store) {
        return new StoreUpdateResDto(
                store.getStoreId(),
                store.getName(),
                store.getPhone(),
                store.getDescription(),
                store.getAddressText(),
                store.getIsOpen(),
                store.getStatus().name(),
                store.getUpdateAudit().getUpdatedAt()
        );
    }
}