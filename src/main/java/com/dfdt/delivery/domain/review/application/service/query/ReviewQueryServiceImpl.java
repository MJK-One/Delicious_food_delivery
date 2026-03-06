package com.dfdt.delivery.domain.review.application.service.query;

import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewSearchReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.response.ReviewListResDto;
import com.dfdt.delivery.domain.review.presentation.dto.response.ReviewResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewQueryServiceImpl implements ReviewQueryService {

    @Override
    public ReviewListResDto getStoreReviews(UUID storeId, Pageable pageable) {

        // TODO 1. 가게 존재 여부 확인

        // TODO 2. 리뷰 목록 조회 (Page)

        // TODO 3. 리뷰 이미지 조회

        // TODO 4. DTO 변환

        // TODO 5. 가게 평점 요약 조회

        // TODO 6. ReviewListResponse 생성

        return null;
    }

    @Override
    public ReviewListResDto getMyReviews(String username, Pageable pageable) {

        // TODO 1. 내 리뷰 목록 조회

        // TODO 2. 리뷰 이미지 조회

        // TODO 3. DTO 변환

        // TODO 4. ReviewListResponse 생성

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