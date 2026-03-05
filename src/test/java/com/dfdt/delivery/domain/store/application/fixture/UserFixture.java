package com.dfdt.delivery.domain.store.application.fixture;

import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
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

    public static CustomUserDetails createUserDetails() {
        return new CustomUserDetails(createUser());
    }
}
