package com.dfdt.delivery.domain.region.domain.entity;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.UpdateAudit;
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
public class Region {

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

    @Column(nullable = false)
    private Boolean isOrderEnabled = false;

    @Embedded
    private CreateAudit createAudit;

    @Embedded
    private UpdateAudit updateAudit;

    @Embedded
    private SoftDeleteAudit softDeleteAudit;

    @OneToMany(mappedBy = "parentRegionId")
    private List<Region> childRegions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_region_id")
    private Region parentRegionId;

}