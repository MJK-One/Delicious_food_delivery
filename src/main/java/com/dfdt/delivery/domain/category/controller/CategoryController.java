package com.dfdt.delivery.domain.category.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.category.dto.request.CategoryCreateReqDto;
import com.dfdt.delivery.domain.category.dto.request.CategoryUpdateReqDto;
import com.dfdt.delivery.domain.category.dto.response.CategoryResDto;
import com.dfdt.delivery.domain.category.service.CategoryService;
import com.dfdt.delivery.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 카테고리 목록 조회
     * GET /api/v1/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponseDto<List<CategoryResDto>>> getCategories() {
        List<CategoryResDto> categories = categoryService.getCategories();

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "카테고리 목록이 성공적으로 조회되었습니다.",
                categories
        );
    }

    /**
     * 카테고리 생성
     * POST /api/v1/categories
     */
    @PreAuthorize("hasRole('MASTER')")
    @PostMapping("/categories")
    public ResponseEntity<ApiResponseDto<CategoryResDto>> createCategory(@Valid @RequestBody CategoryCreateReqDto request, @AuthenticationPrincipal User user) {
        CategoryResDto createdCategory = categoryService.createCategory(request);

        return ApiResponseDto.success(
                HttpStatus.CREATED.value(),
                "카테고리가 성공적으로 생성되었습니다.",
                createdCategory
        );
    }

    /**
     * 카테고리 수정
     * PUT /api/v1/categories/{category_id}
     */
    @PreAuthorize("hasRole('MASTER')")
    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<ApiResponseDto<CategoryResDto>> updateCategory(
            @PathVariable("categoryId") UUID categoryId,
            @Valid @RequestBody CategoryUpdateReqDto request,
            @AuthenticationPrincipal User user
    ) {
        CategoryResDto updated = categoryService.updateCategory(categoryId, request);

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "카테고리가 성공적으로 수정되었습니다.",
                updated
        );
    }

    /**
     * 카테고리 삭제 (Soft Delete)
     * DELETE /api/v1/categories/{category_id}
     */
    @PreAuthorize("hasRole('MASTER')")
    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<ApiResponseDto<Object>> deleteCategory(@PathVariable("categoryId") UUID categoryId, @AuthenticationPrincipal User user) {
        categoryService.deleteCategory(categoryId, user);

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "카테고리가 성공적으로 삭제되었습니다.",
                null
        );
    }

    /**
     * 카테고리 복구
     * PATCH /api/v1/categories/{category_id}/restore
     */
    @PreAuthorize("hasRole('MASTER')")
    @PatchMapping("/categories/{categoryId}/restore")
    public ResponseEntity<ApiResponseDto<Object>> restoreCategory(@PathVariable("categoryId") UUID categoryId, @AuthenticationPrincipal User user) {
        categoryService.restoreCategory(categoryId);

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "카테고리가 성공적으로 복구되었습니다.",
                null
        );
    }
}
