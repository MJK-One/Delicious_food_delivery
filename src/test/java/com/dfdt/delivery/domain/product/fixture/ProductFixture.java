package com.dfdt.delivery.domain.product.fixture;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.domain.product.domain.entity.Product;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.user.domain.entity.User;

import java.util.List;
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

    public static List<Product> createProducts(User user, Store store) {
        Product product1 = Product.builder()
                .productId(UUID.randomUUID())
                .name("상품명1")
                .description("상품 설명1")
                .price(10000)
                .isHidden(false)
                .isAiDescription(false)
                .displayOrder(0)
                .store(store)
                .createAudit(CreateAudit.now(user.getUsername()))
                .build();

        Product product2 = Product.builder()
                .productId(UUID.randomUUID())
                .name("상품명2")
                .description("상품 설명2")
                .price(10000)
                .isHidden(false)
                .isAiDescription(false)
                .displayOrder(0)
                .store(store)
                .createAudit(CreateAudit.now(user.getUsername()))
                .build();

        return List.of(product1, product2);
    }
}
