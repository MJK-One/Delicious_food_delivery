package com.dfdt.delivery.domain.order.application.service.checker;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.order.domain.enums.OrderErrorCode;
import com.dfdt.delivery.domain.order.domain.enums.OrderStatus;
import com.dfdt.delivery.domain.order.presentation.dto.OrderReqDto;
import com.dfdt.delivery.domain.product.domain.entity.Product;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import org.springframework.stereotype.Component;

@Component
public class OrderPreConditionChecker {

    public void validateProduct(Product product, Store orderStore, OrderReqDto.OrderItem itemReq) {
        if (product == null || product.getIsHidden()) {
            throw new BusinessException(OrderErrorCode.OUT_OF_STOCK);
        }
        if (itemReq.quantity() < 1) {
            throw new BusinessException(OrderErrorCode.INVALID_QUANTITY);
        }
        if (!product.getStore().equals(orderStore)) {
            throw new BusinessException(OrderErrorCode.SHOP_MISMATCH);
        }
        if (product.getPrice() == null ||
                product.getPrice().compareTo(itemReq.userViewedUnitPrice()) != 0) {
            throw new BusinessException(OrderErrorCode.PRICE_CHANGED);
        }
    }

    public void authoriseOrder(Order order, User user) {
        // 1. 고객인 경우: 본인 주문만 접근 가능
        if (user.getRole() == UserRole.CUSTOMER) {
            if (!order.getUser().getUsername().equals(user.getUsername())) {
                throw new BusinessException(OrderErrorCode.ACCESS_DENIED);
            }
        }
        // 2. 사장님인 경우: 본인 가게의 주문만 접근 가능
        if (user.getRole() == UserRole.OWNER) {
            if (!order.getStore().getUser().getUsername().equals(user.getUsername())) {
                throw new BusinessException(OrderErrorCode.ACCESS_DENIED);
            }
        }

        // 관리자(ADMIN)는 위 조건들에 걸리지 않으므로 모든 권한을 가짐 (자동 통과)
    }
    public void validateModifiable(Order order) {
        if (order.getStatus().ordinal() >= OrderStatus.PAID.ordinal()) {
            throw new BusinessException(OrderErrorCode.ALREADY_PROCESSED);
        }
    }
    public void validateQuantity(Order order) {
        if (order.getOrderItems().isEmpty())
            throw new BusinessException(OrderErrorCode.INVALID_QUANTITY);
    }

    public void validateDeletable(Order order) {
        // PAID(결제완료) 이상이면서 COMPLETED(배송완료)가 아닌 '진행 중'인 상태일 때
        // (예: 결제완료, 상품준비중, 배송중 등)
        if (isProcessing(order)) {
            throw new BusinessException(OrderErrorCode.ALREADY_PROCESSED);
        }
    }
    private boolean isProcessing(Order order) {
        OrderStatus status = order.getStatus();
        // PAID(결제완료) 보다는 크거나 같고, COMPLETED(배송완료) 보다는 작은 상태들
        return status.ordinal() >= OrderStatus.PAID.ordinal()
                && status.ordinal() < OrderStatus.COMPLETED.ordinal();
    }

    public void authoriseOrderCustomer(Order order, User user) {
        if (user.getRole() == UserRole.CUSTOMER) {
            authoriseOrder(order, user);
        }
//        throw new BusinessException(OrderErrorCode.ACCESS_DENIED);
    }
}