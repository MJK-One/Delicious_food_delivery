package com.dfdt.delivery.domain.store.domain.entity;

import com.dfdt.delivery.domain.category.domain.entity.Category;
import com.dfdt.delivery.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_store_category")
public class StoreCategory extends BaseAuditSoftDeleteEntity {

    public StoreCategory(Store store, Category category) {
        this.store = store;
        this.category = category;
    }

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

    public void deleteStoreCategory(User user) {
        deletedAt = OffsetDateTime.now();
        deletedBy = user.getUsername();
    }

}