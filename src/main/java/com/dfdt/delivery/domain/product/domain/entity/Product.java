package com.dfdt.delivery.domain.product.domain.entity;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.UpdateAudit;
import com.dfdt.delivery.domain.product.presentation.dto.request.ProductCreateReqDto;
import com.dfdt.delivery.domain.product.presentation.dto.request.ProductUpdateReqDto;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "p_product")
public class Product {

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
    private Boolean isHidden;

    private Boolean isAiDescription;

    @Column(nullable = false)
    private Integer displayOrder;

    @Embedded
    private CreateAudit createAudit;

    @Embedded
    private UpdateAudit updateAudit;

    @Embedded
    private SoftDeleteAudit softDeleteAudit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    public static Product create(ProductCreateReqDto request, Store store, Integer displayOrder, String username) {
        return Product.builder()
                .store(store)
                .name(request.getName())
                .description(request.getDescription())
                .isAiDescription(request.getIsAiDescription())
                .price(request.getPrice())
                .displayOrder(displayOrder)
                .isHidden(request.getIsHidden())
                .createAudit(CreateAudit.now(username))
                .build();
    }

    public void update(ProductUpdateReqDto request, String username) {
        this.name = request.getName();
        this.description = request.getDescription();
        this.isAiDescription = request.getIsAiDescription();
        this.price = request.getPrice();
        this.displayOrder = request.getDisplayOrder();
        this.isHidden = request.getIsHidden();
        makeUpdateAudit(username);
    }

    public void delete(String username) {
        makeUpdateAudit(username);
        this.softDeleteAudit = SoftDeleteAudit.active();
        this.softDeleteAudit.softDelete(username);
    }

    public void applyAiDescription(String aiDescription, String username) {
        this.description = aiDescription;
        this.isAiDescription = true;
        makeUpdateAudit(username);
    }

    public void soldOut(String username) {
        makeUpdateAudit(username);
        this.isHidden = !this.isHidden;
    }

    public void restore(int maxOrder, String username) {
        this.displayOrder = maxOrder;
        this.softDeleteAudit.restore();
        makeUpdateAudit(username);
    }

    private void makeUpdateAudit(String username) {
        this.updateAudit = UpdateAudit.empty();
        this.updateAudit.touch(username);
    }
}