package com.dfdt.delivery.domain.review.application.service.query;

import com.dfdt.delivery.domain.review.presentation.dto.request.MyReviewSearchReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewSearchReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.response.ReviewListResDto;
import com.dfdt.delivery.domain.review.presentation.dto.response.ReviewResDto;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewQueryService {

    ReviewListResDto getStoreReviews(UUID storeId, Pageable pageable);

    ReviewListResDto getMyReviews(String username, MyReviewSearchReqDto request);

    ReviewResDto getReview(UUID reviewId);

    ReviewListResDto searchReviews(ReviewSearchReqDto reviewSearchReqDto);
}