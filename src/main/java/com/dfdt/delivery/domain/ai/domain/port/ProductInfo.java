package com.dfdt.delivery.domain.ai.domain.port;

import java.util.UUID;

/**
 * AI 도메인이 Product 도메인으로부터 수신하는 상품 조회 결과.
 * Product 엔티티를 직접 참조하지 않기 위해 필요한 필드만 보유합니다.
 */
public record ProductInfo(UUID productId, String name) {}
