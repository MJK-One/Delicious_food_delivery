package com.dfdt.delivery.domain.order.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.order.application.service.query.OrderQueryService;
import com.dfdt.delivery.domain.order.presentation.dto.OrderReqDto;
import com.dfdt.delivery.domain.order.presentation.dto.OrderResDto;
import com.dfdt.delivery.domain.order.application.service.command.OrderCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController implements OrderControllerDocs {
    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;


    // API-001 주문 생성하기
    @PostMapping()
    public ResponseEntity<ApiResponseDto<OrderResDto.OrderMutationResponse>> createOrder(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody  OrderReqDto.Create createDTO)
    {
        return ApiResponseDto.success(
                201,
                "주문이 생성되었습니다.",
                orderCommandService.createOrder(customUserDetails.getUsername(),createDTO)
        );
    }

    // API-002 사용자의 주문 목록 조회
    @GetMapping()
    public ResponseEntity<ApiResponseDto<OrderResDto.CustomerOrderResponse>> getOrders(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ){
        return ApiResponseDto.success(
                200,
                "사용자의 주문 목록을 조회하였습니다.",
                null
        );
    }

    // API-003 주문 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponseDto<OrderResDto.GetOrderDetailResponse>> getOrderDetail(
            @PathVariable (value = "orderId") UUID orderId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ){
        return ApiResponseDto.success(
                200,
                "해당 주문을 조회하였습니다.",
                null
        );
    }

    // API-004 주문 수정하기
    @PatchMapping("/{orderId}")
    public ResponseEntity<ApiResponseDto<OrderResDto.OrderMutationResponse>> updateOrder(
            @PathVariable (value = "orderId") UUID orderId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody OrderReqDto.UpdateOrder updateDTO
    ) {
        return ApiResponseDto.success(
                200,
                "주문이 수정되었습니다.",
                orderCommandService.updateOrder(customUserDetails.getUsername(),orderId,updateDTO)
        );
    }

    // API-005 주문 삭제하기
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteOrder(
            @PathVariable (value = "orderId") UUID orderId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        return ApiResponseDto.success(
                200,
                "주문이 삭제되었습니다.",
                orderCommandService.deleteOrder(customUserDetails.getUsername(),orderId)
        );
    }

    // API-006 주문 상태 변경하기
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponseDto<OrderResDto.OrderMutationResponse>> updateOrderStatus(
            @PathVariable (value = "orderId") UUID orderId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody OrderReqDto.UpdateStatus updateStatusDTO
    ) {
        return ApiResponseDto.success(
                200,
                "주문 상태가 성공적으로 변경되었습니다.",
                orderCommandService.updateOrderStatus(customUserDetails.getUsername(),orderId,updateStatusDTO)
        );
    }

    // API-007 가게의 주문 목록 조회하기 ( 상태별 )
    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponseDto<OrderResDto.OwnerDashboardResponse>> getOrdersByOwner(
            @PathVariable (value = "storeId") UUID  storeId
    ) {
        return ApiResponseDto.success(
                200,
                "가게의 주문 목록이 조회되었습니다.",
                null
        );
    }
}
