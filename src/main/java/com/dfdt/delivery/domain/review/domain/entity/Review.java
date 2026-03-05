package com.dfdt.delivery.domain.review.domain.entity;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.UpdateAudit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

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
        this.softDeleteAudit.softDelete(deletedBy);
    }

    public boolean isDeleted() {
        return this.softDeleteAudit.isDeleted();
    }
}