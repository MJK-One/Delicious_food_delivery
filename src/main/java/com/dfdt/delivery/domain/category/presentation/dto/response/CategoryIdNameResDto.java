package com.dfdt.delivery.domain.category.presentation.dto.response;

import com.dfdt.delivery.domain.store.domain.entity.StoreCategory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryIdNameResDto {

    private UUID categoryId;
    private String name;

    public static CategoryIdNameResDto from(StoreCategory sc) {
        return new CategoryIdNameResDto(
                sc.getCategory().getCategoryId(),
                sc.getCategory().getName()
        );
    }
}
