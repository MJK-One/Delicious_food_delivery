package com.dfdt.delivery.domain.product.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProductCreateReqDto {

    @NotBlank
    private String name;

    @Size(min = 0, max = 255)
    private String description;

    private Boolean isAiDescription = false;

    @NotNull
    @Min(0)
    @Max(1_000_000)
    private Integer price;

    @NotNull
    private Boolean isHidden;

    /**
     * (선택) AI 미리보기 로그 ID.
     * productName만으로 생성한 AI 미리보기를 상품 등록 시 바로 적용하려면 전달합니다.
     * 전달하면 해당 AI 로그의 responseText가 상품 설명으로 적용되고,
     * AiLog의 productId와 isApplied가 업데이트됩니다.
     */
    private UUID aiLogId;
}

