package com.dfdt.delivery.domain.store.entity;

import com.dfdt.delivery.common.Entity.BaseAuditSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Table(name = "p_category")
public class Category extends BaseAuditSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "category_id")
    private UUID categoryId;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private Boolean isActive;

    @OneToMany(mappedBy = "category")
    private List<StoreCategory> stores = new ArrayList<>();
}