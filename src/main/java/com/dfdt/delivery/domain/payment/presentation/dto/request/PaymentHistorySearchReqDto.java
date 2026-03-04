package com.dfdt.delivery.domain.payment.presentation.dto.request;

import com.dfdt.delivery.domain.payment.domain.enums.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class PaymentHistorySearchReqDto {

    /**
     * 특정 주문의 결제 이력 필터
     */
    private UUID orderId;

    /**
     * 특정 결제의 이력 필터
     */
    private UUID paymentId;

    /**
     * 변경자(username)
     */
    private String changedBy;

    /**
     * 변경 후 상태
     */
    private PaymentStatus toStatus;

    /**
     * 변경 전 상태
     */
    private PaymentStatus fromStatus;

    /**
     * 조회 시작 시각 (포함)
     */
    private OffsetDateTime fromDate;

    /**
     * 조회 종료 시각 (포함)
     */
    private OffsetDateTime toDate;

    /**
     * 사유 키워드 (부분 일치)
     */
    private String reasonContains;

    /**
     * 페이지 번호 (0부터)
     */
    private Integer page;

    /**
     * 페이지 크기
     */
    private Integer size;

    /**
     * 정렬 기준
     * 예) createdAt,desc
     */
    private String sort;
}