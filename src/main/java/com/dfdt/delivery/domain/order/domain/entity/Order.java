package com.dfdt.delivery.domain.order.domain.entity;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.UpdateAudit;
import com.dfdt.delivery.domain.order.domain.enums.OrderStatus;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.address.domain.entity.Address;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id", updatable = false, nullable = false)
    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="username")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Column(name = "delivery_address_snapshot", length = 400, nullable = false)
    private String deliveryAddressSnapshot;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Column(name = "order_request_message", length = 255)
    private String orderRequestMessage;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderStatusHistory> statusHistories = new ArrayList<>();

    @Embedded
    private CreateAudit createdAudit;

    @Embedded
    private UpdateAudit updateAudit;

    @Embedded
    private SoftDeleteAudit softDeleteAudit;

    // 양방향 추가
    public void addOrderItem(OrderItem item) {
        this.orderItems.add(item);
        this.totalPrice += item.getTotalPrice();
        item.setOrder(this);
    }
    public void addStatusHistory(OrderStatus from, OrderStatus to, String reason) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(this)
                .fromStatus(from)
                .toStatus(to)
                .changeReason(reason)
                .createdAudit(CreateAudit.now(this.user.getName()))
                .build();
        this.statusHistories.add(history);
    }
    public void updateStatus(OrderStatus toStatus, String reason) {
        OrderStatus fromStatus = this.status;
        this.status = toStatus; // 현재 상태 업데이트
        addStatusHistory(fromStatus, toStatus, reason); // 이력 추가
    }
    public void updateAddress(Address address)
    {
        this.address = address;
        this.deliveryAddressSnapshot = String.format("[%s / %s] %s %s (메모: %s)",
                address.getReceiverName(),
                address.getReceiverPhone(),
                address.getAddressLine1(),
                address.getAddressLine2() != null ? address.getAddressLine2() : "",
                address.getDeliveryMemo() != null ? address.getDeliveryMemo() : "없음"
        );
    }
    public void removeOrderItems(List<UUID> itemIdsToDelete)
    {
        if (itemIdsToDelete == null || itemIdsToDelete.isEmpty()) {
            return;
        }
        List<OrderItem> toRemove = this.orderItems.stream()
                .filter(item -> itemIdsToDelete.contains(item.getOrderItemId()))
                .toList();

        for (OrderItem item : toRemove) {
            if (this.totalPrice != null) {
                this.totalPrice -= item.getTotalPrice();
            }
            this.orderItems.remove(item);
        }
    }
    public void updateOrderMessage(String orderRequestMessage)
    {
        if (orderRequestMessage != null && !orderRequestMessage.isBlank()) {
            this.orderRequestMessage = orderRequestMessage;
        }
        this.orderRequestMessage = orderRequestMessage;
    }
    public void deleteOrder(String deleteBy) {
        if (this.softDeleteAudit == null) {
            this.softDeleteAudit = SoftDeleteAudit.active();
        }
        this.softDeleteAudit.softDelete(deleteBy);
    }
}