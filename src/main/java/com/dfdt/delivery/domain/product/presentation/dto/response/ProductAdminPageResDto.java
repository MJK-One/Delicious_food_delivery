package com.dfdt.delivery.domain.product.presentation.dto.response;

import com.dfdt.delivery.domain.store.presentation.dto.response.StoreResDto;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class ProductAdminPageResDto {

    private List<ProductAdminResDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static ProductAdminPageResDto from(Page<ProductAdminResDto> page) {
        return ProductAdminPageResDto.builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

}