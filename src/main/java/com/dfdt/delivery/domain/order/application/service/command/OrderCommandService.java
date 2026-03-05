package com.dfdt.delivery.domain.order.application.service.command;

import com.dfdt.delivery.domain.order.presentation.dto.*;
import jakarta.validation.Valid;

import java.util.UUID;

public interface OrderCommandService {
    OrderResDto.OrderMutationResponse createOrder(String username, OrderReqDto.Create createDTO);
    OrderResDto.OrderMutationResponse updateOrder(String username, UUID orderId, OrderReqDto.UpdateOrder updateOrderDTO);
    Void deleteOrder(String username, UUID orderId);
    OrderResDto.OrderMutationResponse updateOrderStatus(String username, UUID orderId, OrderReqDto.@Valid UpdateStatus updateStatusDTO);
}
