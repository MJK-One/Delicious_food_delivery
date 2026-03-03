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
    private List<CategoryIdNameResDto> category;
    private BigDecimal rating;
    private Integer reviewCount;
    private boolean isOpen;
    private OffsetDateTime createdAt;

    public static MyStoreResDto from(Store store) {
        return MyStoreResDto.builder()
                .storeId(store.getStoreId())
                .name(store.getName())
                .category(
                        store.getCategories()
                                .stream()
                                .map(CategoryIdNameResDto::from)
                                .toList()
                )
                .rating(store.getStoreRating().getRatingAvg())
                .reviewCount(store.getStoreRating().getRatingCount())
                .isOpen(store.getIsOpen())
                .createdAt(store.getCreatedAt())
                .build();
    }
}
