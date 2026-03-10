package com.dfdt.delivery.domain.review.domain.entity;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.UpdateAudit;
import com.dfdt.delivery.domain.review.domain.enums.ReviewErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Review {

    @Id
    @UuidGenerator
    @Column(name = "review_id", nullable = false, updatable = false)
    private UUID reviewId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "writer_username", nullable = false)
    private String writerUsername;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "content", length = 500)
    private String content;

    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ReviewImage> images = new ArrayList<>();

    @Embedded
    private CreateAudit createAudit;

    @Embedded
    private UpdateAudit updateAudit;

    @Embedded
    private SoftDeleteAudit softDeleteAudit;

    public static Review create(
            UUID orderId,
            UUID storeId,
            String writerUsername,
            Integer rating,
            String content,
            String createdBy
    ) {
        return Review.builder()
                .orderId(orderId)
                .storeId(storeId)
                .writerUsername(writerUsername)
                .rating(rating)
                .content(content)
                .createAudit(CreateAudit.now(createdBy))
                .updateAudit(UpdateAudit.empty())
                .softDeleteAudit(SoftDeleteAudit.active())
                .build();
    }

    public void update(Integer newRating, String newContent, String updatedBy) {
        if (newRating != null) this.rating = newRating;
        if (newContent != null) this.content = newContent;

        this.updateAudit.touch(updatedBy);
    }

    public void delete(String deletedBy) {
        if (this.softDeleteAudit == null) {
            this.softDeleteAudit = SoftDeleteAudit.active();
        }
        this.softDeleteAudit.softDelete(deletedBy);
    }

    public boolean isDeleted() {
        return this.softDeleteAudit != null && this.softDeleteAudit.isDeleted();
    }

    public void addImage(String imageUrl) {
        if (this.images.size() >= 5) {
            throw new BusinessException(ReviewErrorCode.IMAGE_LIMIT_EXCEEDED);
        }

        int order = this.images.size() + 1;

        ReviewImage image = ReviewImage.create(imageUrl, order);
        image.assignReview(this);

        this.images.add(image);
    }

    public void updateImages(List<String> imageUrls) {
        this.images.clear();
        if (imageUrls != null) {
            imageUrls.forEach(this::addImage);
        }
    }
}