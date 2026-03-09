package com.dfdt.delivery.domain.review.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.review.application.service.command.ReviewCommandService;
import com.dfdt.delivery.domain.review.application.service.query.ReviewQueryService;
import com.dfdt.delivery.domain.review.presentation.dto.request.MyReviewSearchReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewCreateReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewSearchReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewUpdateReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.StoreReviewSearchReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.response.ReviewListResDto;
import com.dfdt.delivery.domain.review.presentation.dto.response.ReviewResDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<ReviewResDto>> createReview(
            @Valid @RequestBody ReviewCreateReqDto request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        ReviewResDto response = reviewCommandService.createReview(customUserDetails.getUsername(), request);
        return ApiResponseDto.success(201, "리뷰가 성공적으로 작성되었습니다.", response);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponseDto<ReviewResDto>> getReview(
            @PathVariable UUID reviewId
    ) {
        ReviewResDto response = reviewQueryService.getReview(reviewId);
        return ApiResponseDto.success(200, "리뷰 조회가 완료되었습니다.", response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponseDto<ReviewListResDto>> getMyReviews(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @ModelAttribute @Valid MyReviewSearchReqDto request
    ) {
        ReviewListResDto response = reviewQueryService.getMyReviews(customUserDetails.getUsername(), request);
        return ApiResponseDto.success(200, "내 리뷰 목록 조회가 완료되었습니다.", response);
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponseDto<ReviewListResDto>> getStoreReviews(
            @PathVariable UUID storeId,
            @ModelAttribute @Valid StoreReviewSearchReqDto request
    ) {
        ReviewListResDto response = reviewQueryService.getStoreReviews(storeId, request);
        return ApiResponseDto.success(200, "가게 리뷰 목록 조회가 완료되었습니다.", response);
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<ApiResponseDto<ReviewResDto>> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewUpdateReqDto request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        ReviewResDto response = reviewCommandService.updateReview(reviewId, customUserDetails.getUsername(), request);
        return ApiResponseDto.success(200, "리뷰가 성공적으로 수정되었습니다.", response);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        reviewCommandService.deleteReview(reviewId, customUserDetails.getUsername(), customUserDetails.getRole());
        return ApiResponseDto.success(200, "리뷰가 성공적으로 삭제되었습니다.", null);
    }

    @PreAuthorize("hasRole('MASTER')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDto<ReviewListResDto>> searchReviews(
            @ModelAttribute @Valid ReviewSearchReqDto request
    ) {
        ReviewListResDto response = reviewQueryService.searchReviews(request);
        return ApiResponseDto.success(200, "리뷰 검색이 완료되었습니다.", response);
    }
}
