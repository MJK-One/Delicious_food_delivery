package com.dfdt.delivery.domain.order.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    
    // 진행 단계, 설명
    PENDING(1, "결제 대기"),
    PAID(2, "결제 완료"),
    ACCEPTED(3, "주문 수락(조리 중)"),
    COOKING_DONE(4, "조리 완료"),
    DELIVERING(5, "배달 중"),
    DELIVERED(6, "배달 완료"),
    COMPLETED(7, "주문 확정"),

    // 프로세스 종료 상태 (Step 0 이하)
    REJECTED(0, "주문 거절"),
    CANCELED(0, "주문 취소"),
    HIDDEN(-1, "숨김(사용자 삭제) 처리");

    private final int step;
    private final String description;

    // 사장님 수락 전인지 확인 (결제 대기, 결제 완료 포함)
    public boolean isBeforeAcceptance() {
        return this.step > 0 && this.step < ACCEPTED.getStep();
    }

    // 이미 프로세스가 종료된 상태인지 확인
    public boolean isTerminated() {
        return this.step <= 0 || this == COMPLETED;
    }
}