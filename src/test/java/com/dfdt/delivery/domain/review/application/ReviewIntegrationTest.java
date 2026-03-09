package com.dfdt.delivery.domain.review.application;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.domain.address.domain.entity.Address;
import com.dfdt.delivery.domain.address.domain.repository.AddressRepository;
import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.order.domain.entity.OrderItem;
import com.dfdt.delivery.domain.order.domain.enums.OrderStatus;
import com.dfdt.delivery.domain.order.infrastructure.persistence.repository.JpaOrderRepository;
import com.dfdt.delivery.domain.review.application.service.command.ReviewCommandService;
import com.dfdt.delivery.domain.review.application.service.query.ReviewQueryService;
import com.dfdt.delivery.domain.review.domain.entity.Review;
import com.dfdt.delivery.domain.review.domain.repository.ReviewRepository;
import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewCreateReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewUpdateReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.response.ReviewResDto;
import com.dfdt.delivery.domain.region.domain.entity.Region;
import com.dfdt.delivery.domain.region.domain.repository.RegionRepository;
import com.dfdt.delivery.domain.store.domain.entity.Store;
import com.dfdt.delivery.domain.store.domain.entity.StoreRating;
import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import com.dfdt.delivery.domain.user.infrastructure.persistence.repository.JpaUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewSearchReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.StoreReviewSearchReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.response.ReviewListResDto;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ReviewIntegrationTest {

    @Autowired
    private ReviewCommandService reviewCommandService;

    @Autowired
    private ReviewQueryService reviewQueryService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private JpaOrderRepository orderRepository;

    @Autowired
    private JpaUserRepository userRepository;

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
        // 1. 기초 데이터 준비 (완료된 주문 필요)
        setupBaseData();
        Order order = createOrder();
        order.updateStatus(OrderStatus.COMPLETED, "배달 완료");
        orderRepository.saveAndFlush(order);

        // 2. 리뷰 작성 요청
        ReviewCreateReqDto reqDto = createReviewCreateDto(order.getOrderId(), testStore.getStoreId(), 5, "정말 맛있어요!");
        ReviewResDto result = reviewCommandService.createReview(testUser.getUsername(), reqDto);

        // 3. 검증
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("정말 맛있어요!");
    }

    @Test
    @DisplayName("리뷰 작성 실패 테스트 - 이미 작성된 주문")
    void createReview_Fail_AlreadyReviewed() {
        // 1. 기초 데이터 및 첫 번째 리뷰 작성
        setupBaseData();
        Order order = createOrder();
        order.updateStatus(OrderStatus.COMPLETED, "배달 완료");
        orderRepository.saveAndFlush(order);

        ReviewCreateReqDto reqDto = createReviewCreateDto(order.getOrderId(), testStore.getStoreId(), 5, "첫 번째 리뷰");
        reviewCommandService.createReview(testUser.getUsername(), reqDto);
        entityManager.flush();

        // 2. 동일 주문에 대해 두 번째 리뷰 작성 시도 (실패 검증)
        assertThatThrownBy(() -> reviewCommandService.createReview(testUser.getUsername(), reqDto))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("리뷰 수정 테스트 - 성공 시나리오")
    void updateReviewTest() {
        // 1. 기존 리뷰 작성 완료 상태 준비
        setupBaseData();
        Order order = createOrder();
        order.updateStatus(OrderStatus.COMPLETED, "배달 완료");
        orderRepository.saveAndFlush(order);
        
        ReviewCreateReqDto createDto = createReviewCreateDto(order.getOrderId(), testStore.getStoreId(), 5, "원래 내용");
        ReviewResDto created = reviewCommandService.createReview(testUser.getUsername(), createDto);
        entityManager.flush();

        // 2. 리뷰 수정 DTO 준비
        ReviewUpdateReqDto updateDto = new ReviewUpdateReqDto();
        try {
            java.lang.reflect.Field ratingField = ReviewUpdateReqDto.class.getDeclaredField("rating");
            ratingField.setAccessible(true);
            ratingField.set(updateDto, 1);
            java.lang.reflect.Field contentField = ReviewUpdateReqDto.class.getDeclaredField("content");
            contentField.setAccessible(true);
            contentField.set(updateDto, "수정된 내용");
        } catch (Exception e) {}

        // 3. 리뷰 수정 요청 및 검증
        ReviewResDto result = reviewCommandService.updateReview(created.getReviewId(), testUser.getUsername(), updateDto);
        assertThat(result.getRating()).isEqualTo(1);
    }

    @Test
    @DisplayName("리뷰 삭제 테스트 - Soft Delete 확인")
    void deleteReviewTest() {
        // 1. 리뷰 준비
        setupBaseData();
        Order order = createOrder();
        order.updateStatus(OrderStatus.COMPLETED, "배달 완료");
        orderRepository.saveAndFlush(order);
        
        ReviewCreateReqDto createDto = createReviewCreateDto(order.getOrderId(), testStore.getStoreId(), 5, "삭제될 리뷰");
        ReviewResDto created = reviewCommandService.createReview(testUser.getUsername(), createDto);
        entityManager.flush();

        // 2. 리뷰 삭제 호출
        reviewCommandService.deleteReview(created.getReviewId(), testUser.getUsername(), UserRole.CUSTOMER);
        entityManager.flush();
        entityManager.clear();

        // 3. Soft Delete 상태 확인 (DB에는 존재하지만 isDeleted가 true여야 함)
        Review deletedReview = reviewRepository.findById(created.getReviewId()).orElseThrow();
        assertThat(deletedReview.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("가게별 리뷰 조회 시 평점 필터링 테스트")
    void getStoreReviews_FilteringTest() {
        // 1. 다양한 평점의 리뷰 준비
        setupBaseData();
        createReviewWithRating(1, "별로에요");
        createReviewWithRating(3, "보통이에요");
        createReviewWithRating(5, "최고에요");
        entityManager.flush();

        // 2. 필터링 요청 (평점 4~5점 조회)
        StoreReviewSearchReqDto reqDto = new StoreReviewSearchReqDto();
        reqDto.setMinRating(4);
        reqDto.setMaxRating(5);
        
        // 3. 조회 결과 검증
        ReviewListResDto result = reviewQueryService.getStoreReviews(testStore.getStoreId(), reqDto);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("리뷰 단건 상세 조회 테스트")
    void getReview_DetailTest() {
        // 1. 리뷰 준비
        setupBaseData();
        Order order = createOrder();
        order.updateStatus(OrderStatus.COMPLETED, "배달 완료");
        orderRepository.saveAndFlush(order);
        
        ReviewCreateReqDto createDto = createReviewCreateDto(order.getOrderId(), testStore.getStoreId(), 5, "상세 조회용 리뷰");
        ReviewResDto created = reviewCommandService.createReview(testUser.getUsername(), createDto);
        assertThat(created).isNotNull();
        UUID reviewId = created.getReviewId();
        assertThat(reviewId).isNotNull();
        
        entityManager.flush();

        // 2. 상세 조회 호출
        ReviewResDto result = reviewQueryService.getReview(reviewId);
        
        // 3. 결과 검증
        assertThat(result.getReviewId()).isEqualTo(reviewId);
    }

    @Test
    @DisplayName("내 리뷰 목록 조회 테스트")
    void getMyReviews_Test() {
        // 1. 본인 리뷰와 타인 리뷰 혼합 데이터 준비
        setupBaseData();
        createReviewWithRating(5, "내가 쓴 리뷰 1");
        createReviewWithRating(4, "내가 쓴 리뷰 2");

        User otherUser = User.builder()
                .username("other" + UUID.randomUUID().toString().substring(0,5))
                .password("password")
                .name("다른사람")
                .role(UserRole.CUSTOMER)
                .build();
        userRepository.saveAndFlush(otherUser);
        
        Order otherOrder = Order.builder()
                .user(otherUser).store(testStore).address(testAddress)
                .deliveryAddressSnapshot("주소").totalPrice(10000).totalQuantity(1)
                .createdAudit(CreateAudit.now(otherUser.getUsername())).softDeleteAudit(SoftDeleteAudit.active())
                .orderItems(new ArrayList<>())
                .build();
        orderRepository.saveAndFlush(otherOrder);
        addOrderItem(otherOrder, "다른메뉴");
        
        otherOrder.updateStatus(OrderStatus.COMPLETED, "배달 완료");
        orderRepository.saveAndFlush(otherOrder);
        
        ReviewCreateReqDto otherDto = createReviewCreateDto(otherOrder.getOrderId(), testStore.getStoreId(), 3, "다른 리뷰");
        reviewCommandService.createReview(otherUser.getUsername(), otherDto);

        entityManager.flush();

        // 2. 내 리뷰 목록 조회 호출
        com.dfdt.delivery.domain.review.presentation.dto.request.MyReviewSearchReqDto reqDto = 
                new com.dfdt.delivery.domain.review.presentation.dto.request.MyReviewSearchReqDto();
        ReviewListResDto result = reviewQueryService.getMyReviews(testUser.getUsername(), reqDto);

        // 3. 본인 데이터만 2건 조회되는지 검증
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("관리자 권한으로 삭제된 리뷰까지 검색 테스트")
    void searchReviews_AdminTest() {
        // 1. 리뷰 작성 후 삭제 처리
        setupBaseData();
        Order order = createOrder();
        order.updateStatus(OrderStatus.COMPLETED, "배달 완료");
        orderRepository.saveAndFlush(order);
        
        ReviewCreateReqDto createDto = createReviewCreateDto(order.getOrderId(), testStore.getStoreId(), 5, "삭제될 리뷰");
        ReviewResDto created = reviewCommandService.createReview(testUser.getUsername(), createDto);
        reviewCommandService.deleteReview(created.getReviewId(), testUser.getUsername(), UserRole.CUSTOMER);
        entityManager.flush();

        // 2. 관리자 검색 요청 (삭제된 리뷰 포함 설정)
        ReviewSearchReqDto searchReq = new ReviewSearchReqDto();
        try {
            java.lang.reflect.Field includeDeletedField = ReviewSearchReqDto.class.getDeclaredField("includeDeleted");
            includeDeletedField.setAccessible(true);
            includeDeletedField.set(searchReq, true);
            java.lang.reflect.Field storeIdField = ReviewSearchReqDto.class.getDeclaredField("storeId");
            storeIdField.setAccessible(true);
            storeIdField.set(searchReq, testStore.getStoreId());
        } catch (Exception e) {}

        // 3. 관리자용 전체 검색 실행 및 검증
        ReviewListResDto result = reviewQueryService.searchReviews(searchReq);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("리뷰 이미지 정렬 테스트 - displayOrder 순서대로 반환되는지 확인")
    void reviewImageOrderTest() {
        // 1. 완료된 주문 준비
        setupBaseData();
        Order order = createOrder();
        order.updateStatus(OrderStatus.COMPLETED, "배달 완료");
        orderRepository.saveAndFlush(order);

        // 2. 리뷰 작성 (이미지 3개 포함)
        java.util.List<String> imageUrls = java.util.Arrays.asList("url3.jpg", "url1.jpg", "url2.jpg");
        ReviewCreateReqDto reqDto = createReviewCreateDto(order.getOrderId(), testStore.getStoreId(), 5, "이미지 정렬 테스트");
        
        // Reflection을 사용하여 imageUrls 설정 (createReviewCreateDto 헬퍼가 imageUrls를 안받음)
        try {
            java.lang.reflect.Field imagesField = ReviewCreateReqDto.class.getDeclaredField("imageUrls");
            imagesField.setAccessible(true);
            imagesField.set(reqDto, imageUrls);
        } catch (Exception e) {}

        ReviewResDto result = reviewCommandService.createReview(testUser.getUsername(), reqDto);
        entityManager.flush();
        entityManager.clear();

        // 3. 단건 조회로 이미지 순서 검증
        ReviewResDto detailedReview = reviewQueryService.getReview(result.getReviewId());
        
        // 4. 검증: 입력된 순서(url3, url1, url2)대로 displayOrder(1, 2, 3)가 부여되었으므로 그 순서대로 나와야 함
        assertThat(detailedReview.getImages()).hasSize(3);
        assertThat(detailedReview.getImages().get(0)).isEqualTo("url3.jpg");
        assertThat(detailedReview.getImages().get(1)).isEqualTo("url1.jpg");
        assertThat(detailedReview.getImages().get(2)).isEqualTo("url2.jpg");
    }

    // --- Helper Methods ---

    /**
     * 테스트용 기초 데이터 (지역, 사용자, 주소, 가게, 평점객체) 초기화
     */
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
        userRepository.saveAndFlush(testUser);

        testAddress = new Address(
                null,
                "집",
                "강남구",
                null,
                "수령인",
                "010-1234-5678",
                true,
                null,
                CreateAudit.now("SYSTEM"),
                com.dfdt.delivery.common.infrastructure.persistence.embedded.UpdateAudit.empty(),
                SoftDeleteAudit.active()
        );
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
        storeRepository.saveAndFlush(testStore);
        
        try {
            java.lang.reflect.Constructor<StoreRating> constructor = StoreRating.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            StoreRating rating = constructor.newInstance();
            
            java.lang.reflect.Field storeRefField = StoreRating.class.getDeclaredField("store");
            storeRefField.setAccessible(true);
            storeRefField.set(rating, testStore);
            
            java.lang.reflect.Field createAuditField = StoreRating.class.getDeclaredField("createAudit");
            createAuditField.setAccessible(true);
            createAuditField.set(rating, CreateAudit.now("SYSTEM"));
            
            java.lang.reflect.Field softDeleteAuditField = StoreRating.class.getDeclaredField("softDeleteAudit");
            softDeleteAuditField.setAccessible(true);
            softDeleteAuditField.set(rating, SoftDeleteAudit.active());
            
            java.lang.reflect.Field ratingSumField = StoreRating.class.getDeclaredField("ratingSum");
            ratingSumField.setAccessible(true);
            ratingSumField.set(rating, 0);
            
            java.lang.reflect.Field ratingCountField = StoreRating.class.getDeclaredField("ratingCount");
            ratingCountField.setAccessible(true);
            ratingCountField.set(rating, 0);
            
            java.lang.reflect.Field ratingAvgField = StoreRating.class.getDeclaredField("ratingAvg");
            ratingAvgField.setAccessible(true);
            ratingAvgField.set(rating, java.math.BigDecimal.ZERO);

            entityManager.persist(rating);
            
            java.lang.reflect.Field ratingField = Store.class.getDeclaredField("storeRating");
            ratingField.setAccessible(true);
            ratingField.set(testStore, rating);
        } catch (Exception e) {}
        entityManager.flush();
    }

    /**
     * 테스트용 주문 생성
     */
    private Order createOrder() {
        Order order = Order.builder()
                .user(testUser)
                .store(testStore)
                .address(testAddress)
                .deliveryAddressSnapshot("주소")
                .totalPrice(20000)
                .totalQuantity(1)
                .createdAudit(CreateAudit.now(testUser.getUsername()))
                .softDeleteAudit(SoftDeleteAudit.active())
                .orderItems(new ArrayList<>())
                .build();
        orderRepository.saveAndFlush(order);
        addOrderItem(order, "메뉴");
        entityManager.flush();
        return order;
    }

    /**
     * 주문 아이템(메뉴 스냅샷) 추가
     */
    private void addOrderItem(Order order, String productName) {
        OrderItem item = OrderItem.builder()
                .order(order)
                .productId(UUID.randomUUID())
                .productNameSnapshot(productName)
                .unitPriceSnapshot(10000)
                .totalPrice(10000)
                .quantity(1)
                .softDeleteAudit(SoftDeleteAudit.active())
                .build();
        entityManager.persist(item);
        order.getOrderItems().add(item);
        entityManager.flush();
    }

    /**
     * 리뷰 작성을 위한 Request DTO 생성 헬퍼
     */
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

    /**
     * 특정 평점의 리뷰를 바로 생성하는 헬퍼
     */
    private void createReviewWithRating(int rating, String content) {
        Order order = createOrder();
        order.updateStatus(OrderStatus.COMPLETED, "배달 완료");
        orderRepository.saveAndFlush(order);
        ReviewCreateReqDto createDto = createReviewCreateDto(order.getOrderId(), testStore.getStoreId(), rating, content);
        reviewCommandService.createReview(testUser.getUsername(), createDto);
    }
}
