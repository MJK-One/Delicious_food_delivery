package com.dfdt.delivery.domain.store.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_store_rating")
public class StoreRating extends BaseAuditSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "store_rating_id", nullable = false)
    private UUID storeRatingId;

    @Column(nullable = false)
    private Integer ratingSum = 0;

    @Column(nullable = false)
    private Integer ratingCount = 0;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    private OffsetDateTime lastReviewedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // =========================
    // 비즈니스 로직
    // =========================

    public void addRating(int rating) {
        this.ratingSum += rating;
        this.ratingCount += 1;
        calculateAverage();
        this.lastReviewedAt = OffsetDateTime.now();
    }

    public void removeRating(int rating) {
        this.ratingSum -= rating;
        this.ratingCount -= 1;
        calculateAverage();
        this.lastReviewedAt = OffsetDateTime.now();
    }

    private void calculateAverage() {
        if (ratingCount == 0) {
            this.ratingAvg = BigDecimal.ZERO;
            return;
        }

        this.ratingAvg = BigDecimal.valueOf((double) ratingSum / ratingCount)
                .setScale(2, RoundingMode.HALF_UP);
    }
}