package com.dfdt.delivery.domain.auth.infrastructure.security;

import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security에서 인증된 사용자 정보를 보관하기 위한 구현체.
 * 시스템의 User 엔티티 정보를 Spring Security의 권한 규격(UserDetails)으로 변환하는 역할.
 */
@Getter
public class CustomUserDetails implements UserDetails {
    private final String username;
    private final String password;
    private final String name;
    private final UserRole role;

    public CustomUserDetails(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.name = user.getName();
        this.role = user.getRole();
    }

    /**
     * 사용자가 가진 권한 목록을 반환합니다.
     * UserRole Enum 값을 "ROLE_CUSTOMER" 등과 같이 Spring Security 권한 규격 문자열로 변환.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    /**
     * 계정 만료 여부를 반환. (true: 만료 안됨)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정 잠김 여부를 반환. (true: 잠기지 않음)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 자격 증명(비밀번호) 만료 여부를 반환. (true: 만료 안됨)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정 활성화 여부를 반환. (true: 활성)
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
