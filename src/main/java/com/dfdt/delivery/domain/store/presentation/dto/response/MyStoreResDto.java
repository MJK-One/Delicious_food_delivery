package com.dfdt.delivery.domain.store.presentation.dto.response;

import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryIdNameResDto;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class MyStoreResDto {

    private UUID storeId;
    private String name;
    private List<CategoryIdNameResDto> categories;
    private String addressText;
    private String phone;
    private String description;
    private BigDecimal rating;
    private Integer reviewCount;
    private boolean isOpen;
    private String status;
    private OffsetDateTime createdAt;

    public static MyStoreResDto from(Store store) {
        BigDecimal ratingAvg = BigDecimal.ZERO;
        int reviewCount = 0;

        if (store.getStoreRating() != null) {
            ratingAvg = store.getStoreRating().getRatingAvg() == null
                    ? BigDecimal.ZERO
                    : store.getStoreRating().getRatingAvg();

            reviewCount = store.getStoreRating().getRatingCount() == null
                    ? 0
                    : store.getStoreRating().getRatingCount();
        }

        return MyStoreResDto.builder()
                .storeId(store.getStoreId())
                .name(store.getName())
                .categories(
                        store.getCategories()
                                .stream()
                                .filter(sc -> sc.getSoftDeleteAudit() == null)
                                .map(CategoryIdNameResDto::from)
                                .toList()
                )
                .addressText(store.getAddressText())
                .phone(store.getPhone())
                .description(store.getDescription())
                .rating(ratingAvg)
                .reviewCount(reviewCount)
                .isOpen(store.getIsOpen())
                .status(store.getStatus().name())
                .createdAt(store.getCreateAudit().getCreatedAt())
                .build();
    }
}
