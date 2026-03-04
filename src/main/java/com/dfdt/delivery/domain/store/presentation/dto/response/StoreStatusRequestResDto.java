package com.dfdt.delivery.domain.store.presentation.dto.response;

import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryIdNameResDto;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class StoreStatusRequestResDto {

    private UUID storeId;
    private UUID regionId;
    private String ownerName;
    private String name;
    private List<CategoryIdNameResDto> categories;
    private String description;
    private String phone;
    private String addressText;
    private boolean isOpen;
    private String status;
    private OffsetDateTime createdAt;

    public static StoreStatusRequestResDto from(Store store) {
        return StoreStatusRequestResDto.builder()
                .storeId(store.getStoreId())
                .regionId(store.getRegion().getRegionId())
                .ownerName(store.getUser().getUsername())
                .name(store.getUser().getName())
                .categories(store.getCategories().stream()
                        .map(CategoryIdNameResDto::from)
                        .toList())
                .name(store.getName())
                .description(store.getDescription())
                .phone(store.getPhone())
                .addressText(store.getAddressText())
                .isOpen(store.getIsOpen())
                .status(store.getStatus().name())
                .createdAt(store.getCreateAudit().getCreatedAt())
                .build();
    }
}
