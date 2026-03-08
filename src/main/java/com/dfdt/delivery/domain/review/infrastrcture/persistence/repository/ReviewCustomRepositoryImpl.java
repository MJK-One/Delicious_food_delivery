package com.dfdt.delivery.domain.review.infrastrcture.persistence.repository;

import com.dfdt.delivery.domain.review.domain.entity.QReview;
import com.dfdt.delivery.domain.review.domain.entity.Review;
import com.dfdt.delivery.domain.review.domain.repository.ReviewCustomRepository;
import com.dfdt.delivery.domain.review.presentation.dto.request.MyReviewSearchReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewSearchReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.StoreReviewSearchReqDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReviewCustomRepositoryImpl implements ReviewCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Review> searchMyReviews(String username, MyReviewSearchReqDto request) {
        QReview review = QReview.review;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(review.writerUsername.eq(username));
        builder.and(review.softDeleteAudit.deletedAt.isNull()); // Non-deleted only for users

        if (request.getStoreId() != null) {
            builder.and(review.storeId.eq(request.getStoreId()));
        }

        if (request.getMinRating() != null) {
            builder.and(review.rating.goe(request.getMinRating()));
        }

        if (request.getMaxRating() != null) {
            builder.and(review.rating.loe(request.getMaxRating()));
        }

        if (request.getFromDate() != null) {
            builder.and(review.createAudit.createdAt.goe(toOffsetDateTime(request.getFromDate())));
        }

        if (request.getToDate() != null) {
            builder.and(review.createAudit.createdAt.loe(toOffsetDateTime(request.getToDate())));
        }

        if (StringUtils.hasText(request.getKeyword())) {
            builder.and(review.content.containsIgnoreCase(request.getKeyword()));
        }

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        List<Review> results = queryFactory
                .selectFrom(review)
                .where(builder)
                .orderBy(getOrderSpecifier(review, request.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(review.count())
                .from(review)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<Review> searchStoreReviews(UUID storeId, StoreReviewSearchReqDto request) {
        QReview review = QReview.review;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(review.storeId.eq(storeId));
        builder.and(review.softDeleteAudit.deletedAt.isNull());

        if (request.getMinRating() != null) {
            builder.and(review.rating.goe(request.getMinRating()));
        }

        if (request.getMaxRating() != null) {
            builder.and(review.rating.loe(request.getMaxRating()));
        }

        if (StringUtils.hasText(request.getKeyword())) {
            builder.and(review.content.containsIgnoreCase(request.getKeyword()));
        }

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        List<Review> results = queryFactory
                .selectFrom(review)
                .where(builder)
                .orderBy(getOrderSpecifier(review, request.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(review.count())
                .from(review)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<Review> searchAllReviews(ReviewSearchReqDto request) {
        QReview review = QReview.review;

        BooleanBuilder builder = new BooleanBuilder();

        if (Boolean.FALSE.equals(request.getIncludeDeleted())) {
            builder.and(review.softDeleteAudit.deletedAt.isNull());
        }

        if (request.getStoreId() != null) {
            builder.and(review.storeId.eq(request.getStoreId()));
        }

        if (request.getOrderId() != null) {
            builder.and(review.orderId.eq(request.getOrderId()));
        }

        if (request.getReviewId() != null) {
            builder.and(review.reviewId.eq(request.getReviewId()));
        }

        if (StringUtils.hasText(request.getWriter())) {
            builder.and(review.writerUsername.eq(request.getWriter()));
        }

        if (request.getMinRating() != null) {
            builder.and(review.rating.goe(request.getMinRating()));
        }

        if (request.getMaxRating() != null) {
            builder.and(review.rating.loe(request.getMaxRating()));
        }

        if (request.getFromDate() != null) {
            builder.and(review.createAudit.createdAt.goe(toOffsetDateTime(request.getFromDate())));
        }

        if (request.getToDate() != null) {
            builder.and(review.createAudit.createdAt.loe(toOffsetDateTime(request.getToDate())));
        }

        if (StringUtils.hasText(request.getKeyword())) {
            builder.and(review.content.containsIgnoreCase(request.getKeyword()));
        }

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        List<Review> results = queryFactory
                .selectFrom(review)
                .where(builder)
                .orderBy(getOrderSpecifier(review, request.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(review.count())
                .from(review)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(results, pageable, total);
    }

    private OrderSpecifier<?> getOrderSpecifier(QReview review, String sort) {
        if (!StringUtils.hasText(sort)) {
            return review.createAudit.createdAt.desc();
        }

        String[] parts = sort.split(",");
        String property = parts[0];
        Order order = (parts.length > 1 && parts[1].equalsIgnoreCase("asc")) ? Order.ASC : Order.DESC;

        PathBuilder<Review> pathBuilder = new PathBuilder<>(Review.class, "review");

        if (property.equals("createdAt")) {
            return order == Order.ASC ? review.createAudit.createdAt.asc() : review.createAudit.createdAt.desc();
        }
        if (property.equals("updatedAt")) {
            return order == Order.ASC ? review.updateAudit.updatedAt.asc() : review.updateAudit.updatedAt.desc();
        }

        return new OrderSpecifier(order, pathBuilder.get(property));
    }

    private OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
        return localDateTime.atOffset(ZoneOffset.UTC);
    }
}
