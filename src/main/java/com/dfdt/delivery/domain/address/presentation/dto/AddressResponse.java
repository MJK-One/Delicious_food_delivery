package com.dfdt.delivery.domain.address.presentation.dto;

import com.dfdt.delivery.domain.address.domain.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {
    private UUID addressId;
    private String username;
    private UUID regionId;
    private String addressName;
    private String addressLine1;
    private String addressLine2;
    private String receiverName;
    private String receiverPhone;
    private Boolean isDefault;
    private String deliveryMemo;

    public static AddressResponse from(Address address) {
        return AddressResponse.builder()
                .addressId(address.getAddressId())
                .username(address.getUser().getUsername())
                .regionId(address.getRegion().getRegionId())
                .addressName(address.getAddressName())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .isDefault(address.getIsDefault())
                .deliveryMemo(address.getDeliveryMemo())
                .build();
    }
}
