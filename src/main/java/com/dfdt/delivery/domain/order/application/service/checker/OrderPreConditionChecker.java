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

import java.util.Objects;

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
            System.out.println(order.getUser().getUsername()+ user.getUsername());
            if (!Objects.equals(order.getUser().getUsername(), user.getUsername())) {
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
        // 결제 완료(Step 2) 이상이면 수정 불가
        if (order.getStatus().getStep() >= OrderStatus.PAID.getStep()) {
            throw new BusinessException(OrderErrorCode.ALREADY_PROCESSED);
        }
    }
    public void validateQuantity(Order order) {
        if (order.getOrderItems().isEmpty())
            throw new BusinessException(OrderErrorCode.INVALID_QUANTITY);
    }

    public void validateDeletable(Order order) {
        if (isProcessing(order)) {
            throw new BusinessException(OrderErrorCode.ALREADY_PROCESSED);
        }
    }
    private boolean isProcessing(Order order) {
        OrderStatus status = order.getStatus();
        // PAID(2) <= 현재 단계 < COMPLETED(7) 인 경우 진행 중으로 판단
        return status.getStep() >= OrderStatus.PAID.getStep()
                && status.getStep() < OrderStatus.COMPLETED.getStep();
    }
    public void authoriseOrderCustomer(Order order, User user) {
        if (user.getRole() == UserRole.CUSTOMER) {
            authoriseOrder(order, user);
        }
       else throw new BusinessException(OrderErrorCode.ACCESS_DENIED);
    }

    public void validateStatusUpdatable(User user, Order order, OrderStatus toStatus) {
        OrderStatus nowStatus = getOrderStatus(user, order);

        // 동일 상태 변경 방지 및 진행 중인 주문 거절 방지
        // 이미 사장님이 수락(ACCEPTED, Step 3)하여 진행 중인데 거절(REJECTED)하려는 경우 방지
        if (nowStatus == toStatus || (nowStatus.getStep() >= OrderStatus.ACCEPTED.getStep() && toStatus == OrderStatus.REJECTED)) {
            throw new BusinessException(OrderErrorCode.ALREADY_PROCESSED);
        }
    }

    private static OrderStatus getOrderStatus(User user, Order order) {
        OrderStatus nowStatus = order.getStatus();

        // 1. 권한 체크
        if (user.getRole() == UserRole.CUSTOMER) {
            throw new BusinessException(OrderErrorCode.ACCESS_DENIED);
        }

        // 2. 기초 상태 체크
        if (nowStatus == OrderStatus.PENDING) {
            throw new BusinessException(OrderErrorCode.PAYMENT_REQUIRED);
        }

        // 3. 종료된 주문 체크 (isTerminated 활용)
        if (nowStatus.isTerminated()) {
            throw new BusinessException(OrderErrorCode.ACCESS_DENIED);
        }
        return nowStatus;
    }
}