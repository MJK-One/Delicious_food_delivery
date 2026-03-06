package com.dfdt.delivery.domain.payment.domain.repository;

import com.dfdt.delivery.domain.payment.domain.entity.Payment;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentHistorySearchReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentListSearchReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentHistoryResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentListItemResDto;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PaymentCustomRepository {
    /**
     * 역할별 권한이 반영된 결제 단건 조회
     */
    Optional<Payment> findByIdWithRoleCheck(
            UUID paymentId,
            String username,
            UserRole role
    );

    /**
     * 역할별 권한이 반영된 결제 목록 조회
     */
    Page<PaymentListItemResDto> searchPayments(
            PaymentListSearchReqDto reqDto,
            Pageable pageable,
            String username,
            UserRole role
    );

    /**
     * 결제 히스토리 조회 (MASTER 전용)
     */
    Page<PaymentHistoryResDto> searchPaymentHistory(
            PaymentHistorySearchReqDto reqDto,
            Pageable pageable
    );
}
