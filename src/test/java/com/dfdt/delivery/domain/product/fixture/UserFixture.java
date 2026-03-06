package com.dfdt.delivery.domain.product.fixture;

import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;

public class UserFixture {

    public static User createUser() {
        return User.builder()
                .username("tester")
                .password("password")
                .role(UserRole.MASTER)
                .name("testerName")
                .build();
    }

    public static User createOwnerUser() {
        return User.builder()
                .username("testerOwner")
                .password("password")
                .role(UserRole.OWNER)
                .name("testerOwnerName")
                .build();
    }

    public static User createAnotherUser() {
        return User.builder()
                .username("testerAnother")
                .password("password")
                .role(UserRole.OWNER)
                .name("testerAnotherName")
                .build();
    }
}
