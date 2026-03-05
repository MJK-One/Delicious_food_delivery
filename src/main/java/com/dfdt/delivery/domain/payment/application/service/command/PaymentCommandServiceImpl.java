package com.dfdt.delivery.domain.payment.application.service.command;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.common.util.RedisService;
import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.order.domain.enums.OrderErrorCode;
import com.dfdt.delivery.domain.order.domain.enums.OrderStatus;
import com.dfdt.delivery.domain.order.domain.repository.OrderRepository;
import com.dfdt.delivery.domain.payment.application.converter.PaymentConverter;
import com.dfdt.delivery.domain.payment.domain.entity.Payment;
import com.dfdt.delivery.domain.payment.domain.entity.PaymentStatusHistory;
import com.dfdt.delivery.domain.payment.domain.enums.PaymentErrorCode;
import com.dfdt.delivery.domain.payment.domain.enums.PaymentStatus;
import com.dfdt.delivery.domain.payment.domain.repository.PaymentRepository;
import com.dfdt.delivery.domain.payment.domain.repository.PaymentStatusHistoryRepository;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentApproveReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentCreateReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentDetailResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentHiddenToggleResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentCommandServiceImpl implements PaymentCommandService {

    private final PaymentRepository paymentRepository;
    private final PaymentStatusHistoryRepository historyRepository;
    private final OrderRepository orderRepository;
    private final RedisService redisService;

    private static final String TIMEOUT_KEY_PREFIX = "payment:timeout:";
    private static final long FIVE_MINUTES_MS = 5 * 60 * 1000L;

    @Override
    @Transactional
    public PaymentDetailResDto createPayment(PaymentCreateReqDto reqDto, String username) {
        Payment payment = PaymentConverter.toEntity(reqDto, username);
        Payment savedPayment = paymentRepository.save(payment);

        saveHistory(savedPayment, username, null, PaymentStatus.READY, "결제 생성");

        // Redis TTL 설정 (5분)
        redisService.setData(TIMEOUT_KEY_PREFIX + savedPayment.getOrderId(), "PENDING", FIVE_MINUTES_MS);

        return PaymentConverter.toDetailResDto(savedPayment);
    }

    @Override
    @Transactional
    public void timeoutPayment(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getPaymentStatus() != PaymentStatus.READY) {
            return;
        }

        payment.markFailed("SYSTEM", "결제 시간 초과 (5분 경과)");
        saveHistory(payment, "SYSTEM", PaymentStatus.READY, PaymentStatus.FAILED, "결제 타임아웃 자동 처리");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        order.updateStatus(OrderStatus.CANCELED, "결제 시간 초과로 인한 자동 주문 취소");
    }

    private void saveHistory(Payment payment, String username, PaymentStatus from, PaymentStatus to, String reason) {
        PaymentStatusHistory history = PaymentConverter.toStatusHistoryEntity(
                payment,
                username,
                from,
                to,
                reason
        );
        historyRepository.save(history);
    }

    @Override
    @Transactional
    public PaymentDetailResDto approvePayment(UUID paymentId, PaymentApproveReqDto reqDto) {
        // TODO: 결제 승인
        return null;
    }

    @Override
    @Transactional
    public PaymentDetailResDto cancelPayment(UUID paymentId) {
        // TODO: 결제 취소
        return null;
    }

    @Override
    @Transactional
    public void deletePayment(UUID paymentId) {
        // TODO: 결제 삭제
    }

    @Override
    @Transactional
    public PaymentHiddenToggleResDto toggleHidden(UUID paymentId, Boolean hidden) {
        // TODO: 숨김 토글
        return null;
    }
}
