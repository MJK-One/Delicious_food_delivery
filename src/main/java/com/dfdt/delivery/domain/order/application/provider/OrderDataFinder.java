package com.dfdt.delivery.domain.order.application.provider;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.address.domain.entity.Address;
import com.dfdt.delivery.domain.address.domain.repository.AddressRepository;
import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.order.domain.enums.OrderErrorCode;
import com.dfdt.delivery.domain.order.domain.repository.OrderRepository;
import com.dfdt.delivery.domain.product.domain.entity.Product;
import com.dfdt.delivery.domain.product.domain.repository.ProductRepository;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.enums.StoreErrorCode;
import com.dfdt.delivery.domain.store.domain.repository.StoreRepository;
import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.exception.error.enums.UserErrorCode;
import com.dfdt.delivery.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderDataFinder {
    private final StoreRepository storeRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;


    public Order findOrder(UUID orderId) {
        return orderRepository.findByIdWithLock(orderId)
                .orElseThrow(()-> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
    }

    public Store findStore(UUID storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(StoreErrorCode.NOT_FOUND_STORE));
    }

    public User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    public Address findAddress(UUID addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new NoSuchElementException("주소를 찾을 수 없습니다."));
    }

    public Map<UUID, Product> getProductMap(List<UUID> productIds) {
        return productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p));
    }
}