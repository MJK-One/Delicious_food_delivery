package com.dfdt.delivery.domain.product.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.category.presentation.docs.CategoryErrorDocs;
import com.dfdt.delivery.domain.product.presentation.docs.ProductErrorDocs;
import com.dfdt.delivery.domain.product.presentation.docs.ProductSuccessDocs;
import com.dfdt.delivery.domain.product.presentation.dto.request.ProductCreateReqDto;
import com.dfdt.delivery.domain.product.presentation.dto.request.ProductUpdateReqDto;
import com.dfdt.delivery.domain.product.presentation.dto.response.ProductAdminPageResDto;
import com.dfdt.delivery.domain.product.presentation.dto.response.ProductPageResDto;
import com.dfdt.delivery.domain.product.presentation.dto.response.ProductResDto;
import com.dfdt.delivery.domain.product.presentation.dto.response.ProductUpdateResDto;
import com.dfdt.delivery.domain.store.presentation.docs.StoreErrorDocs;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreCreateReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreUpdateReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.MyStoreResDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreCreateResDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.StoreUpdateResDto;

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

@Tag(name = "Product(메뉴) API", description = "메뉴 관련 처리를 담당합니다.")
public interface ProductControllerDocs {

    @Operation(summary = "[API-PRODUCT-001] 메뉴 단일 조회", description = "메뉴 한 건을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메뉴 조회 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ProductSuccessDocs.PRODUCT_GET_SUCCESS))),
            @ApiResponse(responseCode = "404", description = "메뉴를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                @ExampleObject(name = "NOT_FOUND_STORE", value = StoreErrorDocs.NOT_FOUND_STORE),
                                @ExampleObject(name = "NOT_FOUND_PRODUCT", value = ProductErrorDocs.NOT_FOUND_PRODUCT)
                            }))
    })
    ResponseEntity<ApiResponseDto<ProductResDto>> getProduct(@PathVariable("storeId") UUID storeId, @PathVariable("productId") UUID productId);

    @Operation(summary = "[API-PRODUCT-002-1] 메뉴 조회", description = "메뉴 목록을 페이징, 필터링하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메뉴 목록 조회 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ProductSuccessDocs.PRODUCTS_GET_SUCCESS))),
            @ApiResponse(responseCode = "404", description = "등록된 메뉴가 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "NOT_FOUND_PRODUCTS", value = ProductErrorDocs.NOT_FOUND_PRODUCTS)))
    })
    ResponseEntity<ApiResponseDto<ProductPageResDto>> getProducts(
            @PathVariable("storeId") UUID storeId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "isAsc", defaultValue = "true") boolean isAsc,
            @RequestParam(value = "keyword", required = false) String keyword
    );

    @Operation(summary = "[API-PRODUCT-002-2] 메뉴 조회(관리자용)", description = "메뉴 목록을 페이징, 필터링하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메뉴 목록 조회 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ProductSuccessDocs.PRODUCTS_GET_SUCCESS))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "FORBIDDEN", value = ProductErrorDocs.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "등록된 메뉴가 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "NOT_FOUND_PRODUCTS", value = ProductErrorDocs.NOT_FOUND_PRODUCTS)))
    })
    ResponseEntity<ApiResponseDto<ProductAdminPageResDto>> getProductsAdmin(
            @PathVariable("storeId") UUID storeId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "isAsc", defaultValue = "true") boolean isAsc,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "isDeleted", defaultValue = "false") Boolean isDeleted
    );

    @Operation(summary = "[API-PRODUCT-003] 메뉴 생성", description = "메뉴를 생성합니다. 본인의 가게에만 메뉴를 생성할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "메뉴 생성 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ProductSuccessDocs.PRODUCT_CREATE_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "생성 불가",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                @ExampleObject(name = "INVALID_REQUEST", value = ProductErrorDocs.INVALID_REQUEST),
                                @ExampleObject(name = "NOT_MY_STORE", value = StoreErrorDocs.NOT_MY_STORE)
                            })),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "FORBIDDEN", value = ProductErrorDocs.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "해당 가게 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "NOT_FOUND_STORE", value = StoreErrorDocs.NOT_FOUND_STORE)))
    })
    ResponseEntity<ApiResponseDto<ProductResDto>> createProduct(
            @PathVariable("storeId") UUID storeId,
            @Valid @RequestBody ProductCreateReqDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "[API-STORE-004] 메뉴 수정", description = "메뉴를 수정합니다. 본인 또는 MASTER 권한 필요")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가게 수정 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ProductSuccessDocs.PRODUCT_UPDATE_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "수정 불가",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "NOT_MY_STORE", value = StoreErrorDocs.NOT_MY_STORE),
                                    @ExampleObject(name = "ALREADY_DELETED", value = ProductErrorDocs.NOT_MODIFIED)
                            })),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "FORBIDDEN", value = ProductErrorDocs.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "해당 가게/메뉴 없음",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                @ExampleObject(name = "NOT_FOUND_STORE", value = StoreErrorDocs.NOT_FOUND_STORE),
                                @ExampleObject(name = "NOT_FOUND_CATEGORY", value = CategoryErrorDocs.NOT_FOUND_CATEGORY),
                            }))
    })
    ResponseEntity<ApiResponseDto<ProductUpdateResDto>> updateProduct(
            @PathVariable("storeId") UUID storeId,
            @PathVariable("productId") UUID productId,
            @Valid @RequestBody ProductUpdateReqDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "[API-PRODUCT-005] 메뉴 삭제", description = "메뉴를 삭제 처리(Soft Delete)합니다. 본인 또는 MASTER 권한 필요")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메뉴 삭제 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ProductSuccessDocs.PRODUCT_DELETE_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "삭제 불가",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "NOT_MY_STORE", value = StoreErrorDocs.NOT_MY_STORE),
                                    @ExampleObject(name = "ALREADY_DELETED", value = ProductErrorDocs.ALREADY_DELETED)
                            })),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "FORBIDDEN", value = ProductErrorDocs.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "해당 가게/메뉴 없음",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "NOT_FOUND_STORE", value = StoreErrorDocs.NOT_FOUND_STORE),
                                    @ExampleObject(name = "NOT_FOUND_CATEGORY", value = CategoryErrorDocs.NOT_FOUND_CATEGORY),
                            }))
    })
    ResponseEntity<ApiResponseDto<Object>> deleteProduct(@PathVariable("storeId") UUID storeId, @PathVariable("productId") UUID productId, @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "[API-PRODUCT-006] 메뉴 품절 처리", description = "메뉴를 품절 처리(isHidden())합니다. 본인 또는 MASTER 권한 필요")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메뉴 품절 처리 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ProductSuccessDocs.SOLD_OUT_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "품절 처리 불가",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "NOT_MY_STORE", value = ProductErrorDocs.NOT_MODIFIED))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "FORBIDDEN", value = ProductErrorDocs.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "해당 가게/메뉴 없음",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "NOT_FOUND_STORE", value = StoreErrorDocs.NOT_FOUND_STORE),
                                    @ExampleObject(name = "NOT_FOUND_CATEGORY", value = CategoryErrorDocs.NOT_FOUND_CATEGORY),
                            }))
    })
    ResponseEntity<ApiResponseDto<Object>> soleOut(@PathVariable("storeId") UUID storeId, @PathVariable("productId") UUID productId, @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "[API-PRODUCT-007] 메뉴 복구", description = "메뉴를 복구합니다. 본인 또는 MASTER 권한 필요")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메뉴 복구 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ProductSuccessDocs.PRODUCT_RESTORE_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "복구 불가",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "NOT_DELETED", value = ProductErrorDocs.NOT_DELETED))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "FORBIDDEN", value = ProductErrorDocs.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "해당 가게/메뉴 없음",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "NOT_FOUND_STORE", value = StoreErrorDocs.NOT_FOUND_STORE),
                                    @ExampleObject(name = "NOT_FOUND_CATEGORY", value = CategoryErrorDocs.NOT_FOUND_CATEGORY),
                            }))
    })
    ResponseEntity<ApiResponseDto<Object>> restoreProduct(@PathVariable("storeId") UUID storeId, @PathVariable("productId") UUID productId, @AuthenticationPrincipal CustomUserDetails userDetails);
}