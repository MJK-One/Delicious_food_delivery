package com.dfdt.delivery.domain.product.presentation.dto.response;

import com.dfdt.delivery.domain.product.domain.entity.Product;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductResDto {

    private UUID productId;
    private String name;
    private String description;
    private Boolean isAiDescription;
    private Integer price;
    private Integer displayOrder;
    private Boolean isHidden;
    private OffsetDateTime createdAt;

    public static ProductResDto from(Product product) {
        return new ProductResDto(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getIsAiDescription(),
                product.getPrice(),
                product.getDisplayOrder(),
                product.getIsHidden(),
                product.getCreateAudit().getCreatedAt()
        );
    }
}

