package com.dfdt.delivery.domain.user.presentation.dto;

import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class UserRoleUpdateRequestDto {
    @NotBlank(message = "권한을 변경할 유저의 아이디를 입력해주세요.")
    private String username;

    @NotNull(message = "변경할 권한을 선택해주세요.")
    private UserRole newRole;
}
