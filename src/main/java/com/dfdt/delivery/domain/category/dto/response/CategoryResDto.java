package com.dfdt.delivery.domain.category.dto.response;

import com.dfdt.delivery.domain.category.entity.Category;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryResDto {

    private UUID categoryId;
    private String name;
    private Integer sortOrder;
    private Boolean isActive;

    public static CategoryResDto from(Category category) {
        return new CategoryResDto(
                category.getCategoryId(),
                category.getName(),
                category.getSortOrder(),
                category.getIsActive()
        );
    }
}
