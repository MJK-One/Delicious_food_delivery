package com.dfdt.delivery.domain.payment.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class PaymentListResDto {

    private List<PaymentListItemResDto> content;

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static PaymentListResDto from(Page<PaymentListItemResDto> page) {
        return PaymentListResDto.builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}