package com.dfdt.delivery.domain.order.presentation.dto;

import com.dfdt.delivery.domain.order.domain.enums.OrderStatus;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderResDto {

    // 사용자용 GET
    @Builder
    public record CustomerOrderResponse(
            List<CustomerOrderSummary> orderList,
            PaginationInfo pagination
    ){}

    @Builder
    public record CustomerOrderSummary(
            UUID orderId,
            UUID orderStoreId,
            String orderStoreName,
            OffsetDateTime orderedAt,
            Integer totalPrice,
            Integer totalQuantity,
            String representativeProductName,
            String orderAddress,
            OrderStatus orderStatus
    ){}
    // 가게용 GET
    @Builder
    public record OwnerDashboardResponse(
            OrderSummaryCount summary,
            Map<OrderStatus, List<OwnerOrderUnit>> statusGroups,
            PaginationInfo pagination
    ){}

    @Builder
    public record OrderSummaryCount(
            Integer newOrderCount,     // [신규] PAID (수락 대기 중인 주문)
            Integer cookingCount,      // [조리 중] ACCEPTED (현재 만들고 있는 주문)
            Integer deliveryCount,     // [배달 중] COOKING_DONE + DELIVERING (전달 대기 및 배달 중)
            Integer completedCount,    // [완료] DELIVERED + COMPLETED (완료)
            Integer abortedCount       // [취소/거절] REJECTED + CANCELED (중단된 주문)
    ){}

    @Builder
    public record OwnerOrderUnit(
            UUID orderId,
            OffsetDateTime orderTime,
            Integer totalPrice,
            Integer totalQuantity,
            String orderAddress,
            List<ProductSimpleInfo> products
    ){}

    // 주문 상세 정보
    @Builder
    public record GetOrderDetailResponse(
            UUID orderId,
            UUID orderStoreId,
            String orderStoreName,
            OffsetDateTime orderedAt,
            Integer totalPrice,
            Integer totalQuantity,
            String orderAddress,
            OrderStatus orderStatus,
            List<OrderItemResponse> orderItemDetails,
            List<OrderStatusHistoryDetail> orderStatusHistoryDetails
    ){}

    @Builder
    public record OrderItemResponse(
            UUID productId,
            String productName,
            Integer quantity,
            Integer unitPrice,
            Integer totalPrice
    ){}

    @Builder
    public record OrderStatusHistoryDetail(
            UUID orderStatusHistoryId,
            OrderStatus fromStatus,
            OrderStatus nowStatus,
            String changedReason,
            OffsetDateTime changedAt
    ){}

    @Builder
    public record ProductSimpleInfo(
            UUID productId,
            Integer size,
            String productName
    ){}
    
    // 생성 / 수정 공통 응답
    @Builder
    public record OrderMutationResponse(
            UUID orderId,
            OrderStatus orderStatus,
            String orderAddress,
            Integer totalQuantity,
            Integer totalPrice,
            String representativeProductName,
            String orderRequestMessage,
            OffsetDateTime updatedAt
    ){}

    // 페이징 응답
    @Builder
    public record PaginationInfo(
            String nextCursor,
            Integer size,
            boolean hasNext
    ){}
}