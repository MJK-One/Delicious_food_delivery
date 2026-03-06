package com.dfdt.delivery.domain.user.presentation.dto;

import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SignupRequestDto {

    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(min = 4, max = 10, message = "아이디는 4자 이상 10자 이하로 입력해주세요.")
    @Pattern(regexp = "^[a-z0-9]+$", message = "아이디는 알파벳 소문자(a~z)와 숫자(0~9)만 사용 가능합니다.")
    private String username;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 15, message = "비밀번호는 8자 이상 15자 이하로 입력해주세요.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", 
            message = "비밀번호는 알파벳 대소문자, 숫자, 특수문자를 최소 하나씩 포함해야 합니다.")
    private String password;

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    @NotNull(message = "권한을 선택해주세요.")
    private UserRole role;
}
