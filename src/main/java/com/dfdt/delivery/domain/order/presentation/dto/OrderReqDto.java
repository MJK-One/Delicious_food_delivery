package com.dfdt.delivery.domain.order.presentation.dto;

import com.dfdt.delivery.domain.order.domain.enums.OrderStatus;
import com.dfdt.delivery.domain.payment.domain.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class OrderReqDto {
    public record Create(
            @NotNull(message = "가게 아이디가 입력되지 않았습니다.")
            UUID storeId,
            @NotNull(message = "주소가 입력되지 않았습니다.")
            UUID addressId,
            @NotEmpty(message = "하나 이상의 상품이 들어있어야 합니다.")
            List<OrderItem> orderItems,
            @Length(max = 255,message = "최대 255자 입니다.")
            String requestMemo,
            @NotNull
            PaymentMethod paymentMethod
    ){}
    public record OrderItem(
            @NotNull(message = "상품 아이디가 입력되지 않았습니다.")
            UUID productId,
            @NotNull(message = "상품 수량이 입력되지 않았습니다.")
            Integer quantity,
            @NotNull(message = "상품 가격이 입력되지 않았습니다.")
            Integer userViewedUnitPrice
    ){}
    public record UpdateOrder(
            UUID addressId,
            @Length(max = 255,message = "최대 255자 입니다.")
            String requestMemo
    ){}
    public record UpdateStatus(
            @NotNull(message = "상태가 입력되지 않았습니다.")
            OrderStatus orderStatus,
            String changedReason
    ){}
    public record OrderSearchRequest(
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            OffsetDateTime startDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            OffsetDateTime endDate,

            List<OrderStatus> statuses,

            String cursor,
            @Max(100)
            Integer size
    ){
        public OrderSearchRequest {
            if (size == null || size <= 0) size = 10;
        }
    }
}