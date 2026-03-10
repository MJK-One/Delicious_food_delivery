package com.dfdt.delivery.domain.ai.infrastructure.client;

import com.dfdt.delivery.common.exception.BusinessException;
import com.dfdt.delivery.domain.ai.domain.client.GeneratedImageData;
import com.dfdt.delivery.domain.ai.domain.client.ImageGenerationClient;
import com.dfdt.delivery.domain.ai.domain.enums.AiErrorCode;
import com.dfdt.delivery.domain.ai.infrastructure.config.GeminiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Imagen 4 Fast API를 사용한 이미지 생성 클라이언트.
 *
 * Google AI Imagen API의 :predict 엔드포인트를 사용합니다.
 * 요청: instances[0].prompt + parameters.aspectRatio
 * 응답: predictions[0].bytesBase64Encoded + predictions[0].mimeType
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiImageWebClient implements ImageGenerationClient {

    private final RestClient restClient;
    private final GeminiProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public GeneratedImageData generate(String prompt, String aspectRatio) {
        String url = "/v1beta/models/" + properties.imageModel() + ":predict?key=" + properties.apiKey();

        // Imagen API 요청 형식: instances + parameters
        Map<String, Object> requestBody = Map.of(
                "instances", List.of(
                        Map.of("prompt", prompt)
                ),
                "parameters", Map.of(
                        "sampleCount", 1,
                        "aspectRatio", aspectRatio
                )
        );

        String responseJson;
        try {
            responseJson = executeHttpCall(url, requestBody);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[GeminiImageWebClient] Imagen API 호출 실패: {}", e.getMessage(), e);
            throw new BusinessException(AiErrorCode.EXTERNAL_AI_CALL_FAILED);
        }

        return parseImage(responseJson);
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
     * Imagen API 응답 JSON에서 base64 이미지 데이터를 추출합니다.
     * 응답 구조: predictions[0].bytesBase64Encoded + predictions[0].mimeType
     */
    GeneratedImageData parseImage(String responseJson) {
        if (responseJson == null || responseJson.isBlank()) {
            throw new BusinessException(AiErrorCode.EXTERNAL_AI_EMPTY_RESPONSE);
        }

        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode predictions = root.path("predictions");

            if (predictions.isMissingNode() || predictions.isEmpty()) {
                throw new BusinessException(AiErrorCode.EXTERNAL_AI_EMPTY_RESPONSE);
            }

            JsonNode prediction = predictions.get(0);
            String base64Data = prediction.path("bytesBase64Encoded").asText(null);
            String mimeType = prediction.path("mimeType").asText(null);

            if (base64Data == null || base64Data.isBlank()) {
                throw new BusinessException(AiErrorCode.EXTERNAL_AI_EMPTY_RESPONSE);
            }

            return new GeneratedImageData(base64Data, mimeType != null ? mimeType : "image/jpeg");

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[GeminiImageWebClient] Imagen 응답 파싱 실패: {}", e.getMessage(), e);
            throw new BusinessException(AiErrorCode.EXTERNAL_AI_CALL_FAILED);
        }
    }
}
