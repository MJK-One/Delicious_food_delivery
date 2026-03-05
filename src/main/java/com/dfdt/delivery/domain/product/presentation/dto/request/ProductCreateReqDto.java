package com.dfdt.delivery.domain.product.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCreateReqDto {

    @NotBlank
    private String name;

    @Size(min = 0, max = 255)
    private String description;

    private Boolean isAiDescription = false;

    @NotNull
    @Min(0)
    @Max(1_000_000)
    private Integer price;

    @NotNull
    private Boolean isHidden;
}

