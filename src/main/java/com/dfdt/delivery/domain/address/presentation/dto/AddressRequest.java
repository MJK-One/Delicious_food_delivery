package com.dfdt.delivery.domain.address.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 배송지 관련 요청 DTO.
 * 등록(Create) 및 수정(Update) 시 사용되는 데이터를 정의합니다.
 */
public class AddressRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Create {
        
        @NotNull(message = "지역 정보(regionId)는 필수입니다.")
        private UUID regionId;

        @Size(max = 50, message = "배송지 이름은 50자 이내여야 합니다.")
        private String addressName;

        @NotBlank(message = "기본 주소(addressLine1)는 필수입니다.")
        @Size(max = 255)
        private String addressLine1;

        @Size(max = 255)
        private String addressLine2;

        @Size(max = 50)
        private String receiverName;

        @Size(max = 20)
        private String receiverPhone;

        private Boolean isDefault;

        @Size(max = 255)
        private String deliveryMemo;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Update {
        
        private UUID regionId;

        @Size(max = 50)
        private String addressName;

        @Size(max = 255)
        private String addressLine1;

        @Size(max = 255)
        private String addressLine2;

        @Size(max = 50)
        private String receiverName;

        @Size(max = 20)
        private String receiverPhone;

        private Boolean isDefault;

        @Size(max = 255)
        private String deliveryMemo;
    }
}
