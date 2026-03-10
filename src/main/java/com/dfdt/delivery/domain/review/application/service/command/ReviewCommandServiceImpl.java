package com.dfdt.delivery.domain.review.application.service.command;

import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.review.application.converter.ReviewConverter;
import com.dfdt.delivery.domain.review.application.provider.ReviewDataFinder;
import com.dfdt.delivery.domain.review.application.service.validator.ReviewValidator;
import com.dfdt.delivery.domain.review.domain.entity.Review;
import com.dfdt.delivery.domain.review.domain.repository.ReviewRepository;
import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewCreateReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewUpdateReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.response.ReviewResDto;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewCommandServiceImpl implements ReviewCommandService {

    private final ReviewRepository reviewRepository;
    private final ReviewValidator reviewValidator;
    private final ReviewDataFinder reviewDataFinder;

    @Override
    @Transactional
    @CacheEvict(value = "storeReviews", allEntries = true)
    public ReviewResDto createReview(String username, ReviewCreateReqDto request) {

        // 1. 데이터 조회
        Order order = reviewDataFinder.findOrder(request.getOrderId());
        boolean alreadyReviewed = reviewDataFinder.existsByOrderId(request.getOrderId());

        // 2. 리뷰 작성 가능 여부 검증
        reviewValidator.validateCreate(order, username, alreadyReviewed);

        // 3. 가게 조회
        Store store = reviewDataFinder.findStore(request.getStoreId());

        // 4. Review 엔티티 생성
        Review review = Review.create(
                request.getOrderId(),
                request.getStoreId(),
                username,
                request.getRating(),
                request.getContent(),
                username
        );

        // 5. 리뷰 이미지 추가
        if (request.getImageUrls() != null) {
            request.getImageUrls().forEach(review::addImage);
        }

        // 6. 리뷰 저장
        Review savedReview = reviewRepository.save(review);

        // 7. 가게 평점 요약 업데이트
        if (store.getStoreRating() != null) {
            store.getStoreRating().addRating(request.getRating());
        }

        // 8. Response DTO 변환 및 반환 (Converter)
        return ReviewConverter.toResDto(savedReview, store, order);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "reviewDetail", key = "#reviewId.toString()"),
            @CacheEvict(value = "storeReviews", allEntries = true)
    })
    public ReviewResDto updateReview(UUID reviewId, String username, ReviewUpdateReqDto request) {

        // 1. 리뷰 조회
        Review review = reviewDataFinder.findReview(reviewId);

        // 2. 리뷰 수정 가능 여부 검증
        reviewValidator.validateUpdate(review, username);

        // 3. 가게 조회
        Store store = reviewDataFinder.findStore(review.getStoreId());

        // 4. 평점 변경 시 가게 평점 요약 업데이트
        if (request.getRating() != null && !request.getRating().equals(review.getRating())) {
            if (store.getStoreRating() != null) {
                store.getStoreRating().removeRating(review.getRating());
                store.getStoreRating().addRating(request.getRating());
            }
        }

        // 5. 리뷰 수정
        review.update(request.getRating(), request.getContent(), username);

        // 6. 이미지 수정 처리
        if (request.getImageUrls() != null) {
            review.updateImages(request.getImageUrls());
        }

        // 7. DTO 변환을 위한 주문 정보 조회
        Order order = reviewDataFinder.findOrder(review.getOrderId());

        // 8. DTO 변환 및 반환
        return ReviewConverter.toResDto(review, store, order);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "reviewDetail", key = "#reviewId.toString()"),
            @CacheEvict(value = "storeReviews", allEntries = true)
    })
    public void deleteReview(UUID reviewId, String username, UserRole role) {

        // 1. 리뷰 조회
        Review review = reviewDataFinder.findReview(reviewId);

        // 2. 리뷰 삭제 가능 여부 검증
        reviewValidator.validateDelete(review, username, role);

        // 3. 가게 평점 요약 업데이트
        Store store = reviewDataFinder.findStore(review.getStoreId());
        if (store.getStoreRating() != null) {
            store.getStoreRating().removeRating(review.getRating());
        }

        // 4. Soft Delete 수행
        review.delete(username);
    }
}
