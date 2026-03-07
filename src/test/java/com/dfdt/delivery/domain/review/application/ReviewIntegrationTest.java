package com.dfdt.delivery.domain.review.application;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.domain.address.domain.entity.Address;
import com.dfdt.delivery.domain.address.domain.repository.AddressRepository;
import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.order.domain.enums.OrderStatus;
import com.dfdt.delivery.domain.order.domain.repository.OrderRepository;
import com.dfdt.delivery.domain.review.application.service.command.ReviewCommandService;
import com.dfdt.delivery.domain.review.application.service.query.ReviewQueryService;
import com.dfdt.delivery.domain.review.domain.entity.Review;
import com.dfdt.delivery.domain.review.domain.enums.ReviewErrorCode;
import com.dfdt.delivery.domain.review.domain.repository.ReviewRepository;
import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewCreateReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewUpdateReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.response.ReviewResDto;
import com.dfdt.delivery.domain.region.domain.entity.Region;
import com.dfdt.delivery.domain.region.domain.repository.RegionRepository;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.entity.StoreRating;
import com.dfdt.delivery.domain.store.domain.repository.StoreRepository;
import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import com.dfdt.delivery.domain.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class ReviewIntegrationTest {

    @Autowired
    private ReviewCommandService reviewCommandService;

    @Autowired
    private ReviewQueryService reviewQueryService;

    @Autowired
    private ReviewRepository reviewRepository;

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

    private User testUser;
    private Region testRegion;
    private Address testAddress;
    private Store testStore;

    @Test
    @DisplayName("리뷰 작성 테스트 - 성공 시나리오")
    void createReviewTest() {
        // 1. 데이터 준비 (완료된 주문 필요)
        setupBaseData();
        Order order = createOrder();
        order.updateStatus(OrderStatus.COMPLETED, "배달 완료");
        entityManager.flush();

        // 2. 리뷰 작성 요청
        ReviewCreateReqDto reqDto = createReviewCreateDto(order.getOrderId(), testStore.getStoreId(), 5, "정말 맛있어요!");
        ReviewResDto result = reviewCommandService.createReview(testUser.getUsername(), reqDto);

        // 3. 검증
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("정말 맛있어요!");
        assertThat(result.getRating()).isEqualTo(5);
        
        // DB 저장 확인
        Review savedReview = reviewRepository.findById(result.getReviewId()).orElseThrow();
        assertThat(savedReview.getContent()).isEqualTo("정말 맛있어요!");
    }

    @Test
    @DisplayName("리뷰 작성 실패 테스트 - 이미 작성된 주문")
    void createReview_Fail_AlreadyReviewed() {
        setupBaseData();
        Order order = createOrder();
        order.updateStatus(OrderStatus.COMPLETED, "배달 완료");
        entityManager.flush();

        // 첫 번째 리뷰 작성
        ReviewCreateReqDto reqDto = createReviewCreateDto(order.getOrderId(), testStore.getStoreId(), 5, "첫 번째 리뷰");
        reviewCommandService.createReview(testUser.getUsername(), reqDto);
        entityManager.flush();

        // 두 번째 리뷰 작성 시도 (예외 발생해야 함)
        assertThatThrownBy(() -> reviewCommandService.createReview(testUser.getUsername(), reqDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ReviewErrorCode.ALREADY_REVIEWED.getMessage());
    }

    @Test
    @DisplayName("리뷰 수정 테스트 - 성공 시나리오")
    void updateReviewTest() {
        // 1. 리뷰 미리 작성
        setupBaseData();
        Order order = createOrder();
        order.updateStatus(OrderStatus.COMPLETED, "배달 완료");
        entityManager.flush();
        ReviewCreateReqDto createDto = createReviewCreateDto(order.getOrderId(), testStore.getStoreId(), 5, "원래 내용");
        ReviewResDto created = reviewCommandService.createReview(testUser.getUsername(), createDto);
        entityManager.flush();

        // 2. 리뷰 수정 요청
        ReviewUpdateReqDto updateDto = new ReviewUpdateReqDto();
        try {
            java.lang.reflect.Field ratingField = ReviewUpdateReqDto.class.getDeclaredField("rating");
            ratingField.setAccessible(true);
            ratingField.set(updateDto, 1);
            java.lang.reflect.Field contentField = ReviewUpdateReqDto.class.getDeclaredField("content");
            contentField.setAccessible(true);
            contentField.set(updateDto, "수정된 내용 (맛없어요)");
        } catch (Exception e) {}

        ReviewResDto result = reviewCommandService.updateReview(created.getReviewId(), testUser.getUsername(), updateDto);

        // 3. 검증
        assertThat(result.getRating()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo("수정된 내용 (맛없어요)");
    }

    @Test
    @DisplayName("리뷰 삭제 테스트 - Soft Delete 확인")
    void deleteReviewTest() {
        // 1. 리뷰 미리 작성
        setupBaseData();
        Order order = createOrder();
        order.updateStatus(OrderStatus.COMPLETED, "배달 완료");
        entityManager.flush();
        ReviewCreateReqDto createDto = createReviewCreateDto(order.getOrderId(), testStore.getStoreId(), 5, "삭제될 리뷰");
        ReviewResDto created = reviewCommandService.createReview(testUser.getUsername(), createDto);
        entityManager.flush();

        // 2. 리뷰 삭제 호출
        reviewCommandService.deleteReview(created.getReviewId(), testUser.getUsername(), UserRole.CUSTOMER);
        entityManager.flush();
        entityManager.clear();

        // 3. 검증
        Review deletedReview = reviewRepository.findById(created.getReviewId()).orElseThrow();
        assertThat(deletedReview.isDeleted()).isTrue();
    }

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
                .name("리뷰어")
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
                .name("맛있는집")
                .user(testUser)
                .region(testRegion)
                .isOpen(true)
                .status(com.dfdt.delivery.domain.store.domain.enums.StoreStatus.APPROVED)
                .createAudit(CreateAudit.now("SYSTEM"))
                .softDeleteAudit(SoftDeleteAudit.active())
                .build();
        
        // StoreRating 초기화 필요 여부 확인 (서비스에서 처리하지만 테스트 데이터용)
        Store savedStore = storeRepository.saveAndFlush(testStore);
        
        // StoreRating이 1:1 매핑일 경우 초기화
        try {
            java.lang.reflect.Constructor<StoreRating> constructor = StoreRating.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            StoreRating rating = constructor.newInstance();
            
            java.lang.reflect.Field ratingSumField = StoreRating.class.getDeclaredField("ratingSum");
            ratingSumField.setAccessible(true);
            ratingSumField.set(rating, 0);

            java.lang.reflect.Field ratingCountField = StoreRating.class.getDeclaredField("ratingCount");
            ratingCountField.setAccessible(true);
            ratingCountField.set(rating, 0);

            java.lang.reflect.Field ratingAvgField = StoreRating.class.getDeclaredField("ratingAvg");
            ratingAvgField.setAccessible(true);
            ratingAvgField.set(rating, java.math.BigDecimal.ZERO);

            java.lang.reflect.Field storeRefField = StoreRating.class.getDeclaredField("store");
            storeRefField.setAccessible(true);
            storeRefField.set(rating, savedStore);

            // Audit 필드 추가 (필수일 가능성이 높음)
            java.lang.reflect.Field createAuditField = StoreRating.class.getDeclaredField("createAudit");
            createAuditField.setAccessible(true);
            createAuditField.set(rating, CreateAudit.now("SYSTEM"));

            java.lang.reflect.Field softDeleteAuditField = StoreRating.class.getDeclaredField("softDeleteAudit");
            softDeleteAuditField.setAccessible(true);
            softDeleteAuditField.set(rating, SoftDeleteAudit.active());

            entityManager.persist(rating);
            
            java.lang.reflect.Field ratingField = Store.class.getDeclaredField("storeRating");
            ratingField.setAccessible(true);
            ratingField.set(savedStore, rating);
        } catch (Exception e) {
            e.printStackTrace();
        }
        entityManager.flush();
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

    private ReviewCreateReqDto createReviewCreateDto(UUID orderId, UUID storeId, int rating, String content) {
        ReviewCreateReqDto dto = new ReviewCreateReqDto();
        try {
            java.lang.reflect.Field orderIdField = ReviewCreateReqDto.class.getDeclaredField("orderId");
            orderIdField.setAccessible(true);
            orderIdField.set(dto, orderId);

            java.lang.reflect.Field storeIdField = ReviewCreateReqDto.class.getDeclaredField("storeId");
            storeIdField.setAccessible(true);
            storeIdField.set(dto, storeId);

            java.lang.reflect.Field ratingField = ReviewCreateReqDto.class.getDeclaredField("rating");
            ratingField.setAccessible(true);
            ratingField.set(dto, rating);

            java.lang.reflect.Field contentField = ReviewCreateReqDto.class.getDeclaredField("content");
            contentField.setAccessible(true);
            contentField.set(dto, content);
        } catch (Exception e) {}
        return dto;
    }
}
