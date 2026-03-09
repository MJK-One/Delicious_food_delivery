package com.dfdt.delivery.domain.review.application.service.validator;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.order.domain.entity.Order;
import com.dfdt.delivery.domain.order.domain.enums.OrderStatus;
import com.dfdt.delivery.domain.review.domain.entity.Review;
import com.dfdt.delivery.domain.review.domain.enums.ReviewErrorCode;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import org.springframework.stereotype.Component;

@Component
public class ReviewValidator {

    /**
     * 리뷰 작성 가능 여부 검증
     */
    public void validateCreate(Order order, String username, boolean alreadyReviewed) {
        // 1. 주문 소유자 검증
        if (!order.getUser().getUsername().equals(username)) {
            throw new BusinessException(ReviewErrorCode.NOT_ORDER_OWNER);
        }

        // 2. 주문 상태 검증 (COMPLETED 상태에서만 작성 가능)
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException(ReviewErrorCode.INVALID_ORDER_STATUS);
        }

        // 3. 이미 리뷰 작성 여부 확인
        if (alreadyReviewed) {
            throw new BusinessException(ReviewErrorCode.ALREADY_REVIEWED);
        }
    }

    /**
     * 리뷰 수정 권한 및 상태 검증
     */
    public void validateUpdate(Review review, String username) {
        // 1. 삭제 여부 확인
        if (review.isDeleted()) {
            throw new BusinessException(ReviewErrorCode.REVIEW_ALREADY_DELETED);
        }

        // 2. 작성자 검증
        if (!review.getWriterUsername().equals(username)) {
            throw new BusinessException(ReviewErrorCode.NOT_REVIEW_WRITER);
        }
    }

    /**
     * 리뷰 삭제 권한 및 상태 검증
     */
    public void validateDelete(Review review, String username, UserRole role) {
        // 1. 삭제 여부 확인
        if (review.isDeleted()) {
            throw new BusinessException(ReviewErrorCode.REVIEW_ALREADY_DELETED);
        }

        // 2. 권한 검증 (작성자 or MASTER)
        if (!review.getWriterUsername().equals(username) && role != UserRole.MASTER) {
            throw new BusinessException(ReviewErrorCode.NOT_REVIEW_DELETER);
        }
    }

    /**
     * 목록 조회 사이즈 제약 조건 검증 및 조정
     */
    public int validateAndAdjustSize(int size) {
        if (size != 10 && size != 30 && size != 50) {
            return 10;
        }
        return size;
    }
}
