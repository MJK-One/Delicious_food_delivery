package com.dfdt.delivery.domain.region.domain.repository;

import com.dfdt.delivery.domain.region.domain.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegionRepository extends JpaRepository<Region, UUID> {
}
