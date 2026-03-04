package com.dfdt.delivery.domain.payment.entity;

import com.dfdt.delivery.common.Entity.BaseAuditSoftDeleteEntity;
import com.dfdt.delivery.domain.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PaymentStatusHistory extends BaseAuditSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_status_history_id", nullable = false, updatable = false)
    private UUID paymentStatusHistoryId;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "changed_by", nullable = false, length = 10)
    private String changedBy; // 상태 변경자

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20)
    private PaymentStatus fromStatus; // 최초 생성 시 null 가능

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 20)
    private PaymentStatus toStatus;

    @Column(name = "change_reason", length = 255)
    private String changeReason;

    public static PaymentStatusHistory of(
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
                .build();
    }
}