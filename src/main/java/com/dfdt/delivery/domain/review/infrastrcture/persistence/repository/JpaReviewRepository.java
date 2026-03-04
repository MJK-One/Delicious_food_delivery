package com.dfdt.delivery.domain.review.infrastrcture.persistence.repository;

import com.dfdt.delivery.domain.review.domain.entity.Review;
import com.dfdt.delivery.domain.review.domain.repository.ReviewRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaReviewRepository extends JpaRepository<Review, UUID>, ReviewRepository {
}
