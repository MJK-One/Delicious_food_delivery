package com.dfdt.delivery.domain.order.application.converter;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.UpdateAudit;
import com.dfdt.delivery.domain.address.domain.entity.Address;
import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.order.domain.entity.OrderItem;
import com.dfdt.delivery.domain.order.domain.enums.OrderStatus;
import com.dfdt.delivery.domain.order.presentation.dto.OrderResDto;
import com.dfdt.delivery.domain.product.domain.entity.Product;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.user.domain.entity.User;

import java.util.List;

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
}