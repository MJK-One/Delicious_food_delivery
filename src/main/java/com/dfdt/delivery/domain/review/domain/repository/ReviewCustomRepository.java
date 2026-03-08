package com.dfdt.delivery.domain.review.domain.repository;

import com.dfdt.delivery.domain.review.domain.entity.Review;
import com.dfdt.delivery.domain.review.presentation.dto.request.MyReviewSearchReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewSearchReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.StoreReviewSearchReqDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ReviewCustomRepository {
    Page<Review> searchMyReviews(String username, MyReviewSearchReqDto request);

    Page<Review> searchStoreReviews(UUID storeId, StoreReviewSearchReqDto request);

    Page<Review> searchAllReviews(ReviewSearchReqDto request);
}
