package com.dfdt.delivery.domain.category.presentation.dto.response;

import com.dfdt.delivery.domain.category.domain.entity.Category;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryResDto {

    private UUID categoryId;
    private String name;
    private String description;
    private Integer sortOrder;
    private Boolean isActive;
    private OffsetDateTime createdAt;

    public static CategoryResDto from(Category category) {
        return new CategoryResDto(
                category.getCategoryId(),
                category.getName(),
                category.getDescription(),
                category.getSortOrder(),
                category.getIsActive(),
                category.getCreateAudit().getCreatedAt()
        );
    }
}
