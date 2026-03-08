package com.dfdt.delivery.domain.review.presentation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreReviewSearchReqDto {

    @Min(1) @Max(5)
    private Integer minRating;

    @Min(1) @Max(5)
    private Integer maxRating;

    private String keyword;

    private int page = 0;

    @Min(10) @Max(50)
    private int size = 10;

    private String sort = "createdAt,desc";
}
