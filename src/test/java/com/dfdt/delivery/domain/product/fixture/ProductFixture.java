package com.dfdt.delivery.domain.product.fixture;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.domain.product.domain.entity.Product;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.user.domain.entity.User;

import java.util.UUID;

public class ProductFixture {

    public static Product createProduct(User user, Store store) {
        return Product.builder()
                .productId(UUID.randomUUID())
                .name("상품명")
                .description("상품 설명")
                .price(10000)
                .isHidden(false)
                .isAiDescription(false)
                .displayOrder(1)
                .store(store)
                .createAudit(CreateAudit.now(user.getUsername()))
                .build();
    }

    public static Product createNoIdProduct(User user, Store store, String name, Integer displayOrder) {
        return Product.builder()
                .name(name)
                .description("상품 설명")
                .price(10000)
                .isHidden(false)
                .isAiDescription(false)
                .displayOrder(displayOrder)
                .store(store)
                .createAudit(CreateAudit.now(user.getUsername()))
                .build();
    }
}
