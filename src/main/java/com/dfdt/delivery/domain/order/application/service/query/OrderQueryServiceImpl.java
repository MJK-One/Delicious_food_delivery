package com.dfdt.delivery.domain.order.application.service.query;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.common.exception.error.enums.UserErrorCode;
import com.dfdt.delivery.domain.order.application.converter.OrderConverter;
import com.dfdt.delivery.domain.order.application.dto.TimeIdCursor;
import com.dfdt.delivery.domain.order.application.provider.OrderDataFinder;
import com.dfdt.delivery.domain.order.application.service.validator.OrderValidator;
import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.order.domain.entity.QOrder;
import com.dfdt.delivery.domain.order.domain.repository.OrderRepository;
import com.dfdt.delivery.domain.order.presentation.dto.OrderReqDto;
import com.dfdt.delivery.domain.order.presentation.dto.OrderResDto;
import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.repository.UserRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {
    private final OrderRepository orderRepository;
    private final OrderDataFinder orderDataFinder;
    private final UserRepository userRepository;
    private final OrderValidator orderValidator;
    private final QOrder order = QOrder.order;


    @Override
    public OrderResDto.CustomerOrderResponse getCustomerOrderHistory(String username, OrderReqDto.OrderSearchRequest orderSearchRequest) {

        BooleanBuilder builder = new BooleanBuilder();

        // 첫번째 조건 : 내 주문만 가져오기
        builder.and(order.user.username.eq(username));

        if (orderSearchRequest.statuses() != null && !orderSearchRequest.statuses().isEmpty())
            builder.and(order.status.in(orderSearchRequest.statuses()));
        if (orderSearchRequest.startDate() != null && orderSearchRequest.endDate() != null) {
            builder.and(
                    order.createdAudit.createdAt.between(
                            orderSearchRequest.startDate(), orderSearchRequest.endDate()));
        }

        if (orderSearchRequest.cursor() != null)
        {
            TimeIdCursor cursor = new TimeIdCursor(orderSearchRequest.cursor());
            builder.and(
                    order.createdAudit.createdAt.lt(cursor.getTime())
                            .or(order.createdAudit.createdAt.eq(cursor.getTime())
                                    .and(order.orderId.lt(cursor.getId())))
            );
        }

        int pageSize = orderSearchRequest.size();
        List<Order> orders = orderRepository.findAllByBuilder(pageSize,builder);
        log.info("사용자의 주문 목록 조회 완료");
        // 컨버터 변환
        return OrderConverter.toCustomerOrderResponse(orders,pageSize);
    }


    @Transactional
    @Override
    public OrderResDto.GetOrderDetailResponse getOrderDetail(String username, UUID orderId) {
        Order order = orderDataFinder.findOrder(orderId);
        User user = userRepository.findByUsername(username)
                .orElseThrow(()-> new BusinessException(UserErrorCode.INVALID_AUTH_REQUEST));

        orderValidator.authoriseOrder(order,user);
        return OrderConverter.toOrderDetailResponse(order);
    }

    @Override
    public OrderResDto.OwnerDashboardResponse getOwnerDashboard(String username, UUID storeId,OrderReqDto.OrderSearchRequest orderSearchRequest) {
        BooleanBuilder builder = new BooleanBuilder();

        // 첫번째 조건 : 가게 주문 가져오기
        builder.and(order.store.user.username.eq(username));
        builder.and(order.store.storeId.eq(storeId));

        if (orderSearchRequest.startDate() != null && orderSearchRequest.endDate() != null) {
            builder.and(
                    order.createdAudit.createdAt.between(
                            orderSearchRequest.startDate(), orderSearchRequest.endDate()));
        }

        OrderResDto.OrderSummaryCount summaryCounts = orderRepository.countOrdersByStatus(builder);

        if (orderSearchRequest.statuses() != null && !orderSearchRequest.statuses().isEmpty())
            builder.and(order.status.in(orderSearchRequest.statuses()));

        if (orderSearchRequest.cursor() != null)
        {
            TimeIdCursor cursor = new TimeIdCursor(orderSearchRequest.cursor());
            builder.and(
                    order.createdAudit.createdAt.lt(cursor.getTime())
                            .or(order.createdAudit.createdAt.eq(cursor.getTime())
                                    .and(order.orderId.lt(cursor.getId())))
            );
        }
        
        int pageSize = orderSearchRequest.size();
        List<Order> orders = orderRepository.findAllByBuilder(pageSize,builder);
        log.info("가게의 주문 목록 조회 완료");
        // 컨버터 변환
        return OrderConverter.toOwnerDashboardResponse(summaryCounts,pageSize,orders);
    }
}
