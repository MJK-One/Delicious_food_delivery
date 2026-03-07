package com.dfdt.delivery.domain.review.application.converter;

import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.order.domain.entity.OrderItem;
import com.dfdt.delivery.domain.review.domain.entity.Review;
import com.dfdt.delivery.domain.review.domain.entity.ReviewImage;
import com.dfdt.delivery.domain.review.presentation.dto.response.ReviewResDto;
import com.dfdt.delivery.domain.store.domain.entity.Store;

import java.util.stream.Collectors;

public class ReviewConverter {

    public static ReviewResDto toResDto(Review review, Store store, Order order) {
        return ReviewResDto.builder()
                .reviewId(review.getReviewId())
                .username(review.getWriterUsername())
                .storeName(store.getName())
                .orderMenuNames(order.getOrderItems().stream()
                        .map(OrderItem::getProductNameSnapshot)
                        .collect(Collectors.toList()))
                .rating(review.getRating())
                .content(review.getContent())
                .images(review.getImages().stream()
                        .map(ReviewImage::getImageUrl)
                        .collect(Collectors.toList()))
                .createdAt(review.getCreateAudit().getCreatedAt())
                .updatedAt(review.getUpdateAudit().getUpdatedAt())
                .build();
    }
}
