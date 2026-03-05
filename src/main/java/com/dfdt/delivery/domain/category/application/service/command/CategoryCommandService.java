package com.dfdt.delivery.domain.category.application.service.command;

import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.category.presentation.dto.request.CategoryCreateReqDto;
import com.dfdt.delivery.domain.category.presentation.dto.request.CategoryUpdateReqDto;
import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryResDto;
import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryUpdateResDto;

import java.util.UUID;

public interface CategoryCommandService {
    CategoryResDto createCategory(CategoryCreateReqDto request, CustomUserDetails userDetails);

    CategoryUpdateResDto updateCategory(UUID categoryId, CategoryUpdateReqDto request, CustomUserDetails userDetails);

    void deleteCategory(UUID categoryId, CustomUserDetails userDetails);

    void restoreCategory(UUID categoryId, CustomUserDetails userDetails);
}
