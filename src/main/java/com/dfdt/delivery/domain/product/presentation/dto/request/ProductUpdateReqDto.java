package com.dfdt.delivery.domain.product.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductUpdateReqDto {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @Min(0)
    private Integer price;

    private Integer displayOrder;
}

