package com.dfdt.delivery.domain.category.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.category.application.service.command.CategoryCommandService;
import com.dfdt.delivery.domain.category.application.service.query.CategoryQueryService;
import com.dfdt.delivery.domain.category.presentation.dto.request.CategoryCreateReqDto;
import com.dfdt.delivery.domain.category.presentation.dto.request.CategoryUpdateReqDto;
import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryAdminResDto;
import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryPageResDto;
import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryResDto;
import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryUpdateResDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController implements CategoryControllerDocs{

    private final CategoryQueryService categoryQueryService;
    private final CategoryCommandService categoryCommandService;

    /**
     * 카테고리 단일 조회
     * GET /api/v1/categories
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponseDto<CategoryResDto>> getCategory(@PathVariable("categoryId") UUID categoryId) {
        CategoryResDto categories = categoryQueryService.getCategory(categoryId);

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "카테고리가 성공적으로 조회되었습니다.",
                categories
        );
    }

    /**
     * 카테고리 조회
     * GET /api/v1/categories
     */
    @GetMapping()
    public ResponseEntity<ApiResponseDto<List<CategoryResDto>>> getCategories() {
        List<CategoryResDto> categories = categoryQueryService.getCategories();

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "카테고리 목록이 성공적으로 조회되었습니다.",
                categories
        );
    }

    /**
     * 카테고리 조회(관리자용)
     * 삭제 여부 상관 없이 불러옴
     * GET /api/v1/categories/admin
     */
    @PreAuthorize("hasRole('MASTER')")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponseDto<CategoryPageResDto>> getCategoriesAdmin(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "isAsc", defaultValue = "true") boolean isAsc,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "isDeleted", defaultValue = "false") Boolean isDeleted
    ) {
        Page<CategoryAdminResDto> categories = categoryQueryService.getCategoriesAdmin(page, size, sortBy, isAsc, name, isDeleted);
        CategoryPageResDto response = new CategoryPageResDto(categories);

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "카테고리 목록이 성공적으로 조회되었습니다.",
                response
        );
    }

    /**
     * 카테고리 생성
     * POST /api/v1/categories
     */
    @PreAuthorize("hasRole('MASTER')")
    @PostMapping()
    public ResponseEntity<ApiResponseDto<CategoryResDto>> createCategory(@Valid @RequestBody CategoryCreateReqDto request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        CategoryResDto createdCategory = categoryCommandService.createCategory(request, userDetails);

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
    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponseDto<CategoryUpdateResDto>> updateCategory(@PathVariable("categoryId") UUID categoryId, @Valid @RequestBody CategoryUpdateReqDto request,
                                                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        CategoryUpdateResDto updated = categoryCommandService.updateCategory(categoryId, request, userDetails);

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
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponseDto<Object>> deleteCategory(@PathVariable("categoryId") UUID categoryId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        categoryCommandService.deleteCategory(categoryId, userDetails);

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
    @PatchMapping("/{categoryId}/restore")
    public ResponseEntity<ApiResponseDto<Object>> restoreCategory(@PathVariable("categoryId") UUID categoryId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        categoryCommandService.restoreCategory(categoryId, userDetails);

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "카테고리가 성공적으로 복구되었습니다.",
                null
        );
    }
}
