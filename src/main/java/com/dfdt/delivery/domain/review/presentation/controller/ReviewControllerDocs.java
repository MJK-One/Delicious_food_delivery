package com.dfdt.delivery.domain.review.presentation.controller;

import com.dfdt.delivery.common.response.ApiResponseDto;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.review.presentation.dto.request.MyReviewSearchReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewCreateReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewSearchReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.ReviewUpdateReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.request.StoreReviewSearchReqDto;
import com.dfdt.delivery.domain.review.presentation.dto.response.ReviewListResDto;
import com.dfdt.delivery.domain.review.presentation.dto.response.ReviewResDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tag(name = "Review (리뷰)", description = "리뷰 생성, 수정, 삭제 및 다중 조건 조회를 담당합니다.")
public interface ReviewControllerDocs {

    @Operation(summary = "API-001 리뷰 작성", description = "완료된 주문에 대해 리뷰를 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "리뷰 작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "평점 오류", value = ReviewErrorDocs.INVALID_RATING),
                    @ExampleObject(name = "주문 상태 오류", value = ReviewErrorDocs.INVALID_ORDER_STATUS),
                    @ExampleObject(name = "이미지 개수 초과", value = ReviewErrorDocs.IMAGE_LIMIT_EXCEEDED)
            })),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "주문 소유자 아님", value = ReviewErrorDocs.NOT_ORDER_OWNER)
            })),
            @ApiResponse(responseCode = "404", description = "찾을 수 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "주문 없음", value = ReviewErrorDocs.ORDER_NOT_FOUND),
                    @ExampleObject(name = "가게 없음", value = ReviewErrorDocs.STORE_NOT_FOUND)
            })),
            @ApiResponse(responseCode = "409", description = "이미 존재함", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "이미 리뷰 작성됨", value = ReviewErrorDocs.ALREADY_REVIEWED)
            }))
    })
    ResponseEntity<ApiResponseDto<ReviewResDto>> createReview(
            @Valid @RequestBody ReviewCreateReqDto request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails);

    @Operation(summary = "API-002 리뷰 단건 조회", description = "리뷰 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "찾을 수 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "리뷰 없음", value = ReviewErrorDocs.REVIEW_NOT_FOUND)
            }))
    })
    ResponseEntity<ApiResponseDto<ReviewResDto>> getReview(@PathVariable UUID reviewId);

    @Operation(summary = "API-003 내 리뷰 목록 조회", description = "로그인한 사용자가 작성한 리뷰 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<ApiResponseDto<ReviewListResDto>> getMyReviews(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @ModelAttribute @Valid MyReviewSearchReqDto request);

    @Operation(summary = "API-004 가게 리뷰 목록 조회", description = "특정 가게의 리뷰 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<ApiResponseDto<ReviewListResDto>> getStoreReviews(
            @PathVariable UUID storeId,
            @ModelAttribute @Valid StoreReviewSearchReqDto request);

    @Operation(summary = "API-005 리뷰 수정", description = "본인이 작성한 리뷰를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "이미 삭제된 리뷰", value = ReviewErrorDocs.REVIEW_ALREADY_DELETED)
            })),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "작성자 아님", value = ReviewErrorDocs.NOT_REVIEW_WRITER)
            })),
            @ApiResponse(responseCode = "404", description = "찾을 수 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "리뷰 없음", value = ReviewErrorDocs.REVIEW_NOT_FOUND)
            }))
    })
    ResponseEntity<ApiResponseDto<ReviewResDto>> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewUpdateReqDto request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails);

    @Operation(summary = "API-006 리뷰 삭제", description = "리뷰를 삭제(Soft Delete)합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "삭제 권한 없음", value = ReviewErrorDocs.NOT_REVIEW_DELETER)
            })),
            @ApiResponse(responseCode = "404", description = "찾을 수 없음", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "리뷰 없음", value = ReviewErrorDocs.REVIEW_NOT_FOUND)
            }))
    })
    ResponseEntity<ApiResponseDto<Void>> deleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails);

    @Operation(summary = "API-007 리뷰 통합 검색", description = "전체 리뷰를 대상으로 다중 조건 검색을 수행합니다. (MASTER 전용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "403", description = "권한 부족")
    })
    ResponseEntity<ApiResponseDto<ReviewListResDto>> searchReviews(
            @ModelAttribute @Valid ReviewSearchReqDto request);
}
