package com.dfdt.delivery.domain.store.domain.entity;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.UpdateAudit;
import com.dfdt.delivery.domain.category.domain.entity.Category;
import com.dfdt.delivery.domain.product.domain.entity.Product;
import com.dfdt.delivery.domain.region.domain.entity.Region;
import com.dfdt.delivery.domain.store.domain.enums.StoreStatus;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreCreateReqDto;
import com.dfdt.delivery.domain.store.presentation.dto.request.StoreUpdateReqDto;
import com.dfdt.delivery.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "p_store")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "store_id")
    private UUID storeId;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 20)
    @Pattern(regexp = "^(0\\d{1,2})-?\\d{3,4}-?\\d{4}$")
    private String phone;

    @Column(length = 500)
    private String description;

    @Column(length = 255)
    private String addressText;

    @Column(nullable = false)
    private Boolean isOpen;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StoreStatus status;

    @Embedded
    private CreateAudit createAudit;

    @Embedded
    private UpdateAudit updateAudit;

    @Embedded
    private SoftDeleteAudit softDeleteAudit;

    @Column(nullable = false)
    @OneToMany(mappedBy = "store")
    private List<StoreCategory> categories = new ArrayList<>();

    @OneToMany(mappedBy = "store")
    private List<Product> products = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @OneToOne(mappedBy = "store")
    private StoreRating storeRating;


    public static Store create(StoreCreateReqDto request, User user) {
        return Store.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .description(request.getDescription())
                .addressText(request.getAddressText())
                .isOpen(false)
                .status(StoreStatus.REQUESTED)
                .user(user)
                .build();
    }

    public void addCategory(Category category) {
        this.categories.add(new StoreCategory(this, category));
    }

    public void addCategories(List<Category> categoryList) {
        for (Category category : categoryList) {
            StoreCategory storeCategory = new StoreCategory(this, category);
            this.categories.add(storeCategory);
        }
    }

    public void update(StoreUpdateReqDto request) {
        this.name = request.getName();
        this.phone = request.getPhone();
        this.description = request.getDescription();
        this.addressText = request.getAddressText();
        this.isOpen = request.getIsOpen();
    }

//    public void delete(String username) {
//        this.deletedAt = OffsetDateTime.now();
//        this.deletedBy = username;
//    }

    public void changeIsOpen() {
        this.isOpen = this.isOpen ? false : true;
    }

    public void restore() {
        this.status = StoreStatus.APPROVED;
    }

    public void changeStatus(String status) {
        try {
            this.status = StoreStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 상태 값입니다: " + status);
        }
    }

}