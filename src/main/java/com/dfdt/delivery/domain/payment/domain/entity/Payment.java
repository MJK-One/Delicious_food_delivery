package com.dfdt.delivery.domain.payment.domain.entity;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.UpdateAudit;
import com.dfdt.delivery.domain.payment.domain.enums.PaymentMethod;
import com.dfdt.delivery.domain.payment.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id", nullable = false, updatable = false)
    private UUID paymentId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "pg_provider", length = 50)
    private String pgProvider;

    @Column(name = "pg_transaction_id", length = 100)
    private String pgTransactionId;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @Column(name = "failed_at")
    private OffsetDateTime failedAt;

    @Column(name = "canceled_at")
    private OffsetDateTime canceledAt;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "hidden_at")
    private OffsetDateTime hiddenAt;

    @Column(name = "hidden_by", length = 50)
    private String hiddenBy;

    @Embedded
    private CreateAudit createAudit;

    @Embedded
    private UpdateAudit updateAudit;

    @Embedded
    private SoftDeleteAudit softDeleteAudit;

    public static Payment create(
            UUID orderId,
            PaymentMethod paymentMethod,
            Integer amount,
            String createdBy
    ) {
        return Payment.builder()
                .orderId(orderId)
                .paymentMethod(paymentMethod)
                .paymentStatus(PaymentStatus.READY)
                .amount(amount)
                .createAudit(CreateAudit.now(createdBy))
                .updateAudit(UpdateAudit.empty())
                .softDeleteAudit(SoftDeleteAudit.active())
                .build();
    }

    public void markPaid(String updatedBy,
                         String pgProvider,
                         String pgTransactionId) {
        this.paymentStatus = PaymentStatus.PAID;
        this.paidAt = OffsetDateTime.now();
        this.pgProvider = pgProvider;
        this.pgTransactionId = pgTransactionId;
        ensureUpdateAudit();
        this.updateAudit.touch(updatedBy);
    }

    public void markFailed(String updatedBy, String failureReason) {
        this.paymentStatus = PaymentStatus.FAILED;
        this.failedAt = OffsetDateTime.now();
        this.failureReason = failureReason;
        ensureUpdateAudit();
        this.updateAudit.touch(updatedBy);
    }

    public void markCanceled(String updatedBy, String reason) {
        this.paymentStatus = PaymentStatus.CANCELED;
        this.canceledAt = OffsetDateTime.now();
        this.failureReason = reason;
        ensureUpdateAudit();
        this.updateAudit.touch(updatedBy);
    }

    public void softDelete(String deletedBy) {
        this.softDeleteAudit.softDelete(deletedBy);
    }

    public void hide(String username) {
        this.hiddenAt = OffsetDateTime.now();
        this.hiddenBy = username;
        ensureUpdateAudit();
        this.updateAudit.touch(username);
    }

    public void unhide(String username) {
        this.hiddenAt = null;
        this.hiddenBy = null;
        ensureUpdateAudit();
        this.updateAudit.touch(username);
    }

    private void ensureUpdateAudit() {
        if (this.updateAudit == null) {
            this.updateAudit = UpdateAudit.empty();
        }
    }

    public boolean isHidden() {
        return this.hiddenAt != null;
    }
}