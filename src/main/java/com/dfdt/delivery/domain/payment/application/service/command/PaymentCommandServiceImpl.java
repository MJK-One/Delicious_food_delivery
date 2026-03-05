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
    public PaymentDetailResDto approvePayment(UUID paymentId, PaymentApproveReqDto reqDto, String username) {
        // 1. 결제 데이터 조회
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        // 2. READY 상태일 때만 승인 가능
        if (payment.getPaymentStatus() != PaymentStatus.READY) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }

        PaymentStatus fromStatus = payment.getPaymentStatus();

        // 3. 주문 정보 조회 (상태 변경을 위해)
        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        if (reqDto.getResult() == PaymentStatus.PAID) {
            // 4-1. 승인 성공 처리
            payment.markPaid(
                    username,
                    reqDto.getPgProvider(),
                    reqDto.getPgTransactionId() != null ? reqDto.getPgTransactionId() : UUID.randomUUID().toString()
            );
            saveHistory(payment, username, fromStatus, PaymentStatus.PAID, "결제 승인 완료");

            // 주문 상태를 PAID로 변경
            order.updateStatus(OrderStatus.PAID, "결제 승인 완료로 인한 주문 상태 변경");

            // Redis TTL 키 즉시 삭제 (타임아웃 방지)
            redisService.deleteData(TIMEOUT_KEY_PREFIX + payment.getOrderId());
        } else {
            // 4-2. 승인 실패 처리
            String reason = reqDto.getFailureReason() != null ? reqDto.getFailureReason() : "PG 승인 거절";
            payment.markFailed(username, reason);
            saveHistory(payment, username, fromStatus, PaymentStatus.FAILED, "결제 승인 거절: " + reason);

            // 주문 상태를 CANCELED로 변경
            order.updateStatus(OrderStatus.CANCELED, "결제 승인 거부로 인한 주문 취소");

            // Redis TTL 키 삭제
            redisService.deleteData(TIMEOUT_KEY_PREFIX + payment.getOrderId());
        }

        return PaymentConverter.toDetailResDto(payment);
    }

    @Override
    @Transactional
    public PaymentDetailResDto cancelPayment(UUID paymentId, String username) {
        // 1. 결제 데이터 조회
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        // 2. 결제 상태 확인 (이미 취소되었거나 실패한 경우 제외)
        if (payment.getPaymentStatus() == PaymentStatus.CANCELED || payment.getPaymentStatus() == PaymentStatus.FAILED) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }

        // 3. 주문 정보 조회 및 상태 체크
        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        // PENDING, PAID 상태일 때만 취소 가능 (ACCEPTED 이후로는 취소 불가)
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }

        PaymentStatus fromStatus = payment.getPaymentStatus();

        // 4. 결제 상태 CANCELED 처리
        payment.markCanceled(username, "사용자 요청으로 인한 취소");
        saveHistory(payment, username, fromStatus, PaymentStatus.CANCELED, "사용자 요청 결제 취소");

        // 5. 주문 상태 CANCELED 변경
        order.updateStatus(OrderStatus.CANCELED, "결제 취소로 인한 주문 자동 취소");

        // 6. Redis TTL 키 즉시 삭제
        redisService.deleteData(TIMEOUT_KEY_PREFIX + payment.getOrderId());

        return PaymentConverter.toDetailResDto(payment);
    }

    @Override
    @Transactional
    public void deletePayment(UUID paymentId, String username) {
        // TODO: 결제 삭제
    }

    @Override
    @Transactional
    public PaymentHiddenToggleResDto toggleHidden(UUID paymentId, Boolean hidden, String username) {
        // TODO: 숨김 토글
        return null;
    }
}
