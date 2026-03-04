package com.dfdt.delivery.domain.store.presentation.dto.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class StoreAdminPageResDto {

    public StoreAdminPageResDto(Page<StoreAdminResDto> pageResult) {
        this.content = pageResult.getContent();
        this.page = pageResult.getNumber();
        this.size = pageResult.getSize();
        this.totalElements = pageResult.getTotalElements();
        this.totalPages = pageResult.getTotalPages();
    }

    private List<StoreAdminResDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

}