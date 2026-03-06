package com.dfdt.delivery.domain.review.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewImageResDto {

    private String imageUrl;

    private Integer displayOrder;
}