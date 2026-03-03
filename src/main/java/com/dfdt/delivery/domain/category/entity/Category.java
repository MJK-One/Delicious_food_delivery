package com.dfdt.delivery.domain.category.entity;

import com.dfdt.delivery.domain.category.dto.request.CategoryCreateReqDto;
import com.dfdt.delivery.domain.category.dto.request.CategoryUpdateReqDto;
import com.dfdt.delivery.domain.store.domain.entity.StoreCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
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
    private Boolean isActive = false;

    @OneToMany(mappedBy = "category")
    private List<StoreCategory> stores = new ArrayList<>();

    public static Category create(CategoryCreateReqDto request) {
        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder())
                .isActive(request.getIsActive())
                .build();
    }

    public void update(CategoryUpdateReqDto request) {
        this.name = request.getName();
        this.description = request.getDescription();
        this.sortOrder = request.getSortOrder();
        this.isActive = request.getIsActive();
    }

    public void delete(String username) {
        this.deletedAt = OffsetDateTime.now();
        this.deletedBy = username;
    }

    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
    }
}