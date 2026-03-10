package com.dfdt.delivery.domain.ai.domain.port;

import java.util.Optional;
import java.util.UUID;

/**
 * AI 도메인이 Product 도메인에 요청하는 연산을 정의하는 포트.
 * 구현체는 Product 도메인 infrastructure 계층에 위치합니다.
 */
public interface ProductForAiPort {

    /**
     * 활성 상품 조회. soft-delete된 경우 Optional.empty() 반환.
     */
    Optional<ProductInfo> findActive(UUID productId, UUID storeId);

    /**
     * 상품에 AI 설명을 적용하고 적용 이전의 description을 반환합니다.
     * 상품이 없거나 soft-delete된 경우 Optional.empty() 반환.
     */
    Optional<String> applyAiDescription(UUID productId, UUID storeId, String aiDescription, String username);

    /**
     * 상품 설명을 이전 값으로 복원합니다.
     * 상품이 없거나 soft-delete된 경우 false 반환.
     */
    boolean restoreDescription(UUID productId, UUID storeId, String previousDescription, String username);
}
