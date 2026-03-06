package com.dfdt.delivery.domain.payment.infrastructure.listener;

import com.dfdt.delivery.domain.payment.application.service.command.PaymentCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentTimeoutHandler implements MessageListener {

    private final PaymentCommandService paymentCommandService;
    private static final String TIMEOUT_KEY_PREFIX = "payment:timeout:";

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        // Expired 된 키가 "payment:timeout:{orderId}" 형식인 경우에만 처리합니다.
        if (expiredKey.startsWith(TIMEOUT_KEY_PREFIX)) {
            String orderIdStr = expiredKey.replace(TIMEOUT_KEY_PREFIX, "");
            try {
                UUID orderId = UUID.fromString(orderIdStr);
                paymentCommandService.timeoutPayment(orderId);
            } catch (IllegalArgumentException e) {
                log.error("유효하지 않은 Order ID 형식입니다: {}", orderIdStr);
            } catch (Exception e) {
                log.error("결제 타임아웃 처리 중 예외 발생. OrderId: {}, Error: {}", orderIdStr, e.getMessage(), e);
            }
        }
    }
}
