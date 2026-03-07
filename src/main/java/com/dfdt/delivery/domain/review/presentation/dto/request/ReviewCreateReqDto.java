package com.dfdt.delivery.domain.review.presentation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class ReviewCreateReqDto {

    @NotNull
    private UUID orderId;

    @NotNull
    private UUID storeId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    private String content;

    private List<String> imageUrls;
}