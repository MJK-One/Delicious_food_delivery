package com.dfdt.delivery.domain.store.presentation.dto.response;

import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.entity.StoreRating;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreResDto {

    private UUID storeId;
    private String ownerName;
    private UUID regionId;
    private String name;
    private String address;
    private String phone;
    private String description;
    private boolean isOpen;
    private BigDecimal rating;
    private int reviewCount;

    public static StoreResDto from(Store store, StoreRating rating) {
        BigDecimal ratingAvg = rating.getRatingAvg() == null
                ? BigDecimal.ZERO
                : rating.getRatingAvg();

        int ratingCount = rating.getRatingCount() == null
                ? 0
                : rating.getRatingCount();

        return new StoreResDto(
                store.getStoreId(),
                store.getUser().getName(),
                store.getRegion().getRegionId(),
                store.getName(),
                store.getAddressText(),
                store.getPhone(),
                store.getDescription(),
                store.getIsOpen(),
                ratingAvg,
                ratingCount
        );
    }
}
