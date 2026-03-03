package com.dfdt.delivery.domain.category.dto.request;

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

    @NotNull(message = "정렬 순서는 필수입니다.")
    private Integer sortOrder;

    private Boolean isActive = false;
}
