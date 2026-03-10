package com.dfdt.delivery.domain.ai.domain.client;

/**
 * 외부 이미지 생성 AI API 호출 인터페이스.
 * 구현체는 infrastructure 계층에 위치합니다.
 */
public interface ImageGenerationClient {

    /**
     * 프롬프트와 비율 옵션을 이미지 생성 API에 전달하여 base64 이미지 데이터를 반환합니다.
     *
     * @param prompt      최종 조립된 이미지 생성 프롬프트
     * @param aspectRatio 이미지 비율 문자열 (예: "1:1", "16:9") — API 네이티브 파라미터로 전달
     * @return base64 인코딩된 이미지 데이터 및 MIME 타입
     * @throws com.dfdt.delivery.common.exception.BusinessException AI 호출 실패 시
     */
    GeneratedImageData generate(String prompt, String aspectRatio);
}
