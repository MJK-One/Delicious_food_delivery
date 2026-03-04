package com.dfdt.delivery.domain.store.domain.repository;

import com.dfdt.delivery.domain.store.domain.entity.StoreRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreRatingRepository extends JpaRepository<StoreRating, UUID> {
}
