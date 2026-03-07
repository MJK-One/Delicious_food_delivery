package com.dfdt.delivery.domain.payment.application;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.UpdateAudit;
import com.dfdt.delivery.domain.address.domain.entity.Address;
import com.dfdt.delivery.domain.address.domain.repository.AddressRepository;
import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.order.domain.enums.OrderStatus;
import com.dfdt.delivery.domain.order.domain.repository.OrderRepository;
import com.dfdt.delivery.domain.payment.application.service.command.PaymentCommandService;
import com.dfdt.delivery.domain.payment.application.service.query.PaymentQueryService;
import com.dfdt.delivery.domain.payment.domain.entity.Payment;
import com.dfdt.delivery.domain.payment.domain.enums.PaymentErrorCode;
import com.dfdt.delivery.domain.payment.domain.enums.PaymentStatus;
import com.dfdt.delivery.domain.payment.domain.repository.PaymentRepository;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentApproveReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentCreateReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentHistorySearchReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.request.PaymentListSearchReqDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentDetailResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentHistoryResDto;
import com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentListItemResDto;
import com.dfdt.delivery.domain.region.domain.entity.Region;
import com.dfdt.delivery.domain.region.domain.repository.RegionRepository;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import com.dfdt.delivery.domain.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class PaymentIntegrationTest {

    @Autowired
    private PaymentCommandService paymentCommandService;

    @Autowired
    private PaymentQueryService paymentQueryService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private com.dfdt.delivery.domain.store.domain.repository.JpaStoreRepository storeRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    @DisplayName("결제 생성 통합 테스트")
    void createPaymentTest() {
        setupBaseData();
        Order savedOrder = createOrder();

        PaymentCreateReqDto reqDto = PaymentCreateReqDto.builder()
                .orderId(savedOrder.getOrderId())
                .amount(20000)
                .paymentMethod(com.dfdt.delivery.domain.payment.domain.enums.PaymentMethod.CARD)
                .build();

        PaymentDetailResDto result = paymentCommandService.createPayment(reqDto, "testuser");

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(savedOrder.getOrderId());
    }

    @Test
    @DisplayName("결제 승인 통합 테스트 - 성공 시나리오")
    void approvePaymentTest() {
        setupBaseData();
        Order savedOrder = createOrder();
        
        PaymentCreateReqDto createDto = PaymentCreateReqDto.builder()
                .orderId(savedOrder.getOrderId())
                .amount(20000)
                .paymentMethod(com.dfdt.delivery.domain.payment.domain.enums.PaymentMethod.CARD)
                .build();
        PaymentDetailResDto createdPayment = paymentCommandService.createPayment(createDto, "testuser");
        entityManager.flush();

        PaymentApproveReqDto approveDto = createApproveDto(PaymentStatus.PAID);

        PaymentDetailResDto result = paymentCommandService.approvePayment(createdPayment.getPaymentId(), approveDto, "testuser");

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PAID);
        
        Order updatedOrder = orderRepository.findById(savedOrder.getOrderId()).orElseThrow();
        assertThat(updatedOrder.getStatus().name()).isEqualTo("PAID"); 
    }

    @Test
    @DisplayName("결제 히스토리 목록 조회 테스트 - 상태 변경 이력 확인")
    void listPaymentHistoryTest() {
        // 1. 기초 데이터 및 결제 준비
        setupBaseData();
        Order savedOrder = createOrder();
        
        // 2. 결제 생성 (History 1: null -> READY)
        PaymentCreateReqDto createDto = PaymentCreateReqDto.builder()
                .orderId(savedOrder.getOrderId())
                .amount(20000)
                .paymentMethod(com.dfdt.delivery.domain.payment.domain.enums.PaymentMethod.CARD)
                .build();
        PaymentDetailResDto createdPayment = paymentCommandService.createPayment(createDto, testUser.getUsername());
        entityManager.flush();

        // 3. 결제 승인 (History 2: READY -> PAID)
        paymentCommandService.approvePayment(createdPayment.getPaymentId(), createApproveDto(PaymentStatus.PAID), testUser.getUsername());
        entityManager.flush();

        // 4. 결제 취소 (History 3: PAID -> CANCELED)
        paymentCommandService.cancelPayment(createdPayment.getPaymentId(), testUser.getUsername());
        entityManager.flush();
        entityManager.clear();

        // 5. 히스토리 목록 조회 요청 (특정 paymentId로 필터링)
        PaymentHistorySearchReqDto searchDto = new PaymentHistorySearchReqDto();
        searchDto.setPaymentId(createdPayment.getPaymentId());
        
        Page<PaymentHistoryResDto> result = paymentQueryService.listPaymentHistory(
                searchDto, 
                PageRequest.of(0, 10)
        );

        // 6. 검증
        assertThat(result).isNotNull();
        // 최소 3건의 이력이 있어야 함 (생성, 승인, 취소)
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(3);
        
        // 상태 변경 흐름 확인
        boolean hasReady = result.getContent().stream().anyMatch(h -> h.getToStatus() == PaymentStatus.READY);
        boolean hasPaid = result.getContent().stream().anyMatch(h -> h.getToStatus() == PaymentStatus.PAID);
        boolean hasCanceled = result.getContent().stream().anyMatch(h -> h.getToStatus() == PaymentStatus.CANCELED);
        
        assertThat(hasReady).isTrue();
        assertThat(hasPaid).isTrue();
        assertThat(hasCanceled).isTrue();
    }

    @Test
    @DisplayName("결제 목록 조회 테스트 - 사용자 본인의 다건 조회 시나리오")
    void listPaymentsTest() {
        setupBaseData();
        for (int i = 0; i < 3; i++) {
            Order order = createOrder();
            PaymentCreateReqDto createDto = PaymentCreateReqDto.builder()
                    .orderId(order.getOrderId())
                    .amount(10000 + (i * 1000))
                    .paymentMethod(com.dfdt.delivery.domain.payment.domain.enums.PaymentMethod.CARD)
                    .build();
            paymentCommandService.createPayment(createDto, testUser.getUsername());
        }
        entityManager.flush();
        entityManager.clear();

        PaymentListSearchReqDto searchDto = new PaymentListSearchReqDto();
        Page<PaymentListItemResDto> result = paymentQueryService.listPayments(
                searchDto, 
                PageRequest.of(0, 10), 
                testUser.getUsername(), 
                UserRole.CUSTOMER
        );

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("결제 단건 조회 테스트 - 소유자 조회 성공 시나리오")
    void getPaymentTest() {
        setupBaseData();
        Order savedOrder = createOrder();
        PaymentCreateReqDto createDto = PaymentCreateReqDto.builder()
                .orderId(savedOrder.getOrderId())
                .amount(20000)
                .paymentMethod(com.dfdt.delivery.domain.payment.domain.enums.PaymentMethod.CARD)
                .build();
        PaymentDetailResDto createdPayment = paymentCommandService.createPayment(createDto, testUser.getUsername());
        entityManager.flush();

        PaymentDetailResDto result = paymentQueryService.getPayment(createdPayment.getPaymentId(), testUser.getUsername(), UserRole.CUSTOMER);

        assertThat(result).isNotNull();
        assertThat(result.getPaymentId()).isEqualTo(createdPayment.getPaymentId());
        assertThat(result.getOrderId()).isEqualTo(savedOrder.getOrderId());
        assertThat(result.getAmount()).isEqualTo(20000L);
    }

    @Test
    @DisplayName("결제 단건 조회 실패 테스트 - 타인의 결제 내역 조회 시도")
    void getPayment_Fail_AccessDenied() {
        setupBaseData();
        Order savedOrder = createOrder();
        PaymentCreateReqDto createDto = PaymentCreateReqDto.builder()
                .orderId(savedOrder.getOrderId())
                .amount(20000)
                .paymentMethod(com.dfdt.delivery.domain.payment.domain.enums.PaymentMethod.CARD)
                .build();
        PaymentDetailResDto createdPayment = paymentCommandService.createPayment(createDto, testUser.getUsername());
        entityManager.flush();

        assertThatThrownBy(() -> paymentQueryService.getPayment(createdPayment.getPaymentId(), "another_user", UserRole.CUSTOMER))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(PaymentErrorCode.PAYMENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("결제 취소 통합 테스트 - 성공 시나리오")
    void cancelPaymentTest() {
        setupBaseData();
        Order savedOrder = createOrder();
        
        PaymentCreateReqDto createDto = PaymentCreateReqDto.builder()
                .orderId(savedOrder.getOrderId())
                .amount(20000)
                .paymentMethod(com.dfdt.delivery.domain.payment.domain.enums.PaymentMethod.CARD)
                .build();
        PaymentDetailResDto createdPayment = paymentCommandService.createPayment(createDto, "testuser");
        entityManager.flush();

        paymentCommandService.approvePayment(createdPayment.getPaymentId(), createApproveDto(PaymentStatus.PAID), "testuser");
        entityManager.flush();

        PaymentDetailResDto result = paymentCommandService.cancelPayment(createdPayment.getPaymentId(), "testuser");

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.CANCELED);
        
        Order updatedOrder = orderRepository.findById(savedOrder.getOrderId()).orElseThrow();
        assertThat(updatedOrder.getStatus().name()).isEqualTo("CANCELED");
    }

    @Test
    @DisplayName("결제 승인 실패 테스트 - 이미 완료된 결제를 재승인 시도")
    void approvePayment_Fail_AlreadyPaid() {
        setupBaseData();
        Order savedOrder = createOrder();
        PaymentCreateReqDto createDto = PaymentCreateReqDto.builder()
                .orderId(savedOrder.getOrderId())
                .amount(20000)
                .paymentMethod(com.dfdt.delivery.domain.payment.domain.enums.PaymentMethod.CARD)
                .build();
        PaymentDetailResDto createdPayment = paymentCommandService.createPayment(createDto, "testuser");

        paymentCommandService.approvePayment(createdPayment.getPaymentId(), createApproveDto(PaymentStatus.PAID), "testuser");

        assertThatThrownBy(() -> paymentCommandService.approvePayment(createdPayment.getPaymentId(), createApproveDto(PaymentStatus.PAID), "testuser"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(PaymentErrorCode.INVALID_PAYMENT_STATUS.getMessage());
    }

    @Test
    @DisplayName("결제 취소 실패 테스트 - 이미 조리 시작(ACCEPTED)된 주문 취소 시도")
    void cancelPayment_Fail_InvalidOrderStatus() {
        setupBaseData();
        Order savedOrder = createOrder();
        
        savedOrder.updateStatus(OrderStatus.ACCEPTED, "가게에서 주문을 수락함");
        entityManager.flush();

        PaymentCreateReqDto createDto = PaymentCreateReqDto.builder()
                .orderId(savedOrder.getOrderId())
                .amount(20000)
                .paymentMethod(com.dfdt.delivery.domain.payment.domain.enums.PaymentMethod.CARD)
                .build();
        PaymentDetailResDto createdPayment = paymentCommandService.createPayment(createDto, "testuser");

        assertThatThrownBy(() -> paymentCommandService.cancelPayment(createdPayment.getPaymentId(), "testuser"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(PaymentErrorCode.INVALID_PAYMENT_STATUS.getMessage());
    }

    @Test
    @DisplayName("결제 취소 실패 테스트 - 이미 취소된 결제를 재취소 시도")
    void cancelPayment_Fail_AlreadyCanceled() {
        setupBaseData();
        Order savedOrder = createOrder();
        PaymentCreateReqDto createDto = PaymentCreateReqDto.builder()
                .orderId(savedOrder.getOrderId())
                .amount(20000)
                .paymentMethod(com.dfdt.delivery.domain.payment.domain.enums.PaymentMethod.CARD)
                .build();
        PaymentDetailResDto createdPayment = paymentCommandService.createPayment(createDto, "testuser");

        paymentCommandService.cancelPayment(createdPayment.getPaymentId(), "testuser");

        assertThatThrownBy(() -> paymentCommandService.cancelPayment(createdPayment.getPaymentId(), "testuser"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(PaymentErrorCode.INVALID_PAYMENT_STATUS.getMessage());
    }

    @Test
    @DisplayName("결제 내역 삭제 테스트 - Soft Delete 확인")
    void deletePaymentTest() {
        setupBaseData();
        Order savedOrder = createOrder();
        PaymentCreateReqDto createDto = PaymentCreateReqDto.builder()
                .orderId(savedOrder.getOrderId())
                .amount(20000)
                .paymentMethod(com.dfdt.delivery.domain.payment.domain.enums.PaymentMethod.CARD)
                .build();
        PaymentDetailResDto createdPayment = paymentCommandService.createPayment(createDto, "testuser");
        entityManager.flush();

        paymentCommandService.deletePayment(createdPayment.getPaymentId(), "admin");
        entityManager.flush();
        entityManager.clear();

        Payment payment = paymentRepository.findById(createdPayment.getPaymentId()).orElse(null);
        assertThat(payment).isNotNull();
        assertThat(payment.getSoftDeleteAudit().getDeletedAt()).isNotNull();
        assertThat(payment.getSoftDeleteAudit().getDeletedBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("결제 내역 숨김/해제 테스트")
    void toggleHiddenTest() {
        setupBaseData();
        Order savedOrder = createOrder();
        PaymentCreateReqDto createDto = PaymentCreateReqDto.builder()
                .orderId(savedOrder.getOrderId())
                .amount(20000)
                .paymentMethod(com.dfdt.delivery.domain.payment.domain.enums.PaymentMethod.CARD)
                .build();
        PaymentDetailResDto createdPayment = paymentCommandService.createPayment(createDto, testUser.getUsername());
        entityManager.flush();

        com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentHiddenToggleResDto hideResult = 
                paymentCommandService.toggleHidden(createdPayment.getPaymentId(), true, testUser.getUsername());
        
        assertThat(hideResult.getHiddenAt()).isNotNull();

        com.dfdt.delivery.domain.payment.presentation.dto.response.PaymentHiddenToggleResDto unhideResult = 
                paymentCommandService.toggleHidden(createdPayment.getPaymentId(), false, testUser.getUsername());
        
        assertThat(unhideResult.getHiddenAt()).isNull();
    }

    private User testUser;
    private Region testRegion;
    private Address testAddress;
    private Store testStore;

    private void setupBaseData() {
        testRegion = Region.builder()
                .name("서울특별시")
                .level((short) 1)
                .code("SEOUL-" + UUID.randomUUID().toString().substring(0,8))
                .isOrderEnabled(true)
                .createAudit(CreateAudit.now("SYSTEM"))
                .softDeleteAudit(SoftDeleteAudit.active())
                .build();
        regionRepository.saveAndFlush(testRegion);

        testUser = User.builder()
                .username("user-" + UUID.randomUUID().toString().substring(0,5))
                .password("password")
                .name("테스터")
                .role(UserRole.CUSTOMER)
                .build();
        userRepository.save(testUser);
        entityManager.flush();

        testAddress = new Address();
        try {
            java.lang.reflect.Field line1Field = Address.class.getDeclaredField("addressLine1");
            line1Field.setAccessible(true);
            line1Field.set(testAddress, "강남구");
            java.lang.reflect.Field isDefaultField = Address.class.getDeclaredField("isDefault");
            isDefaultField.setAccessible(true);
            isDefaultField.set(testAddress, true);
            java.lang.reflect.Field createAuditField = Address.class.getDeclaredField("createAudit");
            createAuditField.setAccessible(true);
            createAuditField.set(testAddress, CreateAudit.now("SYSTEM"));
            java.lang.reflect.Field softDeleteAuditField = Address.class.getDeclaredField("softDeleteAudit");
            softDeleteAuditField.setAccessible(true);
            softDeleteAuditField.set(testAddress, SoftDeleteAudit.active());
        } catch (Exception e) {}
        addressRepository.saveAndFlush(testAddress);

        testStore = Store.builder()
                .name("치킨집")
                .user(testUser)
                .region(testRegion)
                .isOpen(true)
                .status(com.dfdt.delivery.domain.store.domain.enums.StoreStatus.APPROVED)
                .createAudit(CreateAudit.now("SYSTEM"))
                .softDeleteAudit(SoftDeleteAudit.active())
                .build();
        storeRepository.saveAndFlush(testStore);
    }

    private Order createOrder() {
        Order order = Order.builder()
                .user(testUser)
                .store(testStore)
                .address(testAddress)
                .deliveryAddressSnapshot("서울특별시 강남구 테헤란로")
                .totalPrice(20000)
                .totalQuantity(1)
                .createdAudit(CreateAudit.now(testUser.getUsername()))
                .softDeleteAudit(SoftDeleteAudit.active())
                .build();
        Order saved = orderRepository.save(order);
        entityManager.flush();
        return saved;
    }

    private PaymentApproveReqDto createApproveDto(PaymentStatus status) {
        PaymentApproveReqDto dto = new PaymentApproveReqDto();
        try {
            java.lang.reflect.Field resultField = PaymentApproveReqDto.class.getDeclaredField("result");
            resultField.setAccessible(true);
            resultField.set(dto, status);

            java.lang.reflect.Field providerField = PaymentApproveReqDto.class.getDeclaredField("pgProvider");
            providerField.setAccessible(true);
            providerField.set(dto, "TOSS");

            java.lang.reflect.Field tidField = PaymentApproveReqDto.class.getDeclaredField("pgTransactionId");
            tidField.setAccessible(true);
            tidField.set(dto, "TID-12345");
        } catch (Exception e) {}
        return dto;
    }
}
