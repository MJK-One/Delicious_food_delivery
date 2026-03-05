package com.dfdt.delivery.domain.user.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class UserUpdateRequestDto {
    @NotBlank(message = "이름을 입력해주세요.")
    private String name;
}
