package com.dfdt.delivery.domain.order.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.order.presentation.dto.OrderReqDto;
import com.dfdt.delivery.domain.order.presentation.dto.OrderResDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Order (주문)", description = "주문 생성 및 상태 관리를 담당합니다.")
public interface OrderControllerDocs {

    @Operation(summary = "API-001 주문 생성", description = "사용자가 장바구니의 상품으로 주문을 요청합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "주문 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 주문 요청", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "주문 수량 오류", value = OrderErrorDocs.INVALID_QUANTITY),
                    @ExampleObject(name = "가게 상품 불일치", value = OrderErrorDocs.SHOP_MISMATCH),
                    @ExampleObject(name = "가격 변동 발생", value = OrderErrorDocs.PRICE_CHANGED),
                    @ExampleObject(name = "재고 부족", value = OrderErrorDocs.OUT_OF_STOCK)
            })),
            @ApiResponse(responseCode = "403", description = "권한 부족/판매 중지", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "판매 중지 상품 포함", value = OrderErrorDocs.PRODUCT_NOT_FOR_SALE)
            })),
            @ApiResponse(responseCode = "404", description = "찾을 수 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "상품을 찾을 수 없음"),
                    @ExampleObject(name = "주소를 찾을 수 없음"),
                    @ExampleObject(name = "가게를 찾을 수 없음"),
            })),
    })
    ResponseEntity<ApiResponseDto<OrderResDto.OrderMutationResponse>> createOrder(
            @PathVariable(value = "user_id") String userId,
            @Valid @RequestBody OrderReqDto.Create createDTO);


    @Operation(summary = "API-002 사용자의 주문 목록 조회", description = "본인의 전체 주문 내역을 리스트 형식으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    ResponseEntity<ApiResponseDto<OrderResDto.CustomerOrderResponse>> getOrders(
            @PathVariable(value = "user_id") String userId);


    @Operation(summary = "API-003 주문 상세 조회", description = "특정 주문에 대한 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "권한 오류", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "주문에 접근 권한 없음", value = OrderErrorDocs.ACCESS_DENIED)
            })),
            @ApiResponse(responseCode = "404", description = "찾을 수 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "주문을 찾을 수 없음", value = OrderErrorDocs.ORDER_NOT_FOUND)
            }))
    })
    ResponseEntity<ApiResponseDto<OrderResDto.GetOrderDetailResponse>> getOrderDetail(
            @PathVariable(value = "order_id") String orderId,
            @PathVariable(value = "user_id") String userId);


    @Operation(summary = "API-004 주문 수정하기", description = "배달지 정보나 요청사항 등 주문 내용을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 주문 요청", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "이미 처리된 주문", value = OrderErrorDocs.ALREADY_PROCESSED)
            })),
            @ApiResponse(responseCode = "401", description = "권한 오류", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "주문에 접근 권한 없음", value = OrderErrorDocs.ACCESS_DENIED)
            })),
            @ApiResponse(responseCode = "404", description = "찾을 수 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "주문을 찾을 수 없음", value = OrderErrorDocs.ACCESS_DENIED)
            })),
    })
    ResponseEntity<ApiResponseDto<OrderResDto.OrderMutationResponse>> updateOrder(
            @PathVariable(value = "order_id") String orderId,
            @PathVariable(value = "user_id") String userId,
            @Valid @RequestBody OrderReqDto.UpdateOrder updateDTO
    );


    @Operation(summary = "API-005 주문 삭제하기", description = "주문을 취소하고 내역에서 삭제(Soft Delete)합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 주문 요청", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "이미 처리된 주문", value = OrderErrorDocs.ALREADY_PROCESSED)
            })),
            @ApiResponse(responseCode = "401", description = "권한 오류", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "주문에 접근 권한 없음", value = OrderErrorDocs.ACCESS_DENIED)
            })),
            @ApiResponse(responseCode = "404", description = "찾을 수 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "주문을 찾을 수 없음", value = OrderErrorDocs.ACCESS_DENIED)
            })),
    })
    ResponseEntity<ApiResponseDto<Void>> deleteOrder(
            @PathVariable(value = "order_id") String orderId,
            @PathVariable(value = "user_id") String userId);


    @Operation(summary = "API-006 주문 수락하기", description = "사장님이 접수된 주문을 수락하여 조리를 시작합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수락 성공"),            
            @ApiResponse(responseCode = "400", description = "잘못된 주문 요청", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "이미 처리된 주문", value = OrderErrorDocs.ALREADY_PROCESSED), 
                    @ExampleObject(name = "사용자 결제 전", value = OrderErrorDocs.PAYMENT_REQUIRED)
            })),
            @ApiResponse(responseCode = "401", description = "권한 오류", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "주문에 접근 권한 없음", value = OrderErrorDocs.ACCESS_DENIED)
            })),
            @ApiResponse(responseCode = "404", description = "찾을 수 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "주문을 찾을 수 없음", value = OrderErrorDocs.ACCESS_DENIED)
            })),
    })
    ResponseEntity<ApiResponseDto<OrderResDto.OrderMutationResponse>> acceptOrder(
            @PathVariable(value = "order_id") String orderId,
            @PathVariable(value = "user_id") String userId);


    @Operation(summary = "API-007 주문 거절하기", description = "재고 부족 등의 사유로 사장님이 주문을 거절합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "거절 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 주문 요청", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "이미 처리된 주문", value = OrderErrorDocs.ALREADY_PROCESSED)
            })),
            @ApiResponse(responseCode = "401", description = "권한 오류", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "주문에 접근 권한 없음", value = OrderErrorDocs.ACCESS_DENIED)
            })),
            @ApiResponse(responseCode = "404", description = "찾을 수 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "주문을 찾을 수 없음", value = OrderErrorDocs.ACCESS_DENIED)
            })),
    })
    ResponseEntity<ApiResponseDto<OrderResDto.OrderMutationResponse>> rejectOrder(
            @PathVariable(value = "order_id") String orderId,
            @PathVariable(value = "user_id") String userId);


    @Operation(summary = "API-008 주문 상태 변경하기", description = "준비 중 -> 배달 중 -> 배달 완료 등으로 상태를 수동 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 주문 요청", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "이미 처리된 주문", value = OrderErrorDocs.ALREADY_PROCESSED)
            })),
            @ApiResponse(responseCode = "401", description = "권한 오류", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "주문에 접근 권한 없음", value = OrderErrorDocs.ACCESS_DENIED)
            })),
            @ApiResponse(responseCode = "404", description = "찾을 수 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "주문을 찾을 수 없음", value = OrderErrorDocs.ACCESS_DENIED)
            })),
    })
    ResponseEntity<ApiResponseDto<OrderResDto.OrderMutationResponse>> updateOrderStatus(
            @PathVariable(value = "order_id") String orderId,
            @PathVariable(value = "user_id") String userId,
            @Valid @RequestBody OrderReqDto.UpdateStatus updateStatusDTO
    );


    @Operation(summary = "API-009 가게의 주문 목록 조회하기", description = "가게 ID를 기준으로 들어온 모든 주문을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "가게 정보를 찾을 수 없음")
    })
    ResponseEntity<ApiResponseDto<OrderResDto.OwnerDashboardResponse>> getOrdersByOwner(
            @PathVariable(value = "store_id") String store_id);
}