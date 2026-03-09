package com.dfdt.delivery.domain.category.application.fixture;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.domain.category.domain.entity.Category;

import java.util.UUID;

public class CategoryFixture {

    public static Category createCategory() {
        return Category.builder()
                .categoryId(UUID.randomUUID())
                .name("테스트 카테고리")
                .description("카테고리 설명")
                .sortOrder(0)
                .isActive(true)
                .createAudit(CreateAudit.now("테스트 유저"))
                .build();
    }

    public static Category repoCategory(String name, Integer sortOrder) {
        return Category.builder()
                .name(name)
                .description("카테고리 설명")
                .sortOrder(sortOrder)
                .isActive(true)
                .createAudit(CreateAudit.now("테스트 유저"))
                .build();
    }
}
