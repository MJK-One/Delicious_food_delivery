package com.dfdt.delivery.domain.review.domain.repository;

import com.dfdt.delivery.domain.review.domain.entity.Review;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository {
    Review save(Review review);

    Optional<Review> findById(UUID reviewId);

    boolean existsByOrderIdAndSoftDeleteAuditDeletedAtIsNull(UUID orderId);
}
