package com.dfdt.delivery.domain.payment.application.service;

import com.dfdt.delivery.domain.payment.application.service.command.PaymentCommandService;
import com.dfdt.delivery.domain.payment.domain.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentExpirationService {

    private final PaymentCommandService paymentCommandService;

    @EventListener
    @Transactional
    public void handlePaymentTimeout(PaymentEvent.PaymentTimeout event) {
        log.info("결제 시간 초과로 인한 자동 처리 시작. 주문 ID: {}", event.orderId());
        paymentCommandService.timeoutPayment(event.orderId());
    }
}
