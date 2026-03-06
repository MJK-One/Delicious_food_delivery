package com.dfdt.delivery.domain.review.presentation.dto.request;

import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ReviewSearchReqDto {
    private UUID storeId;

    private UUID orderId;

    private UUID reviewId;

    private String writer;

    private Integer minRating;

    private Integer maxRating;

    private String keyword;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toDate;

    private Boolean includeDeleted = false;

    private Integer page = 0;

    private Integer size = 10;

    private String sort = "createdAt,desc";
}
