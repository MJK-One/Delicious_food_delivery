package com.dfdt.delivery.domain.store.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;

import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.category.presentation.docs.CategoryErrorDocs;
import com.dfdt.delivery.domain.store.presentation.docs.StoreErrorDocs;
import com.dfdt.delivery.domain.store.presentation.docs.StoreSuccessDocs;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreCreateReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreStatusReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreUpdateReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.*;

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

@Tag(name = "Store(가게) API", description = "가게 관련 처리를 담당합니다.")
public interface StoreControllerDocs {

    @Operation(summary = "[API-STORE-001] 가게 단일 조회", description = "가게 한 건을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가게 조회 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = StoreSuccessDocs.STORE_GET_SUCCESS))),
            @ApiResponse(responseCode = "404", description = "가게를 찾을 수 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "NOT_FOUND_STORE", value = StoreErrorDocs.NOT_FOUND_STORE)))
    })
    ResponseEntity<ApiResponseDto<StoreResDto>> getStore(@PathVariable("storeId") UUID storeId);

    @Operation(summary = "[API-STORE-002-1] 가게 조회", description = "검색 조건에 맞는 활성화된(삭제되지 않은) 가게 목록을 페이징하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가게 목록 조회 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = StoreSuccessDocs.STORES_GET_SUCCESS))),
            @ApiResponse(responseCode = "404", description = "등록된 가게가 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "NOT_FOUND_STORES", value = StoreErrorDocs.NOT_FOUND_STORES)))
    })
    ResponseEntity<ApiResponseDto<StorePageResDto>> getStores(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "isAsc", defaultValue = "false") boolean isAsc,
            @RequestParam(value = "category", required = false) UUID category,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "region", required = false) UUID region
    );

    @Operation(summary = "[API-STORE-002-2] 가게 조회(관리자용)", description = "검색 조건에 맞는 가게 목록을 페이징하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가게 목록 조회 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = StoreSuccessDocs.ADMIN_STORES_GET_SUCCESS))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "FORBIDDEN", value = StoreErrorDocs.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "등록된 가게가 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "NOT_FOUND_STORES", value = StoreErrorDocs.NOT_FOUND_STORES)))
    })
    ResponseEntity<ApiResponseDto<StoreAdminPageResDto>> getStoresAdmin(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "isAsc", defaultValue = "false") boolean isAsc,
            @RequestParam(value = "category", required = false) UUID category,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "region", required = false) UUID region,
            @RequestParam(value = "isDeleted", defaultValue = "false") Boolean isDeleted
    );

    @Operation(summary = "[API-STORE-003] 가게 생성", description = "새로운 가게를 등록합니다. 지역 및 카테고리 정보가 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "가게 생성 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = StoreSuccessDocs.STORE_CREATE_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "INVALID_REQUEST", value = StoreErrorDocs.INVALID_REQUEST),
                                    @ExampleObject(name = "NOT_FOUND_STORE", value = StoreErrorDocs.NOT_FOUND_STORE)
                            })),
            @ApiResponse(responseCode = "404", description = "지역 정보 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "NOT_FOUND_REGION", value = StoreErrorDocs.NOT_FOUND_REGION)))
    })
    ResponseEntity<ApiResponseDto<StoreCreateResDto>> createStore(@Valid @RequestBody StoreCreateReqDto request, @AuthenticationPrincipal CustomUserDetails user);

    @Operation(summary = "[API-STORE-004] 가게 정보 수정", description = "가게 정보를 수정합니다. 본인 또는 MASTER 권한 필요")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가게 수정 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = StoreSuccessDocs.STORE_UPDATE_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "수정 실패",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "NOT_MY_STORE", value = StoreErrorDocs.NOT_MY_STORE),
                                    @ExampleObject(name = "ALREADY_DELETED", value = StoreErrorDocs.NOT_MODIFIED),
                                    @ExampleObject(name = "NOT_FOUND_CATEGORY", value = CategoryErrorDocs.NOT_FOUND_CATEGORY)
                            })),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "FORBIDDEN", value = StoreErrorDocs.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "해당 가게 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = StoreErrorDocs.NOT_FOUND_STORE)))
    })
    ResponseEntity<ApiResponseDto<StoreUpdateResDto>> updateStore(
            @PathVariable("storeId") UUID storeId,
            @Valid @RequestBody StoreUpdateReqDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "[API-STORE-005] 가게 삭제", description = "가게를 삭제 처리(Soft Delete)합니다. 본인 또는 MASTER 권한 필요")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가게 삭제 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = StoreSuccessDocs.STORE_DELETE_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "삭제 불가",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "NOT_MY_STORE", value = StoreErrorDocs.NOT_MY_STORE),
                                    @ExampleObject(name = "ALREADY_DELETED", value = StoreErrorDocs.ALREADY_DELETED)
                            })),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "FORBIDDEN", value = StoreErrorDocs.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "해당 가게 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = StoreErrorDocs.NOT_FOUND_STORE)))
    })
    ResponseEntity<ApiResponseDto<Object>> deleteStore(@PathVariable("storeId") UUID storeId, @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "[API-STORE-006] 영업 상태 변경", description = "가게의 영업 상태(IsOpen)을 변경합니다. 본인 또는 MASTER 권한 필요")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "영업 상태 변경 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = StoreSuccessDocs.STORE_DELETE_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "영업 상태 변경 불가",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "NOT_MY_STORE", value = StoreErrorDocs.NOT_MY_STORE),
                                    @ExampleObject(name = "ALREADY_DELETED", value = StoreErrorDocs.ALREADY_DELETED)
                            })),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "FORBIDDEN", value = StoreErrorDocs.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "해당 가게 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = StoreErrorDocs.NOT_FOUND_STORE)))
    })
    ResponseEntity<ApiResponseDto<Object>> changeStoreOpenStatus(@PathVariable("storeId") UUID storeId, @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "[API-STORE-007] 본인 가게 조회", description = "본인 소유 가게를 조회합니다. 본인 또는 MASTER 권한 필요")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "영업 상태 변경 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = StoreSuccessDocs.STORE_DELETE_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "영업 상태 변경 불가",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "NOT_MY_STORE", value = StoreErrorDocs.NOT_MY_STORE))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "FORBIDDEN", value = StoreErrorDocs.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "해당 가게 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = StoreErrorDocs.NOT_FOUND_STORE)))
    })
    ResponseEntity<ApiResponseDto<List<MyStoreResDto>>> getMyStores(@AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "[API-STORE-008] 가게 복구", description = "가게를 복구합니다. MASTER 권한 필요")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가게 복구 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = StoreSuccessDocs.STORE_DELETE_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "가게 복구 불가",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "NOT_MY_STORE", value = StoreErrorDocs.NOT_SUSPENDED))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "FORBIDDEN", value = StoreErrorDocs.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "해당 가게 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = StoreErrorDocs.NOT_FOUND_STORE)))
    })
    ResponseEntity<ApiResponseDto<Object>> restoreStore(@PathVariable("storeId") UUID storeId, @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "[API-STORE-009] 가게 승인 여부", description = "가게 승인 여부를 변경합니다. MASTER 권한 필요")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가게 승인 여부 변경 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = StoreSuccessDocs.STORE_DELETE_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "가게 승인 여부 변경 불가",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "NOT_MY_STORE", value = StoreErrorDocs.NOT_MY_STORE),
                                    @ExampleObject(name = "STATUS_NOT_MODIFIED", value = StoreErrorDocs.STATUS_NOT_MODIFIED)
                            })),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "FORBIDDEN", value = StoreErrorDocs.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "해당 가게 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = StoreErrorDocs.NOT_FOUND_STORE)))
    })
    ResponseEntity<ApiResponseDto<Object>> changeStoreApprovalStatus(
            @PathVariable("storeId") UUID storeId,
            @RequestBody StoreStatusReqDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "[API-STORE-010] 승인 대기 가게 조회 (관리자)", description = "승인 대기 중인 가게 목록을 조회합니다. MASTER 권한 필요")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = StoreSuccessDocs.REQUESTED_STORES_GET_SUCCESS))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = StoreErrorDocs.FORBIDDEN)))
    })
    public ResponseEntity<ApiResponseDto<StoreRequestPageResDto>> getRequestedStores(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "isAsc", defaultValue = "true") boolean isAsc
    );
}