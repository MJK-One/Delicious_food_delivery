package com.dfdt.delivery.domain.payment.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public enum PaymentRedisPrefix {
    PAYMENT_TIMEOUT("payment:timeout:%s", Duration.ofMinutes(5));

    private final String prefix;
    private final Duration ttl;

    public String generateKey(Object id) {
        return String.format(this.prefix, id.toString());
    }

    public UUID parseId(String fullKey) {
        String prefixOnly = this.prefix.replace("%s", "");
        String key = fullKey.replace(prefixOnly, "");
        return UUID.fromString(key);
    }

    public boolean isMatched(String key) {
        String prefixOnly = this.prefix.replace("%s", "");
        return key.startsWith(prefixOnly);
    }

    public static PaymentRedisPrefix fromKey(String fullKey) {
        return Arrays.stream(PaymentRedisPrefix.values())
                .filter(p -> p.isMatched(fullKey))
                .findFirst()
                .orElse(null);
    }
}
