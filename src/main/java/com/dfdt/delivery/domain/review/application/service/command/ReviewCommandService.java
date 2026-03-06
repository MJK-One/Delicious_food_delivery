package com.dfdt.delivery.domain.review.application.service.command;

import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewCreateReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewUpdateReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.response.ReviewResDto;

import java.util.UUID;

public interface ReviewCommandService {

    ReviewResDto createReview(String username, ReviewCreateReqDto request);

    ReviewResDto updateReview(UUID reviewId, String username, ReviewUpdateReqDto request);

    void deleteReview(UUID reviewId, String username);
}