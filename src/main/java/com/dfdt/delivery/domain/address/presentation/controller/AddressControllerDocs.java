package com.dfdt.delivery.domain.address.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.address.presentation.dto.AddressRequest;
import com.dfdt.delivery.domain.address.presentation.dto.AddressResponse;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@Tag(name = "Address (배송지)", description = "배송지 등록, 조회, 수정 및 삭제를 담당합니다.")
public interface AddressControllerDocs {

    @Operation(summary = "API-Address-001 배송지 등록", description = "새로운 배송지를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "입력 형식 오류", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "입력값 검증 실패", value = AddressErrorDocs.INVALID_INPUT_VALUE)
            })),
            @ApiResponse(responseCode = "404", description = "지역 정보 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "지역 조회 실패", value = AddressErrorDocs.REGION_NOT_FOUND)
            }))
    })
    ResponseEntity<ApiResponseDto<AddressResponse>> createAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddressRequest.Create request);

    @Operation(summary = "API-Address-002 배송지 목록 조회", description = "로그인한 사용자의 모든 배송지 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<ApiResponseDto<List<AddressResponse>>> getMyAddresses(
            @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "API-Address-003 배송지 상세 조회", description = "특정 배송지의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "소유권 없음", value = AddressErrorDocs.ADDRESS_ACCESS_DENIED)
            })),
            @ApiResponse(responseCode = "404", description = "배송지 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "배송지 조회 실패", value = AddressErrorDocs.ADDRESS_NOT_FOUND)
            }))
    })
    ResponseEntity<ApiResponseDto<AddressResponse>> getAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            UUID addressId);

    @Operation(summary = "API-Address-004 배송지 수정", description = "기존 배송지 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "입력 형식 오류", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "입력값 검증 실패", value = AddressErrorDocs.INVALID_INPUT_VALUE)
            })),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "소유권 없음", value = AddressErrorDocs.ADDRESS_ACCESS_DENIED)
            })),
            @ApiResponse(responseCode = "404", description = "조회 실패", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "배송지 조회 실패", value = AddressErrorDocs.ADDRESS_NOT_FOUND),
                    @ExampleObject(name = "지역 조회 실패", value = AddressErrorDocs.REGION_NOT_FOUND)
            }))
    })
    ResponseEntity<ApiResponseDto<AddressResponse>> updateAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            UUID addressId,
            @Valid @RequestBody AddressRequest.Update request);

    @Operation(summary = "API-Address-005 배송지 삭제", description = "배송지를 논리적으로 삭제(Soft Delete)합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "소유권 없음", value = AddressErrorDocs.ADDRESS_ACCESS_DENIED)
            })),
            @ApiResponse(responseCode = "404", description = "배송지 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "배송지 조회 실패", value = AddressErrorDocs.ADDRESS_NOT_FOUND)
            }))
    })
    ResponseEntity<ApiResponseDto<Void>> deleteAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            UUID addressId);
}
