package com.dfdt.delivery.domain.store.presentation.dto.response;

import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryIdNameResDto;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreStatusRequestResDto {

    private UUID storeId;
    private UUID regionId;
    private String ownerName;
    private String name;
    private String description;
    private String phone;
    private String addressText;
    private boolean isOpen;
    private String status;
    private OffsetDateTime createdAt;

}