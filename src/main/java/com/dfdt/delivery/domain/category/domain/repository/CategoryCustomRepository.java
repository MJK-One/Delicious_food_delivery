package com.dfdt.delivery.domain.category.domain.repository;

import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryAdminResDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryCustomRepository {
    Page<CategoryAdminResDto> searchCategoriesAdmin(Pageable pageable, String name, Boolean isDeleted);
}
