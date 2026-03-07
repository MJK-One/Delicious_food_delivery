package com.dfdt.delivery.domain.order.application.service;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.order.domain.enums.OrderErrorCode;
import com.dfdt.delivery.domain.order.domain.enums.OrderStatus;
import com.dfdt.delivery.domain.order.domain.event.OrderEvent;
import com.dfdt.delivery.domain.order.domain.repository.OrderRepository;
import com.dfdt.delivery.domain.order.infrastructure.persistence.redis.OrderRedisService;
import com.dfdt.delivery.domain.payment.application.service.command.PaymentCommandService;
import com.dfdt.delivery.domain.payment.domain.enums.PaymentErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
public class OrderExpirationService {

    // 순환 참조로 인해 Lazy 방식 이용
    private final OrderRepository orderRepository;
    private final @Lazy PaymentCommandService paymentCommandService;
    private final OrderRedisService orderRedisService;

    public OrderExpirationService(
            OrderRepository orderRepository,
            @Lazy PaymentCommandService paymentCommandService,
            OrderRedisService orderRedisService) {
        this.orderRepository = orderRepository;
        this.paymentCommandService = paymentCommandService;
        this.orderRedisService = orderRedisService;
    }

    @Transactional
    public void completePayment(UUID orderId) {
        // 1. 주문 상태를 PAID로 변경
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        order.updateStatus(OrderStatus.PAID, "결제 완료");

        // 2. 기존  TTL 삭제
        orderRedisService.cancelTimeOut(orderId);

        // 3. 사장님 수락 대기(10분) TTL 생성
        orderRedisService.setAcceptTimeout(orderId);
        log.info("주문 {}번: 결제 완료 처리 및 사장님 수락 타이머 시작", orderId);
    }
    /**
     * [1] 결제 시간 초과 이벤트 처리 (10분 만료)
     */
    @EventListener
    @Transactional
    public void handlePaymentTimeout(OrderEvent.PaymentTimeout event) {
        log.info("결제 시간 초과로 인한 주문 취소 처리: {}", event.orderId());

        // 주문을 찾아서 '결제 시간 초과 취소' 상태로 변경
        orderRepository.findById(event.orderId())
                .ifPresent(order ->
                        {
                            if (order.getStatus() == OrderStatus.PENDING)
                                order.updateStatus(OrderStatus.REJECTED,"결제 대기 시간 초과");
                            // 결제된 상태로 남아 있다면 가게 수락 TTL
                            if (order.getStatus() == OrderStatus.PAID)
                                orderRedisService.setAcceptTimeout(order.getOrderId());
                        });

    }

    /**
     * [2] 사장님 접수 시간 초과 이벤트 처리 (10분 만료)
     */
    @EventListener
    @Transactional
    public void handleAcceptanceTimeout(OrderEvent.AcceptanceTimeout event) {
        log.info("사장님 미접수로 인한 주문 취소 처리: {}", event.orderId());

        // 가게 미접수 처리 , 환불
        orderRepository.findByIdWithLock(event.orderId())
                .ifPresent(order -> {
                    if (order.getStatus().isBeforeAcceptance())
                    {
                        // 주문 상태를 REJECTED로 변경
                        order.updateStatus(OrderStatus.REJECTED, "주문 후 가게 미수락 취소");

                        // 결제 정보 조회 및 환불 로직 실행
                        orderRepository.findPaymentOrderId(event.orderId())
                                .ifPresentOrElse(payment -> {
                                    log.info("결제 취소 요청 실행: 결제ID {}", payment.getPaymentId());
                                    paymentCommandService.cancelPayment(payment.getPaymentId(),order.getUser().getUsername());
                                }
                                , () -> {
                                    throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS);});
                    }
                });
    }
}