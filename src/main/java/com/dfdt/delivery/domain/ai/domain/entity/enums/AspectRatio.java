package com.dfdt.delivery.domain.ai.domain.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AspectRatio {
    SQUARE("1:1", "정사각형"),
    LANDSCAPE("16:9", "가로형"),
    PORTRAIT("9:16", "세로형"),
    STANDARD("4:3", "일반");

    private final String ratio;        // 프롬프트에 삽입될 비율 문자열
    private final String description;  // 한국어 설명
}
