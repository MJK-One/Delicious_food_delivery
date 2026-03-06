package com.dfdt.delivery.domain.review.presentation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

import java.util.List;

@Getter
public class ReviewUpdateReqDto {

    @Min(1)
    @Max(5)
    private Integer rating;

    private String content;

    private List<String> imageUrls;
}