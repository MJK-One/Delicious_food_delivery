package com.dfdt.delivery.domain.review.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "p_review_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ReviewImage {

    @Id
    @UuidGenerator
    @Column(name = "review_image_id", nullable = false, updatable = false)
    private UUID reviewImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public static ReviewImage create(String imageUrl, int order) {
        return ReviewImage.builder()
                .imageUrl(imageUrl)
                .displayOrder(order)
                .build();
    }

    public void assignReview(Review review) {
        this.review = review;
    }
}