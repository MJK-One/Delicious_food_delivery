package com.dfdt.delivery.domain.region.entity;

import com.dfdt.delivery.common.Entity.BaseAuditSoftDeleteEntity;
import jakarta.persistence.*;
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
@Table(name = "p_region")
public class Region extends BaseAuditSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "region_id")
    private UUID regionId;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(nullable = false)
    private Short level;

    @Column(length = 30, unique = true)
    private String code;

    @OneToMany(mappedBy = "parentRegion")
    private List<Region> childRegions = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isOrderEnabled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_region_id")
    private Region parentRegionId;

}