package com.dfdt.delivery.domain.store.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StoreStatusReqDto {

    @NotBlank(message = "승인 여부는 반드시 입력해야 합니다.")
    private String status;

    @Size(max = 500, message = "최대 500 이하로 작성해주세요.")
    private String message;
}
