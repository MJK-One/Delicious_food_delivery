package com.dfdt.delivery.domain.order.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.order.application.service.query.OrderQueryService;
import com.dfdt.delivery.domain.order.presentation.dto.OrderReqDto;
import com.dfdt.delivery.domain.order.presentation.dto.OrderResDto;
import com.dfdt.delivery.domain.order.application.service.command.OrderCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController implements OrderControllerDocs {
    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;

    // todo: AUTH로 바꾸기,UserDetail 생성 전까지 userId를 경로상에 포함시킵니다.

    // API-001 주문 생성하기
    @PostMapping("/{user_id}")
    public ResponseEntity<ApiResponseDto<OrderResDto.OrderMutationResponse>> createOrder(
            @PathVariable (value = "user_id") String userId,
            @Valid @RequestBody  OrderReqDto.Create createDTO)
    {
        return ApiResponseDto.success(
                201,
                "주문이 생성되었습니다.",
                null
        );
    }
    // todo: AUTH로 바꾸기,UserDetail 생성 전까지 userId를 경로상에 포함시킵니다.

    // API-002 사용자의 주문 목록 조회
    @GetMapping("/{user_id}")
    public ResponseEntity<ApiResponseDto<OrderResDto.CustomerOrderResponse>> getOrders(
            @PathVariable (value = "user_id") String userId
    ){
        return ApiResponseDto.success(
                200,
                "사용자의 주문 목록을 조회하였습니다.",
                null
        );
    }
    // todo: AUTH로 바꾸기,UserDetail 생성 전까지 userId를 경로상에 포함시킵니다.

    // API-003 주문 상세 조회
    @GetMapping("/{order_id}/{user_id}")
    public ResponseEntity<ApiResponseDto<OrderResDto.GetOrderDetailResponse>> getOrderDetail(
            @PathVariable (value = "order_id") String orderId,
            @PathVariable (value = "user_id") String  userId
    ){
        return ApiResponseDto.success(
                200,
                "해당 주문을 조회하였습니다.",
                null
        );
    }
    // todo: AUTH로 바꾸기,UserDetail 생성 전까지 userId를 경로상에 포함시킵니다.

    // API-004 주문 수정하기
    @PatchMapping("/{order_id}/update/{user_id}")
    public ResponseEntity<ApiResponseDto<OrderResDto.OrderMutationResponse>> updateOrder(
            @PathVariable (value = "order_id") String orderId,
            @PathVariable (value = "user_id") String  userId,
            @Valid @RequestBody OrderReqDto.UpdateOrder updateDTO
    ) {
        return ApiResponseDto.success(
                200,
                "주문이 수정되었습니다.",
                null
        );
    }
    // todo: AUTH로 바꾸기,UserDetail 생성 전까지 userId를 경로상에 포함시킵니다.

    // API-005 주문 삭제하기
    @DeleteMapping("/{order_id}/delete/{user_id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteOrder(
            @PathVariable (value = "order_id") String orderId,
            @PathVariable (value = "user_id") String  userId
    ) {
        return ApiResponseDto.success(
                200,
                "주문이 삭제되었습니다.",
                null
        );
    }
    // todo: AUTH로 바꾸기,UserDetail 생성 전까지 userId를 경로상에 포함시킵니다.

    // API-006 주문 수락하기
    @PatchMapping("/{order_id}/accept/{user_id}")
    public ResponseEntity<ApiResponseDto<OrderResDto.OrderMutationResponse>> acceptOrder(
            @PathVariable (value = "order_id") String orderId,
            @PathVariable (value = "user_id") String  userId
    ) {
        return ApiResponseDto.success(
                200,
                "주문이 수락되었습니다.",
                null
        );
    }
    // todo: AUTH로 바꾸기,UserDetail 생성 전까지 userId를 경로상에 포함시킵니다.

    // API-007 주문 거절하기
    @PatchMapping("/{order_id}/reject/{user_id}")
    public ResponseEntity<ApiResponseDto<OrderResDto.OrderMutationResponse>> rejectOrder(
            @PathVariable (value = "order_id") String orderId,
            @PathVariable (value = "user_id") String  userId
    ) {
        return ApiResponseDto.success(
                200,
                "주문이 거절되었습니다.",
                null
        );
    }
    // todo: AUTH로 바꾸기,UserDetail 생성 전까지 userId를 경로상에 포함시킵니다.

    // API-008 주문 상태 변경하기
    @PatchMapping("/{order_id}/status/{user_id}")
    public ResponseEntity<ApiResponseDto<OrderResDto.OrderMutationResponse>> updateOrderStatus(
            @PathVariable (value = "order_id") String orderId,
            @PathVariable (value = "user_id") String  userId,
            @Valid @RequestBody OrderReqDto.UpdateStatus updateStatusDTO
    ) {
        return ApiResponseDto.success(
                200,
                "주문 상태가 성공적으로 변경되었습니다.",
                null
        );
    }
    // todo: AUTH로 바꾸기,UserDetail 생성 전까지 userId를 경로상에 포함시킵니다.

    // API-009 가게의 주문 목록 조회하기 ( 상태별 )
    @GetMapping("/store/{store_id}")
    public ResponseEntity<ApiResponseDto<OrderResDto.OwnerDashboardResponse>> getOrdersByOwner(
            @PathVariable (value = "store_id") String  storeId
    ) {
        return ApiResponseDto.success(
                200,
                "가게의 주문 목록이 조회되었습니다.",
                null
        );
    }
}
