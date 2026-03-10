package com.dfdt.delivery.domain.ai.domain.client;

/**
 * 외부 Gemini AI API 호출 인터페이스.
 * 구현체는 infrastructure 계층에 위치합니다.
 */
public interface GeminiClient {

    /**
     * 프롬프트를 Gemini API에 전달하여 텍스트 응답을 반환합니다.
     *
     * @param prompt 최종 조립된 프롬프트
     * @return AI 생성 응답 텍스트
     * @throws com.dfdt.delivery.common.exception.BusinessException AI 호출 실패 시
     */
    String generate(String prompt);

    /**
     * 현재 사용 중인 Gemini 모델명을 반환합니다.
     */
    String getModelName();
}
