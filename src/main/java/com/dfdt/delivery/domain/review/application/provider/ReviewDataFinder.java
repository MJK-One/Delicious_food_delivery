package com.dfdt.delivery.domain.review.application.provider;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.order.domain.repository.OrderRepository;
import com.dfdt.delivery.domain.review.domain.entity.Review;
import com.dfdt.delivery.domain.review.domain.enums.ReviewErrorCode;
import com.dfdt.delivery.domain.review.domain.repository.ReviewRepository;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReviewDataFinder {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;

    public Review findReview(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }

    public Order findOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ReviewErrorCode.ORDER_NOT_FOUND));
    }

    public Store findStore(UUID storeId) {
        return storeRepository.findByStoreIdAndNotDeleted(storeId)
                .orElseThrow(() -> new BusinessException(ReviewErrorCode.STORE_NOT_FOUND));
    }

    public boolean existsByOrderId(UUID orderId) {
        return reviewRepository.existsByOrderIdAndSoftDeleteAuditDeletedAtIsNull(orderId);
    }
}
