package com.dfdt.delivery.domain.user.domain.entity;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.UpdateAudit;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;


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
@SQLDelete(sql = "UPDATE p_user SET deleted_at = CURRENT_TIMESTAMP WHERE username = ?")
@Where(clause = "deleted_at IS NULL")
public class User {
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

    @Embedded
    private CreateAudit createAudit;

    @Embedded
    private UpdateAudit updateAudit;

    @Embedded
    private SoftDeleteAudit softDeleteAudit;

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