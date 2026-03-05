package com.dfdt.delivery.domain.order.application.service.command;

import com.dfdt.delivery.domain.address.domain.entity.Address;
import com.dfdt.delivery.domain.order.application.converter.OrderConverter;
import com.dfdt.delivery.domain.order.application.provider.OrderDataFinder;
import com.dfdt.delivery.domain.order.application.service.checker.OrderPreConditionChecker;
import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.order.domain.entity.OrderItem;
import com.dfdt.delivery.domain.order.domain.enums.OrderStatus;
import com.dfdt.delivery.domain.order.domain.repository.OrderCacheManager;
import com.dfdt.delivery.domain.order.domain.repository.OrderRepository;
import com.dfdt.delivery.domain.order.presentation.dto.OrderReqDto;
import com.dfdt.delivery.domain.order.presentation.dto.OrderResDto;
import com.dfdt.delivery.domain.product.domain.entity.Product;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderCommandServiceImpl implements OrderCommandService {
    private final OrderCacheManager orderCacheManager;
    private final OrderRepository orderRepository;
    private final OrderPreConditionChecker orderPreConditionChecker;
    private final OrderDataFinder orderDataFinder;


    @Transactional
    @Override
    public OrderResDto.OrderMutationResponse createOrder(String username, OrderReqDto.Create createDTO) {
        // 정보 가져오기
        Store orderStore = orderDataFinder.findStore(createDTO.storeId());
        Address orderAddress = orderDataFinder.findAddress(createDTO.addressId());
        User orderUser = orderDataFinder.findUser(username);

        List<UUID> productIds = createDTO.orderItems().stream().map(OrderReqDto.OrderItem::productId).toList();
        Map<UUID, Product> stockMap = orderDataFinder.getProductMap(productIds);

        // 주문 생성
        Order order = OrderConverter.toOrder(orderUser,orderAddress,orderStore,createDTO.requestMemo());

        for (OrderReqDto.OrderItem productItem : createDTO.orderItems()) {
            Product stock = stockMap.get(productItem.productId());
            // 매장의 상품이 존재하는 지, 재고가 있는지 확인하는 함수
            orderPreConditionChecker.validateProduct(stock,orderStore,productItem);
            // 주문 아이템 생성
            OrderItem orderItem = OrderConverter.toOrderItem(stock, productItem.quantity());
            order.addOrderItem(orderItem);
        }
        // DB 저장
        Order savedOrder = orderRepository.save(order);
        // Redis TTL 설정하여 결제 대기 시간 제한
        orderCacheManager.setPaymentTimeout(savedOrder.getOrderId(), Duration.ofMinutes(5));
        // 응답 반환
        return OrderConverter.toMutationResponse(savedOrder);
    }

    @Transactional
    @Override
    public OrderResDto.OrderMutationResponse updateOrder(String username,UUID orderId, OrderReqDto.UpdateOrder updateOrderDTO) {
        // 정보 가져오기
        Order order  = orderDataFinder.findOrder(orderId);
        User user = orderDataFinder.findUser(username);

        // 권한 체크 ( 주문의 주인인지 )
        orderPreConditionChecker.authoriseOrderCustomer(order,user);
        orderPreConditionChecker.validateModifiable(order);

        if (updateOrderDTO.addressId()!=null)
        {
            Address orderAddress = orderDataFinder.findAddress(updateOrderDTO.addressId());
            order.updateAddress(orderAddress);
        }
        // 주문 상품 처리
        order.removeOrderItems(updateOrderDTO.removeOrderItemIds());
        if (updateOrderDTO.addOrderItems()!=null && !updateOrderDTO.addOrderItems().isEmpty()) {
            List<UUID> productIds = updateOrderDTO.addOrderItems().stream().map(OrderReqDto.OrderItem::productId).toList();
            Map<UUID, Product> stockMap = orderDataFinder.getProductMap(productIds);
            for (OrderReqDto.OrderItem productItem : updateOrderDTO.addOrderItems()) {
                Product stock = stockMap.get(productItem.productId());
                // 매장의 상품이 존재하는 지, 재고가 있는지 확인하는 함수
                orderPreConditionChecker.validateProduct(stock, order.getStore(), productItem);
                // 주문 아이템 생성
                OrderItem orderItem = OrderConverter.toOrderItem(stock, productItem.quantity());
                order.addOrderItem(orderItem);
            }
        }
        // 주문 상품 처리 후 상품 리스트에 상품이 하나도 안 남았다면 오류
        orderPreConditionChecker.validateQuantity(order);

        if (updateOrderDTO.requestMemo()!=null)
            order.updateOrderMessage(updateOrderDTO.requestMemo());
        Order savedOrder = orderRepository.save(order);
        return OrderConverter.toMutationResponse(savedOrder);
    }

    @Transactional
    @Override
    public Void deleteOrder(String username, UUID orderId) {
        // 정보 가져오기
        Order order = orderDataFinder.findOrder(orderId);
        User user = orderDataFinder.findUser(username);

        // 권한 체크
        orderPreConditionChecker.authoriseOrderCustomer(order,user);
        orderPreConditionChecker.validateDeletable(order);

        // 상태별 로직 분기
        if (order.getStatus() == OrderStatus.PENDING)
            order.updateStatus(OrderStatus.CANCELED,"주문 취소");
        else
            order.updateStatus(OrderStatus.HIDDEN,"주문 목록에서 제거");
        order.deleteOrder(user.getName());
        return null;
    }
}
