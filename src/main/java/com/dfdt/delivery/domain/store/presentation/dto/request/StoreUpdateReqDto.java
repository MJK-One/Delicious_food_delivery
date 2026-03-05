package com.dfdt.delivery.domain.store.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreUpdateReqDto {

    @NotBlank(message = "가게명은 필수입니다.")
    @Size(min = 1, max = 100, message = "가게명은 1 ~ 100자 사이로 작성해주세요.")
    private String name;

    @Pattern(
            regexp = "^(01[016789]|0\\d{1,2})-?\\d{3,4}-?\\d{4}$",
            message = "올바른 전화번호 형식이 아닙니다."
    )
    private String phone;

    @Size(max = 200, message = "가게 소개는 200자 이하로 작성해주세요.")
    private String description;

    @Size(max = 255, message = "가게 주소는 255자 이하로 작성해주세요.")
    private String addressText;

    @NotNull
    private Boolean isOpen = true;

    @NotEmpty(message = "카테고리는 최소 1개 이상 선택해야 합니다.")
    private List<UUID> categoryIds;
}
