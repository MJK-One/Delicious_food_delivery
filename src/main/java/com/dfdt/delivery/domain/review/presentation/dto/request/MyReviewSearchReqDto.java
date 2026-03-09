package com.dfdt.delivery.domain.review.presentation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class MyReviewSearchReqDto {

    private UUID storeId;

    @Min(1) @Max(5)
    private Integer minRating;

    @Min(1) @Max(5)
    private Integer maxRating;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toDate;

    private String keyword;

    private int page = 0;

    @Min(10) @Max(50)
    private int size = 10;

    private String sort = "createdAt,desc";
}
