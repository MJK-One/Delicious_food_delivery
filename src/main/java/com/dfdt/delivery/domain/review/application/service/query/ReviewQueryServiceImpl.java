package com.dfdt.delivery.domain.review.application.service.query;

import com.dfdt.delivery.domain.review.application.provider.ReviewDataFinder;
import com.dfdt.delivery.domain.review.application.service.validator.ReviewValidator;
import com.dfdt.delivery.domain.review.presentation.dto.request.MyReviewSearchReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewSearchReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.StoreReviewSearchReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.response.ReviewListResDto;
import com.dfdt.delivery.domain.review.presentation.dto.response.ReviewResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewQueryServiceImpl implements ReviewQueryService {

    private final ReviewValidator reviewValidator;
    private final ReviewDataFinder reviewDataFinder;

    @Override
    public ReviewListResDto getStoreReviews(UUID storeId, StoreReviewSearchReqDto request) {

        request.setSize(reviewValidator.validateAndAdjustSize(request.getSize()));

        // TODO : 가게 리뷰 목록 조회

        return null;
    }

    @Override
    public ReviewListResDto getMyReviews(String username, MyReviewSearchReqDto request) {

        request.setSize(reviewValidator.validateAndAdjustSize(request.getSize()));

        // TODO: 내 리뷰 목록 조회

        return null;
    }

    @Override
    public ReviewResDto getReview(UUID reviewId) {

        // TODO 1. 리뷰 조회

        // TODO 2. 삭제 여부 확인

        // TODO 3. 이미지 조회

        // TODO 4. DTO 변환

        return null;
    }

    @Override
    public ReviewListResDto searchReviews(ReviewSearchReqDto reviewSearchReqDto) {
        // TODO : 관리자용 리뷰 검색
        return null;
    }
}