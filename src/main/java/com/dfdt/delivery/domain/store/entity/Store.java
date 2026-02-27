package com.dfdt.delivery.domain.store.entity;

import com.dfdt.delivery.common.Entity.BaseAuditSoftDeleteEntity;
import com.dfdt.delivery.domain.product.entity.Product;
import com.dfdt.delivery.domain.region.entity.Region;
import com.dfdt.delivery.domain.store.enums.StoreStatus;
import com.dfdt.delivery.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "p_store")
public class Store extends BaseAuditSoftDeleteEntity {

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

    @Column(nullable = false)
    @OneToMany(mappedBy = "store")
    private List<StoreCategory> categories = new ArrayList<>();

    @OneToMany(mappedBy = "store")
    private List<Product> products = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", nullable = false)
    private User ownerUsername;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @OneToOne(mappedBy = "store")
    private StoreRating storeRatings;
}