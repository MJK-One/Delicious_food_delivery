package com.dfdt.delivery.domain.store.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.StorePageResDto;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreCreateReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreStatusReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreUpdateReqDto;
import com.dfdt.delivery.domain.store.application.service.StoreService;
import com.dfdt.delivery.domain.store.presentation.dto.response.*;
import com.dfdt.delivery.domain.user.entity.User;
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
@RequestMapping("/api/v1")
public class StoreController {

    private final StoreService storeService;

    /**
     * 가게 단일 조회
     * GET /api/v1/stores/{store_id}
     */
    @GetMapping("/stores/{storeId}")
    public ResponseEntity<ApiResponseDto<StoreResDto>> getStore(@PathVariable("storeId") UUID storeId) {
        StoreResDto store = storeService.getStore(storeId);

        return ApiResponseDto.success(
                    HttpStatus.OK.value(),
                    store.getName() + " 가게 조회가 완료되었습니다.",
                    store
        );
    }

    /**
     * 가게 목록 조회
     * GET /api/v1/stores
     */
    @GetMapping("/stores")
    public ResponseEntity<ApiResponseDto<StorePageResDto>> getStores(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "isAsc", defaultValue = "false") boolean isAsc,
            @RequestParam(value = "category") UUID category,
            @RequestParam(value = "name") String name
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
     * 가게 생성
     * POST /api/v1/stores
     */
    @PostMapping("/stores")
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
    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    @PutMapping("/stores/{storeId}")
    public ResponseEntity<ApiResponseDto<StoreUpdateResDto>> updateStore(
            @PathVariable("storeId") UUID storeId,
            @Valid @RequestBody StoreUpdateReqDto request,
            @AuthenticationPrincipal User user
    ) {
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
    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    @DeleteMapping("/stores/{storeId}")
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
     * 예: 영업중/휴무/폐업 등 상태 변경
     */
    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    @PatchMapping("/stores/{storeId}")
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
     * GET /api/v1/me/stores
     * (인증 정보에서 회원 id를 꺼내온다고 가정)
     */
    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    @GetMapping("/me/stores")
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
    @PreAuthorize("hasRole('MASTER')")
    @PatchMapping("/stores/{storeId}/restore")
    public ResponseEntity<ApiResponseDto<Object>> restoreStore(@PathVariable("storeId") UUID storeId, @AuthenticationPrincipal User user) {
        storeService.restoreStore(storeId);

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
    @PreAuthorize("hasRole('MASTER')")
    @PatchMapping("/stores/{storeId}/status")
    public ResponseEntity<ApiResponseDto<Object>> changeStoreApprovalStatus(
            @PathVariable("storeId") UUID storeId,
            @RequestBody StoreStatusReqDto request,
            @AuthenticationPrincipal User user
    ) {
        StoreStatusResDto storeStatus = storeService.changeStatus(storeId, request);

        return ApiResponseDto.success(
                    HttpStatus.OK.value(),
                    request.getMessage(),
                    storeStatus
        );
    }
}

