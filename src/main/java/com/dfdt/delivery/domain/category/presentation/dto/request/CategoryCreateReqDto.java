package com.dfdt.delivery.domain.category.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoryCreateReqDto {

    @NotBlank(message = "카테고리명은 필수입니다.")
    @Size(max = 50, message = "카테고리명은 최대 50자 이하로 작성해주세요.")
    private String name;

    @Size(max = 255, message = "카테고리 설명은 최대 255자 이하로 작성해주세요.")
    private String description;

    private Boolean isActive = false;
}
