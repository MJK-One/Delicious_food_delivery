package com.dfdt.delivery.domain.store.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.store.application.service.command.StoreCommandService;
import com.dfdt.delivery.domain.store.application.service.query.StoreQueryService;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreCreateReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreStatusReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreUpdateReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.response.*;
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
@RequestMapping("/stores")
public class StoreController implements StoreControllerDocs{

    private final StoreQueryService storeQueryService;
    private final StoreCommandService storeCommandService;

    /**
     * 가게 단일 조회
     * GET /api/v1/stores/{store_id}
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponseDto<StoreResDto>> getStore(@PathVariable("storeId") UUID storeId) {
        StoreResDto store = storeQueryService.getStore(storeId);

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
    @GetMapping()
    public ResponseEntity<ApiResponseDto<StorePageResDto>> getStores(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "isAsc", defaultValue = "true") boolean isAsc,
            @RequestParam(value = "category", required = false) UUID category,
            @RequestParam(value = "name", required = false) String name
    ) {
        Page<StoreResDto> stores = storeQueryService.getStores(page, size, sortBy, isAsc, category, name);
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
        Page<StoreAdminResDto> stores = storeQueryService.getStoresAdmin(page, size, sortBy, isAsc, category, name, isDeleted);
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
    @PostMapping()
    public ResponseEntity<ApiResponseDto<StoreCreateResDto>> createStore(@Valid @RequestBody StoreCreateReqDto request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        StoreCreateResDto createdStore = storeCommandService.createStore(request, userDetails);

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
    @PutMapping("/{storeId}")
    public ResponseEntity<ApiResponseDto<StoreUpdateResDto>> updateStore(@PathVariable("storeId") UUID storeId, @Valid @RequestBody StoreUpdateReqDto request,
                                                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        StoreUpdateResDto updatedStore = storeCommandService.updateStore(storeId, request, userDetails);

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
    @DeleteMapping("/{storeId}")
    public ResponseEntity<ApiResponseDto<Object>> deleteStore(@PathVariable("storeId") UUID storeId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        storeCommandService.deleteStore(storeId, userDetails);

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
    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    @PatchMapping("/{storeId}/open")
    public ResponseEntity<ApiResponseDto<Object>> changeStoreOpenStatus(@PathVariable("storeId") UUID storeId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        storeCommandService.changeIsOpen(storeId, userDetails);

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
    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponseDto<List<MyStoreResDto>>> getMyStores(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MyStoreResDto> stores = storeCommandService.getMyStores(userDetails.getUsername());

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
    @PatchMapping("/{storeId}/restore")
    public ResponseEntity<ApiResponseDto<Object>> restoreStore(@PathVariable("storeId") UUID storeId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        storeCommandService.restoreStore(storeId, userDetails);

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
    @PatchMapping("/{storeId}/status")
    public ResponseEntity<ApiResponseDto<Object>> changeStoreApprovalStatus(@PathVariable("storeId") UUID storeId, @RequestBody StoreStatusReqDto request,
                                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        StoreStatusResDto storeStatus = storeCommandService.changeStatus(storeId, request, userDetails);

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
    @GetMapping("/status/request")
    public ResponseEntity<ApiResponseDto<StoreRequestPageResDto>> getRequestedStores(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "isAsc", defaultValue = "true") boolean isAsc
    ) {
        Page<StoreStatusRequestResDto> stores = storeQueryService.getRequestedStores(page, size, sortBy, isAsc);
        StoreRequestPageResDto response = new StoreRequestPageResDto(stores);

        return ApiResponseDto.success(
                HttpStatus.OK.value(),
                "승인 대기 가게가 성공적으로 조회되었습니다.",
                response
        );
    }
}

