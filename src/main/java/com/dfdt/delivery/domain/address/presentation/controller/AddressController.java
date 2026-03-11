package com.dfdt.delivery.domain.address.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.address.application.service.AddressService;
import com.dfdt.delivery.domain.address.presentation.dto.AddressRequest;
import com.dfdt.delivery.domain.address.presentation.dto.AddressResponse;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 배송지 관련 API를 제공하는 컨트롤러.
 * 사용자의 배송지 등록, 조회, 수정, 삭제 기능을 담당.
 */
@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController implements AddressControllerDocs {

    private final AddressService addressService;

    /**
     * 새로운 배송지를 등록.
     * @param userDetails 인증된 사용자 정보
     * @param request 배송지 등록 요청 정보 (지역 ID, 상세 주소 등)
     * @return 등록된 배송지 정보
     */
    @PostMapping
    public ResponseEntity<ApiResponseDto<AddressResponse>> createAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddressRequest.Create request) {
        AddressResponse response = addressService.createAddress(userDetails.getUsername(), request);
        return ApiResponseDto.success(201, "배송지가 등록되었습니다.", response);
    }

    /**
     * 로그인한 사용자의 모든 배송지 목록을 조회. (삭제되지 않은 주소만 조회)
     * @param userDetails 인증된 사용자 정보
     * @return 배송지 목록 응답 DTO 리스트
     */
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<AddressResponse>>> getMyAddresses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AddressResponse> response = addressService.getMyAddresses(userDetails.getUsername());
        return ApiResponseDto.success(200, "배송지 목록 조회가 성공하였습니다.", response);
    }

    /**
     * 특정 배송지 상세 정보를 조회.
     * @param userDetails 인증된 사용자 정보 (소유권 확인용)
     * @param addressId 조회할 배송지 ID (UUID)
     * @return 배송지 상세 정보 응답 DTO
     */
    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponseDto<AddressResponse>> getAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID addressId) {
        AddressResponse response = addressService.getAddress(userDetails.getUsername(), addressId);
        return ApiResponseDto.success(200, "배송지 조회가 성공하였습니다.", response);
    }

    /**
     * 기존 배송지 정보를 수정.
     * @param userDetails 인증된 사용자 정보 (소유권 확인용)
     * @param addressId 수정할 배송지 ID (UUID)
     * @param request 수정할 배송지 정보
     * @return 수정된 배송지 정보 응답 DTO
     */
    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponseDto<AddressResponse>> updateAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID addressId,
            @Valid @RequestBody AddressRequest.Update request) {
        AddressResponse response = addressService.updateAddress(userDetails.getUsername(), addressId, request);
        return ApiResponseDto.success(200, "배송지가 수정되었습니다.", response);
    }

    /**
     * 배송지를 삭제합니다. (Soft Delete)
     * @param userDetails 인증된 사용자 정보 (소유권 확인용)
     * @param addressId 삭제할 배송지 ID (UUID)
     * @return 삭제 완료 메시지
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID addressId) {
        addressService.deleteAddress(userDetails.getUsername(), addressId);
        return ApiResponseDto.success(200, "배송지가 삭제되었습니다.", null);
    }
}
