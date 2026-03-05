package com.dfdt.delivery.domain.store.domain.entity;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.UpdateAudit;
import com.dfdt.delivery.domain.category.domain.entity.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_store_category")
public class StoreCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "store_category_id")
    private UUID storeCategoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Embedded
    private CreateAudit createAudit;

    @Embedded
    private UpdateAudit updateAudit;

    @Embedded
    private SoftDeleteAudit softDeleteAudit;

    public static StoreCategory create(Store store, Category category, String username) {
        StoreCategory sc = new StoreCategory();
        sc.store = store;
        sc.category = category;
        sc.createAudit = CreateAudit.now(username);
        return sc;
    }

    public void delete(String username) {
        this.updateAudit = UpdateAudit.empty();
        this.updateAudit.touch(username);
        this.softDeleteAudit = SoftDeleteAudit.active();
        this.softDeleteAudit.softDelete(username);
    }

}