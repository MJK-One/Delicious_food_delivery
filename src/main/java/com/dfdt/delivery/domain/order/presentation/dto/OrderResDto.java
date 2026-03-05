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
            Integer pendingCount,
            Integer paidCount,
            Integer cookingCount,
            Integer doneCount,
            Integer canceledCount
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
            Long totalPrice,
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
            Long unitPrice,
            Long totalPrice
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
            String productName
    ){}
    
    // 생성 / 수정 공통 응답
    @Builder
    public record OrderMutationResponse(
            UUID orderId,
            OrderStatus orderStatus,
            String orderAddress,
            Integer totalPrice,
            String representativeProductName,
            String orderRequestMessage,
            OffsetDateTime updatedAt
    ){}

    // 페이징 응답
    @Builder
    public record PaginationInfo(
            OffsetDateTime nextCursor,
            Integer size,
            boolean hasNext
    ){}
}