package com.dfdt.delivery.domain.auth.presentation.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class TokenResponseDto {
    private String accessToken;
    private String refreshToken;

    public static TokenResponseDto of(String accessToken, String refreshToken) {
        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
