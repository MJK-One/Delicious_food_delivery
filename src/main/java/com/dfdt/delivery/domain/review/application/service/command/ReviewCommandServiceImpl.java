package com.dfdt.delivery.domain.review.application.service.command;

import com.dfdt.delivery.domain.review.domain.repository.ReviewRepository;
import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewCreateReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewUpdateReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.response.ReviewResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewCommandServiceImpl implements ReviewCommandService {

    private final ReviewRepository reviewRepository;

    @Override
    public ReviewResDto createReview(String username, ReviewCreateReqDto request) {

        // TODO 1. 주문 조회

        // TODO 2. 주문 존재 여부 검증

        // TODO 3. 주문 소유자 검증

        // TODO 4. 주문 상태 검증 (COMPLETED 등)

        // TODO 5. 결제 상태 검증 (PAID)

        // TODO 6. 이미 리뷰 작성 여부 확인

        // TODO 7. Review 엔티티 생성

        // TODO 8. 리뷰 이미지 추가

        // TODO 9. 리뷰 저장

        // TODO 10. 가게 평점 요약 업데이트

        // TODO 11. Response DTO 변환

        return null;
    }

    @Override
    public ReviewResDto updateReview(UUID reviewId, String username, ReviewUpdateReqDto request) {

        // TODO 1. 리뷰 조회

        // TODO 2. 삭제 여부 확인

        // TODO 3. 작성자 검증

        // TODO 4. 리뷰 수정

        // TODO 5. 이미지 수정 처리

        // TODO 6. 가게 평점 재계산

        // TODO 7. 저장

        // TODO 8. DTO 변환

        return null;
    }

    @Override
    public void deleteReview(UUID reviewId, String username) {

        // TODO 1. 리뷰 조회

        // TODO 2. 삭제 여부 확인

        // TODO 3. 권한 검증 (작성자 or MASTER)

        // TODO 4. Soft Delete

        // TODO 5. 가게 평점 요약 업데이트

        // TODO 6. 저장
    }
}