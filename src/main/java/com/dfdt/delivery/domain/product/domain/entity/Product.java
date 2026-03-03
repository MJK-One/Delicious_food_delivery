package com.dfdt.delivery.domain.product.domain.entity;

import com.dfdt.delivery.domain.store.domain.entity.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "p_product")
public class Product extends BaseAuditSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id")
    private UUID productId;

    @Column(length = 120, nullable = false)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isHidden = false;

    private Boolean isAiDescription;

    @Column(nullable = false)
    private Integer displayOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    public static Product create(Store store, String name, String description, Integer price, Integer displayOrder) {
        return Product.builder()
                .store(store)
                .name(name)
                .description(description)
                .price(price)
                .displayOrder(displayOrder)
                .isHidden(false)
                .build();
    }

    public void update(String name, String description, Integer price, Integer displayOrder) {
        this.name = name;
        this.description = description;
        this.price = price;
        if (displayOrder != null) {
            this.displayOrder = displayOrder;
        }
    }

    public void delete(String username) {
        this.deletedAt = OffsetDateTime.now();
        this.deletedBy = username;
    }

    public void soldOut() {
        this.isHidden = true;
    }

    public void restore() {
        this.isHidden = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }
}