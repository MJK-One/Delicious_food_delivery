package com.dfdt.delivery.domain.category.application.service;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.category.application.service.command.CategoryCommandService;
import com.dfdt.delivery.domain.category.domain.entity.Category;
import com.dfdt.delivery.domain.category.domain.enums.CategoryErrorCode;
import com.dfdt.delivery.domain.category.domain.repository.CategoryCustomRepository;
import com.dfdt.delivery.domain.category.domain.repository.CategoryRepository;
import com.dfdt.delivery.domain.category.domain.repository.JpaCategoryRepository;
import com.dfdt.delivery.domain.category.presentation.dto.request.CategoryCreateReqDto;
import com.dfdt.delivery.domain.category.presentation.dto.request.CategoryUpdateReqDto;
import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryResDto;
import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryUpdateResDto;
import com.dfdt.delivery.domain.store.domain.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryCommandServiceImpl implements CategoryCommandService {

    private final JpaCategoryRepository categoryRepository;
    private final StoreRepository storeRepository;

    public CategoryResDto createCategory(CategoryCreateReqDto request, CustomUserDetails userDetails) {
        checkDuplicatedName(request.getName());

        int maxSortOrder = categoryRepository.findMaxSortOrder().orElse(0);
        Category category = Category.create(request, maxSortOrder + 1, userDetails.getUsername()); // 기존 sortOrder 값 중 (마지막 번호 + 1) 주입
        categoryRepository.save(category);

        return CategoryResDto.from(category);
    }


    public CategoryUpdateResDto updateCategory(UUID categoryId, CategoryUpdateReqDto request, CustomUserDetails userDetails) {
        Category category = checkExistActiveCategory(categoryId);

        if (categoryRepository.existsByNameAndCategoryIdNot(request.getName(), categoryId)) {
            throw new BusinessException(CategoryErrorCode.ALREADY_EXIST);   // 수정 시 중복 카테고리명 확인(본인 제외)
        }
        if (category.getSoftDeleteAudit() != null) {
            throw new BusinessException(CategoryErrorCode.NOT_MODIFIED);    // 삭제된 카테고리는 정보 변경 X
        }

        // sortOrder 변경이 있으면 sortOrder 값 조정
        int oldSortOrder = category.getSortOrder();
        int newSortOrder = request.getSortOrder();

        if (oldSortOrder != newSortOrder) {
            if (newSortOrder < oldSortOrder) {
                // 위로 이동: 새 위치 ~ 기존 위치-1 까지 +1
                categoryRepository.shiftSortOrdersUp(newSortOrder, oldSortOrder - 1);
            } else {
                // 아래로 이동: 기존 위치+1 ~ 새 위치 까지 -1
                categoryRepository.shiftSortOrdersDown(oldSortOrder + 1, newSortOrder);
            }
        }
        category.update(request, userDetails.getUsername());

        return CategoryUpdateResDto.from(category);
    }

    public void deleteCategory(UUID categoryId, CustomUserDetails userDetails) {
        Category category = checkExistCategory(categoryId);

        if (category.getSoftDeleteAudit().isDeleted()) {
            throw new BusinessException(CategoryErrorCode.ALREADY_DELETED);     // 삭제된 카테고리인지 확인
        }
        if (storeRepository.existsByCategoryIdAndNotDeleted(category.getCategoryId())) {
            throw new BusinessException(CategoryErrorCode.CATEGORY_BE_USED);    // 해당 카테고리를 사용중인 가게가 있는지 확인
        }

        categoryRepository.decrementSortOrdersAfter(category.getSortOrder());   // 삭제된 sortOrder 값 이후 모두 -1
        category.delete(userDetails.getUsername());
    }

    public void restoreCategory(UUID categoryId, CustomUserDetails userDetails) {
        Category category = checkExistCategory(categoryId);

        if (category.getSoftDeleteAudit() == null) {
            throw new BusinessException(CategoryErrorCode.NOT_DELETED); // 삭제된 카테고리인지 확인
        }
        checkDuplicatedName(category.getName());

        // 복구 시 기존 sortOrder 값 중 (마지막 번호 + 1) 주입
        int maxSortOrder = categoryRepository.findMaxSortOrder().orElse(0);
        category.restore(maxSortOrder + 1, userDetails.getUsername());
    }

    // 해당 카테고리가 존재하는지 확인
    private Category checkExistCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(CategoryErrorCode.NOT_FOUND_CATEGORY));
    }

    // 해당 카테고리가 존재하는지 확인(Not Deleted)
    private Category checkExistActiveCategory(UUID categoryId) {
        return categoryRepository.findActiveCategoryById(categoryId)
                .orElseThrow(() -> new BusinessException(CategoryErrorCode.NOT_FOUND_CATEGORY));
    }

    // 카테고리명 중복 확인
    private void checkDuplicatedName(String name) {
        if (categoryRepository.existsByNameAndDeletedAtIsNull(name)) {
            throw new BusinessException(CategoryErrorCode.ALREADY_EXIST);
        }
    }
}
