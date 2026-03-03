package com.dfdt.delivery.domain.product.presentation.dto.response;

import com.dfdt.delivery.domain.product.domain.entity.Product;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductResDto {

    private UUID productId;
    private String name;
    private String description;
    private Integer price;

    public static ProductResDto from(Product product) {
        return new ProductResDto(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getPrice()
        );
    }
}

