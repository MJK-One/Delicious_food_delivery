package com.dfdt.delivery.domain.payment.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class PaymentHistoryPageResDto {

    private List<PaymentHistoryResDto> content;

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static PaymentHistoryPageResDto from(Page<PaymentHistoryResDto> page) {
        return PaymentHistoryPageResDto.builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}