package com.dfdt.delivery.domain.order.presentation.dto;

import com.dfdt.delivery.domain.order.domain.enums.OrderStatus;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.util.List;
import java.util.UUID;

public class OrderReqDto {
    public record Create(
            @NotNull(message = "가게 아이디가 입력되지 않았습니다.")
            UUID storeId,
            @NotNull(message = "주소가 입력되지 않았습니다.")
            UUID addressId,
            @NotEmpty(message = "하나 이상의 상품이 들어있어야 합니다.")
            List<OrderItem> productItemList,
            @Length(max = 255,message = "최대 255자 입니다.")
            String requestMemo
    ){}
    public record OrderItem(
            @NotNull(message = "상품 아이디가 입력되지 않았습니다.")
            UUID productId,
            @NotNull(message = "상품 수량이 입력되지 않았습니다.")
            Integer quantity,
            @NotNull(message = "상품 가격이 입력되지 않았습니다.")
            Long userViewedUnitPrice
    ){}
    public record UpdateOrder(
            UUID addressId,
            List<OrderItem> orderItems,
            @Length(max = 255,message = "최대 255자 입니다.")
            String requestMemo
    ){}
    public record UpdateStatus(
            @NotNull(message = "상태가 입력되지 않았습니다.")
            OrderStatus orderStatus,
            String changedReason
    ){}
}