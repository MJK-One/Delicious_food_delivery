package com.dfdt.delivery.domain.category.application.service.query;

import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryAdminResDto;
import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryResDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface CategoryQueryService {
    CategoryResDto getCategory(UUID categoryId);

    List<CategoryResDto> getCategories();

    Page<CategoryAdminResDto> getCategoriesAdmin(int page, int size, String sortBy, Boolean isAsc, String name, boolean isDeleted);
}
