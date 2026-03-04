package com.dfdt.delivery.domain.order.domain.entity;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.domain.product.domain.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "p_order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_item_id", updatable = false, nullable = false)
    private UUID orderItemId;

    // UUID orderId 대신 객체 참조 (N:1 양방향 매핑)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price_snapshot", nullable = false)
    private Long unitPriceSnapshot;

    @Column(name = "product_name_snapshot", length = 120, nullable = false)
    private String productNameSnapshot;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    @Embedded
    private SoftDeleteAudit softDeleteAudit;

    public void setOrder(Order order) {
        this.order = order;
        if (!order.getOrderItems().contains(this)) {
            order.getOrderItems().add(this);
        }
    }
    // 스냅샷 찍기
    public static OrderItem createOrderItem(Product product, Integer quantity) {
        return OrderItem.builder()
                .productId(product.getProductId())
                .productNameSnapshot(product.getName())
                .unitPriceSnapshot(product.getPrice())
                .quantity(quantity)
                .totalPrice(product.getPrice() * quantity)
                .build();
    }
}