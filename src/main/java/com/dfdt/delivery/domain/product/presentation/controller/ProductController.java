package com.dfdt.delivery.domain.product.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.product.application.command.ProductCommandService;
import com.dfdt.delivery.domain.product.application.query.ProductQueryService;
import com.dfdt.delivery.domain.product.presentation.dto.request.ProductCreateReqDto;
import com.dfdt.delivery.domain.product.presentation.dto.request.ProductUpdateReqDto;
import com.dfdt.delivery.domain.product.presentation.dto.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores/{storeId}/products")
public class ProductController implements ProductControllerDocs{

    private final ProductQueryService productQueryService;
    private final ProductCommandService productCommandService;

    /**
     * 메뉴 단일 조회
     * GET /api/v1/stores/{store_id}/products/{product_id}
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponseDto<ProductResDto>> getProduct(@PathVariable("storeId") UUID storeId, @PathVariable("productId") UUID productId) {
        ProductResDto product = productQueryService.getProduct(storeId, productId);

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "메뉴가 성공적으로 조회되었습니다.",
                product
        );
    }

    /**
     * 메뉴 조회
     * GET /api/v1/stores/{store_id}/products
     */
    @GetMapping
    public ResponseEntity<ApiResponseDto<ProductPageResDto>> getProducts(
            @PathVariable("storeId") UUID storeId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "isAsc", defaultValue = "true") boolean isAsc,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        Page<ProductResDto> products = productQueryService.getProducts(storeId, page, size, sortBy, isAsc, keyword);
        ProductPageResDto response = ProductPageResDto.from(products);

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "메뉴 목록이 성공적으로 조회되었습니다.",
                response
        );
    }

    /**
     * 메뉴 조회(관리자용)
     * GET /api/v1/stores/{store_id}/products/admin
     */
    @GetMapping("/admin")
    public ResponseEntity<ApiResponseDto<ProductAdminPageResDto>> getProductsAdmin(
            @PathVariable("storeId") UUID storeId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "isAsc", defaultValue = "true") boolean isAsc,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "isDeleted", defaultValue = "false") Boolean isDeleted
    ) {
        Page<ProductAdminResDto> products = productQueryService.getProductsAdmin(storeId, page, size, sortBy, isAsc, keyword, isDeleted);
        ProductAdminPageResDto response = ProductAdminPageResDto.from(products);

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "메뉴 목록이 성공적으로 조회되었습니다.",
                response
        );
    }

    /**
     * 메뉴 생성
     * POST /api/v1/stores/{store_id}/products
     */
    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    @PostMapping
    public ResponseEntity<ApiResponseDto<ProductResDto>> createProduct(@PathVariable("storeId") UUID storeId, @Valid @RequestBody ProductCreateReqDto request,
                                                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProductResDto created = productCommandService.createProduct(storeId, request, userDetails);

        return ApiResponseDto.success(
                HttpStatus.CREATED.value(),
                "메뉴가 성공적으로 생성되었습니다.",
                created
        );
    }

    /**
     * 메뉴 수정
     * PUT /api/v1/stores/{store_id}/products/{product_id}
     */
    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponseDto<ProductUpdateResDto>> updateProduct(
            @PathVariable("storeId") UUID storeId,
            @PathVariable("productId") UUID productId,
            @Valid @RequestBody ProductUpdateReqDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ProductUpdateResDto updated = productCommandService.updateProduct(storeId, productId, request, userDetails);

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "메뉴가 성공적으로 수정되었습니다.",
                updated
        );
    }

    /**
     * 메뉴 삭제
     * DELETE /api/v1/stores/{store_id}/products/{product_id}
     */
    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponseDto<Object>> deleteProduct(@PathVariable("storeId") UUID storeId, @PathVariable("productId") UUID productId,
                                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        productCommandService.deleteProduct(storeId, productId, userDetails);

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "메뉴가 성공적으로 삭제되었습니다.",
                null
        );
    }

    /**
     * 품절 처리
     * PATCH /api/v1/stores/{store_id}/products/{product_id}/sold-out
     */
    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    @PatchMapping("/{productId}/sold-out")
    public ResponseEntity<ApiResponseDto<Object>> soleOut(@PathVariable("storeId") UUID storeId, @PathVariable("productId") UUID productId,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        productCommandService.soleOut(storeId, productId, userDetails);

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "메뉴가 품절 처리되었습니다.",
                null
        );
    }

    /**
     * 메뉴 복구
     * PATCH /api/v1/stores/{store_id}/products/{product_id}/restore
     */
    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    @PatchMapping("/{productId}/restore")
    public ResponseEntity<ApiResponseDto<Object>> restoreProduct(@PathVariable("storeId") UUID storeId, @PathVariable("productId") UUID productId,
                                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        productCommandService.restoreProduct(storeId, productId, userDetails);

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "메뉴가 성공적으로 복구되었습니다.",
                null
        );
    }
}

