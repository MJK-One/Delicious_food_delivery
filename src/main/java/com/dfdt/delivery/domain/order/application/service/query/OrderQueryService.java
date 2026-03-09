package com.dfdt.delivery.domain.order.application.service.query;

import com.dfdt.delivery.domain.order.presentation.dto.OrderReqDto;
import com.dfdt.delivery.domain.order.presentation.dto.OrderResDto;

import java.util.UUID;

public interface OrderQueryService {
    OrderResDto.CustomerOrderResponse getCustomerOrderHistory(String username, OrderReqDto.OrderSearchRequest orderSearchRequest);
    OrderResDto.GetOrderDetailResponse getOrderDetail(String username, UUID orderId);
    OrderResDto.OwnerDashboardResponse getOwnerDashboard(String username, UUID storeId,OrderReqDto.OrderSearchRequest orderSearchRequest);
}
