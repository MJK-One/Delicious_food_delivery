package com.dfdt.delivery.domain.user.entity;

import com.dfdt.delivery.common.Entity.BaseAuditSoftDeleteEntity;
import com.dfdt.delivery.domain.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;

@Entity
@Table(
        name = "p_user",
        indexes = {
                @Index(name = "idx_user_role", columnList = "role")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseAuditSoftDeleteEntity {
    @Id
    @Column(length = 10, updatable = false, nullable = false)
    private String username;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 60)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Builder
    public User(String username, String name, String password, UserRole role) {
        this.username = username;
        this.name = name;
        this.password = password;
        this.role = role;
    }

    // 로그인 성공 시 호출
    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
