package com.dfdt.delivery.domain.category.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.category.presentation.docs.CategoryErrorDocs;
import com.dfdt.delivery.domain.category.presentation.docs.CategorySuccessDocs;
import com.dfdt.delivery.domain.category.presentation.dto.request.CategoryCreateReqDto;
import com.dfdt.delivery.domain.category.presentation.dto.request.CategoryUpdateReqDto;
import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryPageResDto;
import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryResDto;
import com.dfdt.delivery.domain.category.presentation.dto.response.CategoryUpdateResDto;
import com.dfdt.delivery.domain.store.presentation.docs.StoreErrorDocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Tag(name = "Category(카테고리) API", description = "카테고리 관련 처리를 담당합니다.")
public interface CategoryControllerDocs {

    @Operation(summary = "[API-CATEGORY-001] 카테고리 단일 조회", description = "카테고리 한 건을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 조회 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = CategorySuccessDocs.CATEGORY_GET_SUCCESS))),
            @ApiResponse(responseCode = "404", description = "해당 카테고리 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "NOT_FOUND_CATEGORY", value = CategoryErrorDocs.NOT_FOUND_CATEGORY)))
    })
    ResponseEntity<ApiResponseDto<CategoryResDto>> getCategory(@PathVariable("categoryId") UUID categoryId);

    @Operation(summary = "[API-CATEGORY-002-1] 카테고리 조회", description = "활성화된(삭제되지 않은) 카테고리 전체 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = CategorySuccessDocs.CATEGORIES_GET_SUCCESS))),
            @ApiResponse(responseCode = "404", description = "등록된 카테고리가 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "NOT_FOUND_CATEGORIES", value = CategoryErrorDocs.NOT_FOUND_CATEGORIES)))
    })
    ResponseEntity<ApiResponseDto<List<CategoryResDto>>> getCategories();

    @Operation(summary = "[API-CATEGORY-002-2] 카테고리 조회(관리자용)", description = "관리자용 카테고리 목록을 페이징하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "관리자용 카테고리 목록 조회 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = CategorySuccessDocs.ADMIN_CATEGORIES_GET_SUCCESS))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "FORBIDDEN", value = CategoryErrorDocs.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "등록된 카테고리가 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "NOT_FOUND_CATEGORIES", value = CategoryErrorDocs.NOT_FOUND_CATEGORIES)))
    })
    ResponseEntity<ApiResponseDto<CategoryPageResDto>> getCategoriesAdmin(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "isAsc", defaultValue = "true") boolean isAsc,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "isDeleted", defaultValue = "false") Boolean isDeleted
    );

    @Operation(summary = "[API-CATEGORY-003] 카테고리 생성", description = "카테고리를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "카테고리 생성 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = CategorySuccessDocs.CATEGORY_CREATE_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "카테고리 생성 불가",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "INVALID_REQUEST", value = CategoryErrorDocs.INVALID_REQUEST),
                                    @ExampleObject(name = "ALREADY_EXIST", value = CategoryErrorDocs.ALREADY_EXIST),
                                    @ExampleObject(name = "NOT_FOUND_STORE", value = StoreErrorDocs.NOT_FOUND_STORE)
                            })),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "FORBIDDEN", value = CategoryErrorDocs.FORBIDDEN)))
    })
    ResponseEntity<ApiResponseDto<CategoryResDto>> createCategory(@Valid @RequestBody CategoryCreateReqDto request, @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "[API-CATEGORY-004] 카테고리 수정", description = "카테고리를 수정합니다. MASTER 권한 필요")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 수정 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = CategorySuccessDocs.CATEGORY_UPDATE_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "수정 불가",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "ALREADY_EXIST", summary = "이름 중복", value = CategoryErrorDocs.ALREADY_EXIST),
                                    @ExampleObject(name = "NOT_MODIFIED", summary = "삭제된 항목 수정 시도", value = CategoryErrorDocs.NOT_MODIFIED)
                            })),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "FORBIDDEN", value = CategoryErrorDocs.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "해당 카테고리 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "NOT_FOUND_CATEGORY", value = CategoryErrorDocs.NOT_FOUND_CATEGORY)))
    })
    ResponseEntity<ApiResponseDto<CategoryUpdateResDto>> updateCategory(
            @PathVariable("categoryId") UUID categoryId,
            @Valid @RequestBody CategoryUpdateReqDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "[API-CATEGORY-005] 카테고리 삭제", description = "카테고리를 Soft Delete합니다. 해당 카테고리를 사용하는 가게가 있으면 삭제할 수 없습니다. MASTER 권한 필요")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 삭제 성공",
                content = @Content(mediaType = "application/json", examples = @ExampleObject(value = CategorySuccessDocs.CATEGORY_DELETE_SUCCESS))),
    @ApiResponse(responseCode = "400", description = "삭제 불가",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "ALREADY_DELETED", value = CategoryErrorDocs.ALREADY_DELETED),
                                    @ExampleObject(name = "CATEGORY_BE_USED", value = CategoryErrorDocs.CATEGORY_BE_USED)
                            })),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "FORBIDDEN", value = CategoryErrorDocs.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "해당 카테고리 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "NOT_FOUND_CATEGORY", value = CategoryErrorDocs.NOT_FOUND_CATEGORY)))
    })
    ResponseEntity<ApiResponseDto<Object>> deleteCategory(@PathVariable("categoryId") UUID categoryId, @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "[API-CATEGORY-006] 카테고리 복구", description = "Soft Delete된 카테고리를 복구합니다. MASTER 권한 필요")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 복구 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = CategorySuccessDocs.CATEGORY_RESTORE_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "복구 불가",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "NOT_DELETED", summary = "삭제되지 않은 항목", value = CategoryErrorDocs.NOT_DELETED),
                                    @ExampleObject(name = "ALREADY_EXIST", summary = "이름 중복 발생", value = CategoryErrorDocs.ALREADY_EXIST)
                            })),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "FORBIDDEN", value = CategoryErrorDocs.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "해당 카테고리 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "NOT_FOUND_CATEGORY", value = CategoryErrorDocs.NOT_FOUND_CATEGORY)))
    })
    ResponseEntity<ApiResponseDto<Object>> restoreCategory(@PathVariable("categoryId") UUID categoryId, @AuthenticationPrincipal CustomUserDetails userDetails);
}