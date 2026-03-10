package com.dfdt.delivery.domain.ai.domain.client;

/**
 * 이미지 생성 API의 응답 데이터를 담는 값 객체.
 * base64 인코딩된 이미지 데이터와 MIME 타입을 포함합니다.
 */
public record GeneratedImageData(
        String base64Data,  // base64 인코딩된 이미지 바이너리
        String mimeType     // e.g., "image/png"
) {
}
