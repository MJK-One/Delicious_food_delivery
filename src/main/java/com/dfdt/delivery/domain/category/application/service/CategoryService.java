package com.dfdt.delivery.domain.category.application.service;

import com.dfdt.delivery.domain.category.presentation.dto.request.CategoryCreateReqDto;
import com.dfdt.delivery.domain.category.presentation.dto.request.CategoryUpdateReqDto;
import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryResDto;
import com.dfdt.delivery.domain.category.domain.entity.Category;
import com.dfdt.delivery.domain.category.domain.repository.CategoryRepository;
import com.dfdt.delivery.domain.store.domain.repository.StoreRepository;
import com.dfdt.delivery.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;

    @Transactional(readOnly = true)
    public List<CategoryResDto> getCategories() {
        return categoryRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(CategoryResDto::from)
                .toList();
    }

    public CategoryResDto createCategory(CategoryCreateReqDto request) {
        Category category = Category.create(request);
        categoryRepository.save(category);

        return CategoryResDto.from(category);
    }

    public CategoryResDto updateCategory(UUID categoryId, CategoryUpdateReqDto request) {
        Category category = categoryRepository.findByCategoryIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다."));

        if (!category.getIsActive()) {
            boolean hasStore = storeRepository.existsByCategoryIdAndDeletedAtIsNull((category.getCategoryId()));

            if (hasStore) {
                throw new IllegalArgumentException("삭제된 카테고리가 아닙니다.");
            }
        }
        category.update(request);

        return CategoryResDto.from(category);
    }

    public void deleteCategory(UUID categoryId, User user) {
        Category category = categoryRepository.findByCategoryIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다."));

        category.delete(user.getUsername());
    }

    public void restoreCategory(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다."));
        if (category.getDeletedAt() == null) {
            throw new IllegalArgumentException("삭제된 카테고리가 아닙니다.");
        }

        category.restore();
    }
}
