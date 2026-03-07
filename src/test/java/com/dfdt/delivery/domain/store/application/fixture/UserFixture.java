package com.dfdt.delivery.domain.store.application.fixture;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import org.springframework.test.util.ReflectionTestUtils;

public class UserFixture {

    public static User createUser() {
        return User.builder()
                .username("tester")
                .password("password")
                .role(UserRole.MASTER)
                .name("testerName")
                .build();
    }

    public static User createRepoUser() {
        User user = User.builder()
                .username("rUser1")
                .name("repoUserName")
                .password("password")
                .role(UserRole.OWNER)
                .build();

        ReflectionTestUtils.setField(user, "createAudit", CreateAudit.now("master"));

        return user;
    }

    public static User createAnotherRepoUser() {
        User user =  User.builder()
                .username("rUser2")
                .password("password")
                .role(UserRole.OWNER)
                .name("testerAnotherName")
                .build();

        ReflectionTestUtils.setField(user, "createAudit", CreateAudit.now("master"));

        return user;
    }
}
