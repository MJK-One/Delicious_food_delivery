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
public class CategoryAdminResDto {

    private UUID categoryId;
    private String name;
    private String description;
    private Integer sortOrder;
    private Boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;

    public static CategoryAdminResDto from(Category category) {
        return new CategoryAdminResDto(
                category.getCategoryId(),
                category.getName(),
                category.getDescription(),
                category.getSortOrder(),
                category.getIsActive(),
                category.getCreateAudit().getCreatedAt(),
                category.getUpdateAudit().getUpdatedAt(),
                category.getSoftDeleteAudit().getDeletedAt()
        );
    }
}
