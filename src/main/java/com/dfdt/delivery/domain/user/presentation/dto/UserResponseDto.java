package com.dfdt.delivery.domain.user.presentation.dto;

import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserResponseDto {
    private String username;
    private String name;
    private UserRole role;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }
}
