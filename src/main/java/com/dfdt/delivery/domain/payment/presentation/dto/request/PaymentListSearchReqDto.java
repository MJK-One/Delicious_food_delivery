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
public class PaymentListSearchReqDto {

    /**
     * 주문 ID로 검색
     */
    private UUID orderId;

    /**
     * 결제 상태 (READY / PAID / FAILED / CANCELED)
     */
    private PaymentStatus paymentStatus;

    /**
     * 생성일시 시작 (포함)
     */
    private OffsetDateTime from;

    /**
     * 생성일시 종료 (포함)
     */
    private OffsetDateTime to;

    /**
     * 숨김 결제 포함 여부 (운영자용)
     * default = false
     */
    private Boolean includeHidden;

    /**
     * 삭제 여부 (운영자용)
     * default = false
     */
    private Boolean isDeleted;

    /**
     * 페이지 번호 (0부터)
     */
    private Integer page;

    /**
     * 페이지 크기 (10 / 30 / 50 만 허용)
     * 그 외 값은 서비스단에서 10으로 보정
     */
    private Integer size;

    /**
     * 정렬 기준
     * 기본값 ex) createdAt,desc
     */
    private String sort;

}