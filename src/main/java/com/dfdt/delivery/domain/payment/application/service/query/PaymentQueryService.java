package com.dfdt.delivery.domain.payment.application.service.query;

import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentHistorySearchReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentListSearchReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentDetailResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentHistoryResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentListItemResDto;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PaymentQueryService {

    PaymentDetailResDto getPayment(UUID paymentId, String username, UserRole role);

    Page<PaymentListItemResDto> listPayments(PaymentListSearchReqDto reqDto, Pageable pageable, String username, UserRole role);

    Page<PaymentHistoryResDto> listPaymentHistory(PaymentHistorySearchReqDto reqDto, Pageable pageable);
}
