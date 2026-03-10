package com.dfdt.delivery.domain.product.infrastructure.adapter;

import com.dfdt.delivery.domain.ai.domain.port.ProductForAiPort;
import com.dfdt.delivery.domain.ai.domain.port.ProductInfo;
import com.dfdt.delivery.domain.product.domain.entity.Product;
import com.dfdt.delivery.domain.product.domain.repository.JpaProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * AI 도메인의 ProductForAiPort를 Product 도메인 인프라에서 구현합니다.
 * AI 도메인이 Product 엔티티/레포지토리를 직접 참조하지 않도록 중간 역할을 합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductForAiAdapter implements ProductForAiPort {

    private final JpaProductRepository productRepository;

    @Override
    public Optional<ProductInfo> findActive(UUID productId, UUID storeId) {
        Optional<ProductInfo> result = productRepository.findByProductIdAndStoreId(productId, storeId)
                .filter(p -> p.getSoftDeleteAudit() == null || !p.getSoftDeleteAudit().isDeleted())
                .map(p -> new ProductInfo(p.getProductId(), p.getName()));

        if (result.isEmpty()) {
            log.warn("[ProductForAiAdapter] 활성 상품 없음 - productId={}, storeId={}", productId, storeId);
        }
        return result;
    }

    @Override
    public Optional<String> applyAiDescription(UUID productId, UUID storeId,
                                                String aiDescription, String username) {
        Optional<String> result = productRepository.findByProductIdAndStoreId(productId, storeId)
                .filter(p -> p.getSoftDeleteAudit() == null || !p.getSoftDeleteAudit().isDeleted())
                .map(product -> {
                    String previous = product.getDescription();
                    product.applyAiDescription(aiDescription, username);
                    log.info("[ProductForAiAdapter] AI 설명 적용 - productId={}, username={}", productId, username);
                    return previous;
                });

        if (result.isEmpty()) {
            log.warn("[ProductForAiAdapter] AI 설명 적용 대상 상품 없음 - productId={}, storeId={}", productId, storeId);
        }
        return result;
    }

    @Override
    public boolean restoreDescription(UUID productId, UUID storeId,
                                      String previousDescription, String username) {
        Optional<Product> optProduct = productRepository.findByProductIdAndStoreId(productId, storeId)
                .filter(p -> p.getSoftDeleteAudit() == null || !p.getSoftDeleteAudit().isDeleted());

        if (optProduct.isEmpty()) {
            log.warn("[ProductForAiAdapter] 원복 대상 상품 없음 - productId={}, storeId={}", productId, storeId);
            return false;
        }

        optProduct.get().restoreDescription(previousDescription, username);
        log.info("[ProductForAiAdapter] 상품 설명 원복 완료 - productId={}, username={}", productId, username);
        return true;
    }
}
