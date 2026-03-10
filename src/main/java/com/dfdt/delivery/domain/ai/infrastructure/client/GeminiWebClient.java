package com.dfdt.delivery.domain.ai.infrastructure.client;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.domain.client.GeminiClient;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
import com.dfdt.delivery.domain.ai.infrastructure.config.GeminiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiWebClient implements GeminiClient {

    private final RestClient restClient;
    private final GeminiProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getModelName() {
        return properties.model();
    }

    @Override
    public String generate(String prompt) {
        String url = "/v1beta/models/" + properties.model() + ":generateContent?key=" + properties.apiKey();

        // Gemini API 요청 바디: {"contents": [{"parts": [{"text": "..."}]}]}
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        String responseJson;
        try {
            responseJson = executeHttpCall(url, requestBody);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[GeminiWebClient] API 호출 실패: {}", e.getMessage(), e);
            throw new BusinessException(AiErrorCode.EXTERNAL_AI_CALL_FAILED);
        }

        return parseText(responseJson);
    }

    /**
     * 실제 HTTP 요청을 수행합니다.
     * 테스트에서 오버라이드하여 HTTP 호출 없이 단위 테스트가 가능합니다.
     */
    protected String executeHttpCall(String url, Object body) {
        return restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);
    }

    /**
     * Gemini API JSON 응답에서 텍스트를 추출합니다.
     * 응답 구조: candidates[0].content.parts[0].text
     */
    String parseText(String responseJson) {
        if (responseJson == null || responseJson.isBlank()) {
            throw new BusinessException(AiErrorCode.EXTERNAL_AI_EMPTY_RESPONSE);
        }

        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode candidates = root.path("candidates");

            if (candidates.isMissingNode() || candidates.isEmpty()) {
                throw new BusinessException(AiErrorCode.EXTERNAL_AI_EMPTY_RESPONSE);
            }

            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (parts.isMissingNode() || parts.isEmpty()) {
                log.warn("[GeminiWebClient] 응답에 parts 배열이 없음");
                throw new BusinessException(AiErrorCode.EXTERNAL_AI_EMPTY_RESPONSE);
            }

            String text = parts.get(0).path("text").asText(null);

            if (text == null || text.isBlank()) {
                throw new BusinessException(AiErrorCode.EXTERNAL_AI_EMPTY_RESPONSE);
            }

            return text;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[GeminiWebClient] 응답 파싱 실패: {}", e.getMessage(), e);
            throw new BusinessException(AiErrorCode.EXTERNAL_AI_CALL_FAILED);
        }
    }
}
