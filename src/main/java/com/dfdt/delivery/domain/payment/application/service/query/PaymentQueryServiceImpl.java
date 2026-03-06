package com.dfdt.delivery.domain.payment.application.service.query;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.payment.application.converter.PaymentConverter;
import com.dfdt.delivery.domain.payment.domain.entity.Payment;
import com.dfdt.delivery.domain.payment.domain.enums.PaymentErrorCode;
import com.dfdt.delivery.domain.payment.domain.repository.PaymentRepository;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentHistorySearchReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentListSearchReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentDetailResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentHistoryResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentListItemResDto;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryServiceImpl implements PaymentQueryService {

    private final PaymentRepository paymentRepository;

    @Override
    public PaymentDetailResDto getPayment(UUID paymentId, String username, UserRole role) {

        Payment payment = paymentRepository.findByIdWithRoleCheck(paymentId, username, role)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        return PaymentConverter.toDetailResDto(payment);
    }

    @Override
    public Page<PaymentListItemResDto> listPayments(PaymentListSearchReqDto reqDto, Pageable pageable, String username, UserRole role) {
        return paymentRepository.searchPayments(reqDto, pageable, username, role);
    }

    @Override
    public Page<PaymentHistoryResDto> listPaymentHistory(PaymentHistorySearchReqDto reqDto, Pageable pageable) {
        return paymentRepository.searchPaymentHistory(reqDto, pageable);
    }
}