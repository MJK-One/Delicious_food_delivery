package com.dfdt.delivery.domain.store.presentation.dto.response;

import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.entity.StoreRating;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class StoreAdminResDto {

    private UUID storeId;
    private String ownerName;
    private UUID regionId;
    private String name;
    private String addressText;
    private String phone;
    private String description;
    private boolean isOpen;
    private String status;
    private BigDecimal rating;
    private int reviewCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;

    public static StoreAdminResDto from(Store store, StoreRating rating) {
        BigDecimal ratingAvg = BigDecimal.ZERO;
        int reviewCount = 0;

        if (rating != null) {
            ratingAvg = rating.getRatingAvg() == null
                    ? BigDecimal.ZERO
                    : rating.getRatingAvg();

            reviewCount = rating.getRatingCount() == null
                    ? 0
                    : rating.getRatingCount();
        }

        return new StoreAdminResDto(
                store.getStoreId(),
                store.getUser().getName(),
                store.getRegion().getRegionId(),
                store.getName(),
                store.getAddressText(),
                store.getPhone(),
                store.getDescription(),
                store.getIsOpen(),
                store.getStatus().name(),
                ratingAvg,
                reviewCount,
                store.getCreateAudit().getCreatedAt(),
                store.getUpdateAudit().getUpdatedAt(),
                store.getSoftDeleteAudit().getDeletedAt()
        );
    }
}
