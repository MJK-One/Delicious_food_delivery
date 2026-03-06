package com.dfdt.delivery.domain.order.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public enum OrderRedisPrefix {
    ORDER_PENDING("order:pending:%s", Duration.ofMinutes(10)),
    OWNER_CONFIRM("order:accept:%s",Duration.ofMinutes(10));

    private final String prefix;
    private final Duration ttl;
    
    // 템플릿 생성
    public String generateKey(Object id) {
        return String.format(this.prefix, id.toString());
    }
    
    // 아이디 추출
    public UUID parseId(String fullKey) {
        String prefixOnly = this.prefix.replace("%s", "");
        String key = fullKey.replace(prefixOnly, "");
        return UUID.fromString(key);
    }
    
    // 키가 맞는지 확인
    public boolean isMatched(String key) {
        String prefixOnly = this.prefix.replace("%s", "");
        return key.startsWith(prefixOnly);
    }
    // 어떤 ENUM 사용하는 지 확인
    public static OrderRedisPrefix fromKey(String fullKey) {
        return Arrays.stream(OrderRedisPrefix.values())
                .filter(p -> p.isMatched(fullKey))
                .findFirst()
                .orElse(null);
    }
}
