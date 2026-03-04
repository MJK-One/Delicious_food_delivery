package com.dfdt.delivery.domain.review.entity;

import com.dfdt.delivery.common.Entity.BaseAuditSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Review extends BaseAuditSoftDeleteEntity {

    @Id
    @UuidGenerator
    @Column(name = "review_id", nullable = false, updatable = false)
    private UUID reviewId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId; // 주문당 리뷰 1개

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1~5

    @Column(name = "content", length = 500)
    private String content;

    public void update(Integer newRating, String newContent) {
        if (newRating != null) this.rating = newRating;
        if (newContent != null) this.content = newContent;
    }
}