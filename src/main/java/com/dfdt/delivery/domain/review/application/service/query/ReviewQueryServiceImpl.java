package com.dfdt.delivery.domain.review.application.service.query;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.review.application.converter.ReviewConverter;
import com.dfdt.delivery.domain.review.application.provider.ReviewDataFinder;
import com.dfdt.delivery.domain.review.application.service.validator.ReviewValidator;
import com.dfdt.delivery.domain.review.domain.entity.Review;
import com.dfdt.delivery.domain.review.domain.enums.ReviewErrorCode;
import com.dfdt.delivery.domain.review.domain.repository.ReviewCustomRepository;
import com.dfdt.delivery.domain.review.presentation.dto.request.MyReviewSearchReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewSearchReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.StoreReviewSearchReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.response.ReviewListResDto;
import com.dfdt.delivery.domain.review.presentation.dto.response.ReviewResDto;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewQueryServiceImpl implements ReviewQueryService {

    private final ReviewValidator reviewValidator;
    private final ReviewDataFinder reviewDataFinder;
    private final ReviewCustomRepository reviewCustomRepository;

    @Override
    @Transactional(readOnly = true)
    public ReviewListResDto getStoreReviews(UUID storeId, StoreReviewSearchReqDto request) {

        request.setSize(reviewValidator.validateAndAdjustSize(request.getSize()));

        Page<Review> reviewPage = reviewCustomRepository.searchStoreReviews(storeId, request);

        return ReviewListResDto.from(reviewPage.map(review -> {
            Store store = reviewDataFinder.findStore(review.getStoreId());
            Order order = reviewDataFinder.findOrder(review.getOrderId());
            return ReviewConverter.toResDto(review, store, order);
        }));
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewListResDto getMyReviews(String username, MyReviewSearchReqDto request) {

        request.setSize(reviewValidator.validateAndAdjustSize(request.getSize()));

        Page<Review> reviewPage = reviewCustomRepository.searchMyReviews(username, request);

        return ReviewListResDto.from(reviewPage.map(review -> {
            Store store = reviewDataFinder.findStore(review.getStoreId());
            Order order = reviewDataFinder.findOrder(review.getOrderId());
            return ReviewConverter.toResDto(review, store, order);
        }));
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResDto getReview(UUID reviewId) {

        Review review = reviewDataFinder.findReview(reviewId);

        if (review.isDeleted()) {
            throw new BusinessException(ReviewErrorCode.REVIEW_ALREADY_DELETED);
        }

        Store store = reviewDataFinder.findStore(review.getStoreId());
        Order order = reviewDataFinder.findOrder(review.getOrderId());

        return ReviewConverter.toResDto(review, store, order);
    }


    @Override
    @Transactional(readOnly = true)
    public ReviewListResDto searchReviews(ReviewSearchReqDto request) {
        Page<Review> reviewPage = reviewCustomRepository.searchAllReviews(request);

        return ReviewListResDto.from(reviewPage.map(review -> {
            Store store = reviewDataFinder.findStore(review.getStoreId());
            Order order = reviewDataFinder.findOrder(review.getOrderId());
            return ReviewConverter.toResDto(review, store, order);
        }));
    }
}