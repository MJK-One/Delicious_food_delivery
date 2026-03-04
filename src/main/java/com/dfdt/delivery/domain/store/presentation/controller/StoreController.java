package com.dfdt.delivery.domain.store.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.StorePageResDto;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreCreateReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreStatusReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreUpdateReqDto;
import com.dfdt.delivery.domain.store.application.service.StoreService;
import com.dfdt.delivery.domain.store.presentation.dto.response.*;
import com.dfdt.delivery.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "가게 API")
@RequestMapping("/stores")
public class StoreController {

    private final StoreService storeService;

    /**
     * 가게 단일 조회
     * GET /api/v1/stores/{store_id}
     */
    @Operation(summary = "가게 단일 조회",description = "가게 한 건을 조회합니다.")
    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponseDto<StoreResDto>> getStore(@PathVariable("storeId") UUID storeId) {
        StoreResDto store = storeService.getStore(storeId);

        return ApiResponseDto.success(
                    HttpStatus.OK.value(),
                    store.getName() + " 가게 조회가 완료되었습니다.",
                    store
        );
    }

    /**
     * 가게 조회
     * GET /api/v1/stores
     */
    @Operation(summary = "가게 조회",description = "가게 목록을 조회합니다.")
    @GetMapping()
    public ResponseEntity<ApiResponseDto<StorePageResDto>> getStores(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "isAsc", defaultValue = "true") boolean isAsc,
            @RequestParam(value = "category", required = false) UUID category,
            @RequestParam(value = "name", required = false) String name
    ) {
        Page<StoreResDto> stores = storeService.getStores(page, size, sortBy, isAsc, category, name);
        StorePageResDto response = new StorePageResDto(stores);

        return ApiResponseDto.success(
                    HttpStatus.OK.value(),
                    "가게 목록이 성공적으로 조회되었습니다.",
                    response
        );
    }

    /**
     * 가게 목록 조회(관리자용)
     * 주문 가능 지역, 승인 여부, 삭제 여부 상관 없이 불러옴
     * GET /api/v1/stores/admin
     */
    @Operation(summary = "가게 조회(관리자용)",description = "가게 목록을 조회합니다.(삭제된 건 포함)")
    @PreAuthorize("hasAnyRole('MASTER')")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponseDto<StoreAdminPageResDto>> getStoresAdmin(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "isAsc", defaultValue = "true") boolean isAsc,
            @RequestParam(value = "category", required = false) UUID category,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "isDeleted", defaultValue = "false") Boolean isDeleted
    ) {
        Page<StoreAdminResDto> stores = storeService.getStoresAdmin(page, size, sortBy, isAsc, category, name, isDeleted);
        StoreAdminPageResDto response = new StoreAdminPageResDto(stores);

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "가게 목록이 성공적으로 조회되었습니다.",
                response
        );
    }

    /**
     * 가게 생성
     * POST /api/v1/stores
     */
    @Operation(summary = "가게 생성",description = "가게를 생성합니다.")
    @PostMapping()
    public ResponseEntity<ApiResponseDto<StoreCreateResDto>> createStore(@Valid @RequestBody StoreCreateReqDto request, @AuthenticationPrincipal User user) {
        StoreCreateResDto createdStore = storeService.createStore(request, user);

        return ApiResponseDto.success(
                    HttpStatus.CREATED.value(),
                    "승인 대기중입니다.",
                    createdStore
        );
    }

    /**
     * 가게 수정
     * PUT /api/v1/stores/{store_id}
     */
    @Operation(summary = "가게 수정",description = "가게를 수정합니다.")
    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    @PutMapping("/{storeId}")
    public ResponseEntity<ApiResponseDto<StoreUpdateResDto>> updateStore(@PathVariable("storeId") UUID storeId, @Valid @RequestBody StoreUpdateReqDto request, @AuthenticationPrincipal User user) {
        StoreUpdateResDto updatedStore = storeService.updateStore(storeId, request, user);

        return ApiResponseDto.success(
                    HttpStatus.OK.value(),
                    "가게 정보가 성공적으로 수정되었습니다.",
                    updatedStore
        );
    }

    /**
     * 가게 삭제 (Soft Delete 가정)
     * DELETE /api/v1/stores/{store_id}
     */
    @Operation(summary = "가게 삭제(Soft Delete)",description = "가게를 삭제합니다.")
    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    @DeleteMapping("/{storeId}")
    public ResponseEntity<ApiResponseDto<Object>> deleteStore(@PathVariable("storeId") UUID storeId, @AuthenticationPrincipal User user) {
        storeService.deleteStore(storeId, user);

        return ApiResponseDto.success(
                    HttpStatus.OK.value(),
                    "가게가 성공적으로 삭제되었습니다.",
                    null
        );
    }

    /**
     * 영업 상태 변경
     * PATCH /api/v1/stores/{store_id}
     * isOpen 변경
     */
    @Operation(summary = "가게 영업 상태 변경",description = "영업 상태를 변경합니다.(isOpen 변경)")
    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    @PatchMapping("/{storeId}/open")
    public ResponseEntity<ApiResponseDto<Object>> changeStoreOpenStatus(@PathVariable("storeId") UUID storeId, @AuthenticationPrincipal User user) {
        storeService.changeIsOpen(storeId, user);

        return ApiResponseDto.success(
                    HttpStatus.OK.value(),
                    "영업 상태가 변경되었습니다.",
                    null
        );
    }

    /**
     * 본인 가게 조회
     * GET /api/v1/stores/me
     * (인증 정보에서 회원 id를 꺼내온다고 가정)
     */
    @Operation(summary = "본인 가게 조회",description = "본인이 소유한 가게를 조회합니다.")
    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponseDto<List<MyStoreResDto>>> getMyStores(@AuthenticationPrincipal User user) {
        List<MyStoreResDto> stores = storeService.getMyStores(user.getUsername());

        return ApiResponseDto.success(
                    HttpStatus.OK.value(),
                    "가게가 성공적으로 조회되었습니다.",
                    stores
        );
    }

    /**
     * 가게 복구
     * PATCH /api/v1/stores/{store_id}/restore
     */
    @Operation(summary = "가게 복구",description = "가게를 복구합니다.")
    @PreAuthorize("hasRole('MASTER')")
    @PatchMapping("/{storeId}/restore")
    public ResponseEntity<ApiResponseDto<Object>> restoreStore(@PathVariable("storeId") UUID storeId, @AuthenticationPrincipal User user) {
        storeService.restoreStore(storeId, user);

        return ApiResponseDto.success(
                    HttpStatus.OK.value(),
                    "가게가 성공적으로 복구되었습니다.",
                    null
        );
    }

    /**
     * 가게 승인 여부 변경
     * PATCH /api/v1/stores/{store_id}/status
     * 예: APPROVED / REJECTED 등
     */
    @Operation(summary = "가게 승인 여부 변경",description = "가게 승인 여부를 변경합니다.(예: APPROVED, SUSPENDED)")
    @PreAuthorize("hasRole('MASTER')")
    @PatchMapping("/{storeId}/status")
    public ResponseEntity<ApiResponseDto<Object>> changeStoreApprovalStatus(@PathVariable("storeId") UUID storeId, @RequestBody StoreStatusReqDto request, @AuthenticationPrincipal User user) {
        StoreStatusResDto storeStatus = storeService.changeStatus(storeId, request, user);

        return ApiResponseDto.success(
                    HttpStatus.OK.value(),
                    request.getMessage(),
                    storeStatus
        );
    }

    /**
     * 가게 승인 대기 목록 조회
     * GET /api/v1/stores/status/request
     */
    @Operation(summary = "가게 승인 대기 목록 조회",description = "승인 대기중인 가게들의 목록을 조회합니다.(status == REQUESTED)")
    @GetMapping("/status/request")
    public ResponseEntity<ApiResponseDto<List<StoreStatusRequestResDto>>> getRequestedStores() {
        List<StoreStatusRequestResDto> stores = storeService.getRequestedStores();

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "승인 대기 가게가 성공적으로 조회되었습니다.",
                stores
        );
    }
}

