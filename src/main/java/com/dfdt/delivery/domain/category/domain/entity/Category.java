package com.dfdt.delivery.domain.category.domain.entity;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.UpdateAudit;
import com.dfdt.delivery.domain.category.presentation.dto.request.CategoryCreateReqDto;
import com.dfdt.delivery.domain.category.presentation.dto.request.CategoryUpdateReqDto;
import com.dfdt.delivery.domain.store.domain.entity.StoreCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "p_category")
public class Category {

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

    @Embedded
    private CreateAudit createAudit;

    @Embedded
    private UpdateAudit updateAudit;

    @Embedded
    private SoftDeleteAudit softDeleteAudit;

    @OneToMany(mappedBy = "category")
    private List<StoreCategory> stores = new ArrayList<>();

    public static Category create(CategoryCreateReqDto request, int maxOrder, String username) {
        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .sortOrder(maxOrder)
                .isActive(request.getIsActive())
                .createAudit(CreateAudit.now(username))
                .build();
    }

    public void update(CategoryUpdateReqDto request, String username) {
        this.name = request.getName();
        this.description = request.getDescription();
        this.sortOrder = request.getSortOrder();
        this.isActive = request.getIsActive();
        makeUpdateAudit(username);
    }

    public void delete(String username) {
        makeUpdateAudit(username);
        this.softDeleteAudit = SoftDeleteAudit.active();
        this.softDeleteAudit.softDelete(username);
    }

    public void restore(int maxSortOrder, String username) {
        this.sortOrder = maxSortOrder;
        this.softDeleteAudit.restore();
        makeUpdateAudit(username);
    }

    private void makeUpdateAudit(String username) {
        this.updateAudit = UpdateAudit.empty();
        this.updateAudit.touch(username);
    }
}