package com.dfdt.delivery.domain.order.application.converter;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.UpdateAudit;
import com.dfdt.delivery.domain.address.domain.entity.Address;
import com.dfdt.delivery.domain.order.application.dto.TimeIdCursor;
import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.order.domain.entity.OrderItem;
import com.dfdt.delivery.domain.order.domain.entity.OrderStatusHistory;
import com.dfdt.delivery.domain.order.domain.enums.OrderStatus;
import com.dfdt.delivery.domain.order.presentation.dto.OrderResDto;
import com.dfdt.delivery.domain.product.domain.entity.Product;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.user.domain.entity.User;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderConverter {

    // DTO -> Entity
    public static Order toOrder(User user, Address address, Store store, String requestMessage)
    {
        String addressSnapshot = String.format("[%s / %s] %s %s (메모: %s)",
            address.getReceiverName(),
            address.getReceiverPhone(),
            address.getAddressLine1(),
            address.getAddressLine2() != null ? address.getAddressLine2() : "",
            address.getDeliveryMemo() != null ? address.getDeliveryMemo() : "없음"
    );

        // 엔티티 빌드 (초기화 로직 포함)
        Order order = Order.builder()
                .user(user)
                .address(address)
                .store(store)
                .deliveryAddressSnapshot(addressSnapshot)
                .orderRequestMessage(requestMessage)
                .totalPrice(0)
                .totalQuantity(0)
                .status(OrderStatus.PENDING)
                .createdAudit(CreateAudit.now(user.getName()))
                .updateAudit(UpdateAudit.empty())
                .build();


        // 초기 히스토리 추가 (이 부분만 Order 엔티티의 메서드를 호출)
        order.getUpdateAudit().touch(user.getName());
        order.addStatusHistory(null,OrderStatus.PENDING,"주문 생성");
        return order;
    }

    public static OrderItem toOrderItem(Product product, Integer quantity) {
        return OrderItem.builder()
                .productId(product.getProductId())
                .productNameSnapshot(product.getName())
                .unitPriceSnapshot(product.getPrice())
                .quantity(quantity)
                .totalPrice(product.getPrice() * quantity)
                .build();
    }

    // Entity -> OrderMutationResponse 변환
    public static OrderResDto.OrderMutationResponse toMutationResponse(Order order) {
        return OrderResDto.OrderMutationResponse.builder()
                .orderId(order.getOrderId())
                .orderAddress(order.getDeliveryAddressSnapshot())
                .representativeProductName(generateOrderSummary(order.getOrderItems())) // 로직 추가
                .totalPrice(order.getTotalPrice())
                .totalQuantity(order.getTotalQuantity())
                .orderRequestMessage(order.getOrderRequestMessage())
                .orderStatus(order.getStatus())
                .updatedAt(order.getUpdateAudit().getUpdatedAt())
                .build();
    }

    // 헬퍼 메서드로 분리
    private static String generateOrderSummary(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return "주문 항목 없음";
        }

        // 첫 번째 상품명 추출 (p_product 테이블의 name 컬럼 참조)
        String firstProductName = items.getFirst().getProductNameSnapshot();
        int otherCount = items.size() - 1;

        return (otherCount > 0)
                ? String.format("%s 외 %d건", firstProductName, otherCount)
                : firstProductName;
    }

    public static OrderResDto.CustomerOrderResponse toCustomerOrderResponse(List<Order> orders,int pageSize) {
        boolean hasNext = orders.size() > pageSize;

        List<Order> displayOrders = hasNext
                ? orders.subList(0, pageSize)
                : orders;
        String nextCursor = null;
        if (!displayOrders.isEmpty()) {
            TimeIdCursor cursor = new TimeIdCursor(displayOrders.getLast());
            nextCursor = cursor.getCursorString();
        }

        return OrderResDto.CustomerOrderResponse.builder()
                .orderList(displayOrders.stream().map(OrderConverter::toCustomerOrderSummary).toList())
                .pagination(OrderResDto.PaginationInfo.builder()
                        .nextCursor(nextCursor)
                        .hasNext(hasNext)
                        .size(displayOrders.size())
                        .build())
                .build();
    }

    private static OrderResDto.CustomerOrderSummary toCustomerOrderSummary(Order order) {
        return OrderResDto.CustomerOrderSummary.builder()
                .orderId(order.getOrderId())
                .orderAddress(order.getDeliveryAddressSnapshot())
                .orderedAt(order.getCreatedAudit().getCreatedAt())
                .orderStatus(order.getStatus())
                .orderStoreId(order.getStore().getStoreId())
                .orderStoreName(order.getStore().getName())
                .representativeProductName(generateOrderSummary(order.getOrderItems()))
                .totalPrice(order.getTotalPrice())
                .totalQuantity(order.getTotalQuantity())
                .build();
    }

    public static OrderResDto.GetOrderDetailResponse toOrderDetailResponse(Order order) {
        return OrderResDto.GetOrderDetailResponse.builder()
                .orderId(order.getOrderId())
                .orderAddress(order.getDeliveryAddressSnapshot())
                .orderedAt(order.getCreatedAudit().getCreatedAt())
                .orderStatus(order.getStatus())
                .orderStoreId(order.getStore().getStoreId())
                .orderItemDetails(order.getOrderItems().stream().map(OrderConverter::toOrderItemDetail).toList())
                .orderStoreName(order.getStore().getName())
                .totalPrice(order.getTotalPrice())
                .totalQuantity(order.getTotalQuantity())
                .orderStatusHistoryDetails(order.getStatusHistories().stream().map(OrderConverter::toOrderHistoryDetail).toList())
                .build();
    }

    private static OrderResDto.OrderStatusHistoryDetail toOrderHistoryDetail(OrderStatusHistory orderStatusHistory) {
        return OrderResDto.OrderStatusHistoryDetail.builder()
                .orderStatusHistoryId(orderStatusHistory.getOrderStatusHistoryId())
                .fromStatus(orderStatusHistory.getFromStatus())
                .nowStatus(orderStatusHistory.getToStatus())
                .changedAt(orderStatusHistory.getCreatedAudit().getCreatedAt())
                .changedReason(orderStatusHistory.getChangeReason())
                .build();
    }

    private static OrderResDto.OrderItemResponse toOrderItemDetail(OrderItem orderItem) {
        return OrderResDto.OrderItemResponse.builder()
                .productId(orderItem.getProductId())
                .productName(orderItem.getProductNameSnapshot())
                .unitPrice(orderItem.getUnitPriceSnapshot())
                .totalPrice(orderItem.getTotalPrice())
                .quantity(orderItem.getQuantity())
                .build();
    }

    public static OrderResDto.OwnerDashboardResponse toOwnerDashboardResponse(OrderResDto.OrderSummaryCount summaryCounts, int pageSize, List<Order> orders) {

        boolean hasNext = orders.size() > pageSize;

        List<Order> displayOrders = hasNext
                ? orders.subList(0, pageSize)
                : orders;
        String nextCursor = null;
        if (!displayOrders.isEmpty()) {
            TimeIdCursor cursor = new TimeIdCursor(displayOrders.getLast());
            nextCursor = cursor.getCursorString();
        }

        Map<OrderStatus, List<OrderResDto.OwnerOrderUnit>> statusGroups = displayOrders.stream()
                .collect(Collectors.groupingBy(
                        Order::getStatus, // 그룹핑 기준 (Key)
                        Collectors.mapping(
                                OrderConverter::toOwnerOrderUnit,
                                Collectors.toList()
                        )
                ));

        return OrderResDto.OwnerDashboardResponse.builder()
                .summary(summaryCounts)
                .statusGroups(statusGroups)
                .pagination(OrderResDto.PaginationInfo.builder()
                        .nextCursor(nextCursor)
                        .hasNext(hasNext)
                        .size(displayOrders.size())
                        .build())
                .build();
    }
    private static OrderResDto.OwnerOrderUnit toOwnerOrderUnit(Order order) {
        return OrderResDto.OwnerOrderUnit.builder()
                .orderId(order.getOrderId())
                .orderAddress(order.getDeliveryAddressSnapshot())
                .orderTime(order.getCreatedAudit().getCreatedAt())
                .totalPrice(order.getTotalPrice())
                .totalQuantity(order.getTotalQuantity())
                .products(order.getOrderItems().stream().map(
                        product->
                        new OrderResDto.ProductSimpleInfo(product.getProductId(), product.getQuantity(), product.getProductNameSnapshot())
                ).toList())
                .build();

    }
}