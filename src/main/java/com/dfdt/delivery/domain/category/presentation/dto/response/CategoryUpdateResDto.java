package com.dfdt.delivery.domain.category.presentation.dto.response;

import com.dfdt.delivery.domain.category.domain.entity.Category;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryUpdateResDto {

    private UUID categoryId;
    private String name;
    private String description;
    private Integer sortOrder;
    private Boolean isActive;
    private OffsetDateTime updateAt;

    public static CategoryUpdateResDto from(Category category) {
        return new CategoryUpdateResDto(
                category.getCategoryId(),
                category.getName(),
                category.getDescription(),
                category.getSortOrder(),
                category.getIsActive(),
                category.getUpdateAudit().getUpdatedAt()
        );
    }
}
