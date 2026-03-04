package com.dfdt.delivery.domain.product.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class ProductPageResDto {

    private List<ProductResDto> products;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static ProductPageResDto from(Page<ProductResDto> page) {
        return ProductPageResDto.builder()
                .products(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}

