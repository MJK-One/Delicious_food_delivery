package com.dfdt.delivery.domain.category.application.service;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.category.application.service.query.CategoryQueryService;
import com.dfdt.delivery.domain.category.domain.entity.Category;
import com.dfdt.delivery.domain.category.domain.enums.CategoryErrorCode;
import com.dfdt.delivery.domain.category.domain.repository.CategoryCustomRepository;
import com.dfdt.delivery.domain.category.domain.repository.JpaCategoryRepository;
import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryAdminResDto;
import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryQueryServiceImpl implements CategoryQueryService {

    private final JpaCategoryRepository categoryRepository;
    private final CategoryCustomRepository categoryCustomRepository;

    public CategoryResDto getCategory(UUID categoryId) {
        Category category = checkExistCategory(categoryId);

        return CategoryResDto.from(category);
    }

    public List<CategoryResDto> getCategories() {
        List<Category> categories = categoryRepository.findActiveCategoriesOrderBySortOrder();
        if (categories.isEmpty()) {
            throw new BusinessException(CategoryErrorCode.NOT_FOUND_CATEGORIES);    // 등록된 카테고리가 존재하는지 확인
        }

        return categories.stream()
                .map(CategoryResDto::from)
                .toList();
    }

    public Page<CategoryAdminResDto> getCategoriesAdmin(int page, int size, String sortBy, Boolean isAsc, String name, boolean isDeleted) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CategoryAdminResDto> categoryResDto = categoryCustomRepository.searchCategoriesAdmin(pageable, name, isDeleted);
        if (categoryResDto.getTotalElements() == 0) {
            throw new BusinessException(CategoryErrorCode.NOT_FOUND_CATEGORIES);    // 등록된 카테고리가 존재하는지 확인
        }

        return categoryResDto;
    }

    // 해당 카테고리가 존재하는지 확인
    private Category checkExistCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(CategoryErrorCode.NOT_FOUND_CATEGORY));
    }

}
