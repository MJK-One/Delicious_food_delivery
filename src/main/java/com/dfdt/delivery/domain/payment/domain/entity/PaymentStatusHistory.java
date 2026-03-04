package com.dfdt.delivery.domain.payment.domain.entity;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.domain.payment.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "p_payment_status_history")
public class PaymentStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_status_history_id", nullable = false, updatable = false)
    private UUID paymentStatusHistoryId;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "changed_by", nullable = false, length = 10)
    private String changedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20)
    private PaymentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 20)
    private PaymentStatus toStatus;

    @Column(name = "change_reason", length = 255)
    private String changeReason;

    @Embedded
    private CreateAudit createAudit;

    @Embedded
    private SoftDeleteAudit softDeleteAudit;

    public static PaymentStatusHistory create(
            UUID paymentId,
            UUID orderId,
            String actorUsername,
            PaymentStatus fromStatus,
            PaymentStatus toStatus,
            String reason
    ) {
        return PaymentStatusHistory.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .changedBy(actorUsername)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changeReason(reason)
                .createAudit(CreateAudit.now(actorUsername))
                .softDeleteAudit(SoftDeleteAudit.active())
                .build();
    }

    public void delete(String deletedBy) {
        this.softDeleteAudit.softDelete(deletedBy);
    }

    public boolean isDeleted() {
        return softDeleteAudit.isDeleted();
    }
}