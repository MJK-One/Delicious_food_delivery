package com.dfdt.delivery.domain.review.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class ReviewListResDto {

    private List<ReviewResDto> content;

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static ReviewListResDto from(Page<ReviewResDto> page) {
        return ReviewListResDto.builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}