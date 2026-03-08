package com.dfdt.delivery.domain.review.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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