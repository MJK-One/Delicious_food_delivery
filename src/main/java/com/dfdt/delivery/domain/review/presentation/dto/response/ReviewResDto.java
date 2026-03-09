package com.dfdt.delivery.domain.review.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResDto {

    private UUID reviewId;

    private String username;

    private String storeName;

    private List<String> orderMenuNames;

    private int rating;

    private String content;

    private List<String> images;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}