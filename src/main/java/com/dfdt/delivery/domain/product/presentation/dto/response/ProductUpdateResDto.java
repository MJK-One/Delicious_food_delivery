package com.dfdt.delivery.domain.product.presentation.dto.response;

import com.dfdt.delivery.domain.product.domain.entity.Product;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductUpdateResDto {

    private UUID productId;
    private String name;
    private String description;
    private Boolean isAiDescription;
    private Integer price;
    private Integer displayOrder;
    private Boolean isHidden;
    private OffsetDateTime updatedAt;

    public static ProductUpdateResDto from(Product product) {
        return new ProductUpdateResDto(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getIsAiDescription(),
                product.getPrice(),
                product.getDisplayOrder(),
                product.getIsHidden(),
                product.getUpdateAudit().getUpdatedAt()
        );
    }
}

