package com.dfdt.delivery.domain.ai.presentation.controller;

import com.dfdt.delivery.common.config.SecurityConfig;
import com.dfdt.delivery.domain.ai.application.dto.AiHealthResult;
import com.dfdt.delivery.domain.ai.application.dto.AiLogDetailResult;
import com.dfdt.delivery.domain.ai.application.dto.AiLogSummaryResult;
import com.dfdt.delivery.domain.ai.application.dto.AiStatsResult;
import com.dfdt.delivery.domain.ai.application.dto.ApplyDescriptionResult;
import com.dfdt.delivery.domain.ai.application.dto.GenerateDescriptionResult;
import com.dfdt.delivery.domain.ai.application.dto.AiPromptRulesResult;
import com.dfdt.delivery.domain.ai.application.dto.GenerateImageResult;
import com.dfdt.delivery.domain.ai.application.dto.RetryDescriptionResult;
import com.dfdt.delivery.domain.ai.application.dto.RollbackDescriptionResult;
import com.dfdt.delivery.domain.ai.application.usecase.ApplyDescriptionUseCase;
import com.dfdt.delivery.domain.ai.application.usecase.CheckAiHealthUseCase;
import com.dfdt.delivery.domain.ai.application.usecase.GenerateDescriptionUseCase;
import com.dfdt.delivery.domain.ai.application.usecase.GenerateImageUseCase;
import com.dfdt.delivery.domain.ai.application.usecase.GetAiLogDetailUseCase;
import com.dfdt.delivery.domain.ai.application.usecase.GetAiStatsUseCase;
import com.dfdt.delivery.domain.ai.application.usecase.GetPromptRulesUseCase;
import com.dfdt.delivery.domain.ai.application.usecase.RetryDescriptionUseCase;
import com.dfdt.delivery.domain.ai.application.usecase.RollbackDescriptionUseCase;
import com.dfdt.delivery.domain.ai.application.usecase.SearchAiLogsUseCase;
import com.dfdt.delivery.domain.ai.application.usecase.SearchProductAiLogsUseCase;
import com.dfdt.delivery.domain.ai.domain.entity.enums.AiRequestType;
import com.dfdt.delivery.domain.ai.domain.entity.enums.Tone;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetailsService;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.enums.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AiDescriptionController.class)
@Import(SecurityConfig.class)
@DisplayName("AiDescriptionController 테스트")
class AiDescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // @EnableJpaAuditing이 메인 앱에 있어서 @WebMvcTest에서 JPA 메타모델 오류 발생 방지
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockBean
    private GenerateDescriptionUseCase generateDescriptionUseCase;

    @MockBean
    private ApplyDescriptionUseCase applyDescriptionUseCase;

    @MockBean
    private SearchAiLogsUseCase searchAiLogsUseCase;

    @MockBean
    private GetAiLogDetailUseCase getAiLogDetailUseCase;

    @MockBean
    private SearchProductAiLogsUseCase searchProductAiLogsUseCase;

    @MockBean
    private CheckAiHealthUseCase checkAiHealthUseCase;

    @MockBean
    private GetPromptRulesUseCase getPromptRulesUseCase;

    @MockBean
    private RetryDescriptionUseCase retryDescriptionUseCase;

    @MockBean
    private RollbackDescriptionUseCase rollbackDescriptionUseCase;

    @MockBean
    private GenerateImageUseCase generateImageUseCase;

    @MockBean
    private GetAiStatsUseCase getAiStatsUseCase;

    @MockBean
    private com.dfdt.delivery.domain.auth.infrastructure.jwt.JwtProvider jwtProvider;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private com.dfdt.delivery.common.util.RedisService redisService;

    private UUID storeId;
    private CustomUserDetails ownerDetails;
    private CustomUserDetails masterDetails;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();

        User owner = User.builder()
                .username("owner123")
                .name("점주")
                .password("pw")
                .role(UserRole.OWNER)
                .build();
        ownerDetails = new CustomUserDetails(owner);

        User master = User.builder()
                .username("master")
                .name("관리자")
                .password("pw")
                .role(UserRole.MASTER)
                .build();
        masterDetails = new CustomUserDetails(master);
    }

    // ──────────────────────────────────────────────────
    // API-AI-201: AI 연동 상태 확인
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("AI 연동 상태 확인 (API-AI-201)")
    class CheckAiHealthTests {

        @Test
        @DisplayName("MASTER가 요청하면 200과 UP 상태를 반환한다")
        void masterShouldReturn200WithUpStatus() throws Exception {
            // given
            AiHealthResult upResult = new AiHealthResult(
                    "UP", "gemini-2.0-flash", 350, null, OffsetDateTime.now()
            );
            given(checkAiHealthUseCase.execute()).willReturn(upResult);

            // when & then
            mockMvc.perform(get("/api/v1/ai/health")
                            .with(user(masterDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.status").value("UP"))
                    .andExpect(jsonPath("$.data.modelName").value("gemini-2.0-flash"))
                    .andExpect(jsonPath("$.data.responseTimeMs").value(350))
                    .andExpect(jsonPath("$.data.errorMessage").doesNotExist());
        }

        @Test
        @DisplayName("Gemini API 장애 시 MASTER에게 200과 DOWN 상태를 반환한다")
        void masterShouldReturn200WithDownStatus() throws Exception {
            // given
            AiHealthResult downResult = new AiHealthResult(
                    "DOWN", "gemini-2.0-flash", 5000, "외부 AI API 호출에 실패했습니다.", OffsetDateTime.now()
            );
            given(checkAiHealthUseCase.execute()).willReturn(downResult);

            // when & then
            mockMvc.perform(get("/api/v1/ai/health")
                            .with(user(masterDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("DOWN"))
                    .andExpect(jsonPath("$.data.errorMessage").value("외부 AI API 호출에 실패했습니다."));
        }

        @Test
        @DisplayName("OWNER 역할은 403을 반환한다")
        void ownerShouldReturn403() throws Exception {
            mockMvc.perform(get("/api/v1/ai/health")
                            .with(user(ownerDetails)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증되지 않은 요청은 4xx를 반환한다")
        void unauthenticatedShouldReturn4xx() throws Exception {
            mockMvc.perform(get("/api/v1/ai/health"))
                    .andExpect(status().is4xxClientError());
        }
    }

    // ──────────────────────────────────────────────────
    // API-AI-202: AI 프롬프트 규칙 미리보기
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("AI 프롬프트 규칙 미리보기 (API-AI-202)")
    class GetPromptRulesTests {

        @Test
        @DisplayName("OWNER가 요청하면 200과 프롬프트 규칙을 반환한다")
        void ownerShouldReturn200WithPromptRules() throws Exception {
            // given
            List<AiPromptRulesResult.ToneRuleResult> tones = List.of(
                    new AiPromptRulesResult.ToneRuleResult("FRIENDLY", "친근하고 따뜻한 톤으로 작성해줘."),
                    new AiPromptRulesResult.ToneRuleResult("SALESY", "세일즈 톤으로 구매 욕구를 자극하게 작성해줘."),
                    new AiPromptRulesResult.ToneRuleResult("INFORMATIVE", "정보 전달 위주의 간결한 톤으로 작성해줘.")
            );
            AiPromptRulesResult mockResult = new AiPromptRulesResult(
                    " 답변을 최대한 간결하게 50자 이하로 작성해줘.", 50,
                    300, 120, 10, 30,
                    tones, "[톤 지시문]\n{inputPrompt} 답변을 최대한 간결하게 50자 이하로 작성해줘."
            );
            given(getPromptRulesUseCase.execute()).willReturn(mockResult);

            // when & then
            mockMvc.perform(get("/api/v1/ai/prompt-rules")
                            .with(user(ownerDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.maxResponseLength").value(50))
                    .andExpect(jsonPath("$.data.maxInputPromptLength").value(300))
                    .andExpect(jsonPath("$.data.maxKeywordsCount").value(10))
                    .andExpect(jsonPath("$.data.availableTones").isArray())
                    .andExpect(jsonPath("$.data.availableTones.length()").value(3))
                    .andExpect(jsonPath("$.data.availableTones[0].tone").value("FRIENDLY"));
        }

        @Test
        @DisplayName("MASTER도 200을 반환한다")
        void masterShouldReturn200() throws Exception {
            // given
            given(getPromptRulesUseCase.execute()).willReturn(
                    new AiPromptRulesResult(" 답변을 최대한 간결하게 50자 이하로 작성해줘.", 50,
                            300, 120, 10, 30, List.of(), "template")
            );

            // when & then
            mockMvc.perform(get("/api/v1/ai/prompt-rules")
                            .with(user(masterDetails)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("CUSTOMER 역할은 403을 반환한다")
        void customerShouldReturn403() throws Exception {
            User customer = User.builder()
                    .username("customer1").name("고객").password("pw").role(UserRole.CUSTOMER).build();
            CustomUserDetails customerDetails = new CustomUserDetails(customer);

            mockMvc.perform(get("/api/v1/ai/prompt-rules")
                            .with(user(customerDetails)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증되지 않은 요청은 4xx를 반환한다")
        void unauthenticatedShouldReturn4xx() throws Exception {
            mockMvc.perform(get("/api/v1/ai/prompt-rules"))
                    .andExpect(status().is4xxClientError());
        }
    }

    // ──────────────────────────────────────────────────
    // 정상 케이스
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("정상 요청")
    class SuccessTests {

        @Test
        @DisplayName("OWNER가 유효한 요청을 보내면 201과 AI 응답을 반환한다")
        void shouldReturn201WithResult() throws Exception {
            // given
            UUID aiLogId = UUID.randomUUID();
            GenerateDescriptionResult mockResult = new GenerateDescriptionResult(
                    aiLogId, "바삭한 치킨입니다!", "최종프롬프트"
            );
            given(generateDescriptionUseCase.execute(any())).willReturn(mockResult);

            Map<String, Object> requestBody = Map.of(
                    "inputPrompt", "겉은 바삭하고 속은 촉촉하게 설명해줘",
                    "tone", "FRIENDLY",
                    "keywords", List.of("바삭", "촉촉")
            );

            // when & then
            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/descriptions/preview", storeId)
                            .with(user(ownerDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.data.responseText").value("바삭한 치킨입니다!"))
                    .andExpect(jsonPath("$.data.aiLogId").value(aiLogId.toString()));
        }

        @Test
        @DisplayName("MASTER도 동일하게 201을 반환한다")
        void masterCanAlsoGenerateDescription() throws Exception {
            // given
            given(generateDescriptionUseCase.execute(any())).willReturn(
                    new GenerateDescriptionResult(UUID.randomUUID(), "치킨 맛있어요!", "프롬프트")
            );

            Map<String, Object> requestBody = Map.of(
                    "inputPrompt", "설명 작성해줘",
                    "tone", "INFORMATIVE"
            );

            // when & then
            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/descriptions/preview", storeId)
                            .with(user(masterDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isCreated());
        }
    }

    // ──────────────────────────────────────────────────
    // 인증/권한 실패
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("인증/권한 실패")
    class AuthFailureTests {

        @Test
        @DisplayName("인증되지 않은 요청은 4xx를 반환한다")
        void shouldReturn4xxWhenNotAuthenticated() throws Exception {
            Map<String, Object> requestBody = Map.of(
                    "inputPrompt", "설명 작성",
                    "tone", "FRIENDLY"
            );

            // SecurityConfig에 별도 AuthenticationEntryPoint가 없으면 Spring Security 기본값(403) 반환
            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/descriptions/preview", storeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("CUSTOMER 역할은 403을 반환한다")
        void shouldReturn403WhenCustomerRole() throws Exception {
            User customer = User.builder()
                    .username("customer1").name("고객").password("pw").role(UserRole.CUSTOMER).build();
            CustomUserDetails customerDetails = new CustomUserDetails(customer);

            Map<String, Object> requestBody = Map.of(
                    "inputPrompt", "설명 작성",
                    "tone", "FRIENDLY"
            );

            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/descriptions/preview", storeId)
                            .with(user(customerDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isForbidden());
        }
    }

    // ──────────────────────────────────────────────────
    // 입력 검증 실패 (400 Bad Request)
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("입력 검증 실패")
    class ValidationFailureTests {

        @Test
        @DisplayName("inputPrompt가 없으면 400을 반환한다")
        void shouldReturn400WhenInputPromptMissing() throws Exception {
            Map<String, Object> requestBody = Map.of(
                    "tone", "FRIENDLY"  // inputPrompt 없음
            );

            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/descriptions/preview", storeId)
                            .with(user(ownerDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("tone이 없으면 400을 반환한다")
        void shouldReturn400WhenToneMissing() throws Exception {
            Map<String, Object> requestBody = Map.of(
                    "inputPrompt", "설명 작성"  // tone 없음
            );

            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/descriptions/preview", storeId)
                            .with(user(ownerDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("inputPrompt가 300자를 초과하면 400을 반환한다")
        void shouldReturn400WhenInputPromptTooLong() throws Exception {
            Map<String, Object> requestBody = Map.of(
                    "inputPrompt", "a".repeat(301),
                    "tone", "SALESY"
            );

            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/descriptions/preview", storeId)
                            .with(user(ownerDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("keywords가 10개를 초과하면 400을 반환한다")
        void shouldReturn400WhenKeywordsTooMany() throws Exception {
            List<String> tooManyKeywords = List.of("k1","k2","k3","k4","k5","k6","k7","k8","k9","k10","k11");
            Map<String, Object> requestBody = Map.of(
                    "inputPrompt", "설명 작성",
                    "tone", "FRIENDLY",
                    "keywords", tooManyKeywords
            );

            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/descriptions/preview", storeId)
                            .with(user(ownerDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ──────────────────────────────────────────────────
    // API-AI-002: AI 생성 상품 설명 적용
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("AI 생성 상품 설명 적용 (API-AI-002)")
    class ApplyDescriptionTests {

        private UUID aiLogId;
        private UUID productId;

        @BeforeEach
        void setUpApply() {
            aiLogId = UUID.randomUUID();
            productId = UUID.randomUUID();
        }

        @Test
        @DisplayName("OWNER가 유효한 요청을 보내면 200과 적용 결과를 반환한다")
        void ownerShouldReturn200WithApplyResult() throws Exception {
            // given
            ApplyDescriptionResult mockResult = new ApplyDescriptionResult(
                    aiLogId, productId, "바삭한 치킨입니다!", OffsetDateTime.now()
            );
            given(applyDescriptionUseCase.execute(any())).willReturn(mockResult);

            // when & then
            mockMvc.perform(patch("/api/v1/ai/stores/{storeId}/descriptions/{aiLogId}/apply",
                            storeId, aiLogId)
                            .with(user(ownerDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.aiLogId").value(aiLogId.toString()))
                    .andExpect(jsonPath("$.data.productId").value(productId.toString()))
                    .andExpect(jsonPath("$.data.appliedDescription").value("바삭한 치킨입니다!"));
        }

        @Test
        @DisplayName("MASTER도 200을 반환한다")
        void masterShouldReturn200() throws Exception {
            // given
            given(applyDescriptionUseCase.execute(any())).willReturn(
                    new ApplyDescriptionResult(aiLogId, productId, "치킨 맛있어요!", OffsetDateTime.now())
            );

            // when & then
            mockMvc.perform(patch("/api/v1/ai/stores/{storeId}/descriptions/{aiLogId}/apply",
                            storeId, aiLogId)
                            .with(user(masterDetails)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("인증되지 않은 요청은 4xx를 반환한다")
        void unauthenticatedShouldReturn4xx() throws Exception {
            mockMvc.perform(patch("/api/v1/ai/stores/{storeId}/descriptions/{aiLogId}/apply",
                            storeId, aiLogId))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("CUSTOMER 역할은 403을 반환한다")
        void customerShouldReturn403() throws Exception {
            User customer = User.builder()
                    .username("customer1").name("고객").password("pw").role(UserRole.CUSTOMER).build();
            CustomUserDetails customerDetails = new CustomUserDetails(customer);

            mockMvc.perform(patch("/api/v1/ai/stores/{storeId}/descriptions/{aiLogId}/apply",
                            storeId, aiLogId)
                            .with(user(customerDetails)))
                    .andExpect(status().isForbidden());
        }
    }

    // ──────────────────────────────────────────────────
    // API-AI-102: AI 로그 상세 조회
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("AI 로그 상세 조회 (API-AI-102)")
    class GetAiLogDetailTests {

        private UUID aiLogId;
        private UUID productId;

        @BeforeEach
        void setUpDetail() {
            aiLogId = UUID.randomUUID();
            productId = UUID.randomUUID();
        }

        @Test
        @DisplayName("OWNER가 요청하면 200과 상세 정보를 반환한다")
        void ownerShouldReturn200WithDetail() throws Exception {
            // given
            AiLogDetailResult mockResult = new AiLogDetailResult(
                    aiLogId, storeId, productId, "owner123",
                    AiRequestType.PRODUCT_DESCRIPTION, "FRIENDLY",
                    "테스트 프롬프트", "최종 프롬프트", "바삭한 치킨입니다!",
                    true, null, null, "gemini-pro", 1200,
                    10, 10, false, null, null, null, null, null, null, null,
                    OffsetDateTime.now()
            );
            given(getAiLogDetailUseCase.execute(any())).willReturn(mockResult);

            // when & then
            mockMvc.perform(get("/api/v1/ai/stores/{storeId}/logs/{aiLogId}", storeId, aiLogId)
                            .with(user(ownerDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.aiLogId").value(aiLogId.toString()))
                    .andExpect(jsonPath("$.data.storeId").value(storeId.toString()))
                    .andExpect(jsonPath("$.data.responseText").value("바삭한 치킨입니다!"))
                    .andExpect(jsonPath("$.data.inputPrompt").value("테스트 프롬프트"))
                    .andExpect(jsonPath("$.data.isSuccess").value(true));
        }

        @Test
        @DisplayName("MASTER도 200을 반환한다")
        void masterShouldReturn200() throws Exception {
            // given
            AiLogDetailResult mockResult = new AiLogDetailResult(
                    aiLogId, storeId, productId, "owner123",
                    AiRequestType.PRODUCT_DESCRIPTION, "SALESY",
                    "프롬프트", "최종", "설명입니다.", true, null, null,
                    "gemini-pro", 800, 6, 4, false, null, null, null, null,
                    null, null, null, OffsetDateTime.now()
            );
            given(getAiLogDetailUseCase.execute(any())).willReturn(mockResult);

            // when & then
            mockMvc.perform(get("/api/v1/ai/stores/{storeId}/logs/{aiLogId}", storeId, aiLogId)
                            .with(user(masterDetails)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("인증되지 않은 요청은 4xx를 반환한다")
        void unauthenticatedShouldReturn4xx() throws Exception {
            mockMvc.perform(get("/api/v1/ai/stores/{storeId}/logs/{aiLogId}", storeId, aiLogId))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("CUSTOMER 역할은 403을 반환한다")
        void customerShouldReturn403() throws Exception {
            User customer = User.builder()
                    .username("customer1").name("고객").password("pw").role(UserRole.CUSTOMER).build();
            CustomUserDetails customerDetails = new CustomUserDetails(customer);

            mockMvc.perform(get("/api/v1/ai/stores/{storeId}/logs/{aiLogId}", storeId, aiLogId)
                            .with(user(customerDetails)))
                    .andExpect(status().isForbidden());
        }
    }

    // ──────────────────────────────────────────────────
    // API-AI-101: AI 로그 목록 조회
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("AI 로그 목록 조회 (API-AI-101)")
    class SearchAiLogsTests {

        @Test
        @DisplayName("OWNER가 요청하면 200과 빈 Page를 반환한다")
        void ownerShouldReturn200WithPage() throws Exception {
            // given
            Page<AiLogSummaryResult> emptyPage =
                    new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            given(searchAiLogsUseCase.execute(any())).willReturn(emptyPage);

            // when & then
            mockMvc.perform(get("/api/v1/ai/stores/{storeId}/logs", storeId)
                            .with(user(ownerDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        @DisplayName("OWNER가 데이터가 있는 경우 200과 Page를 반환한다")
        void ownerShouldReturn200WithContent() throws Exception {
            // given
            UUID aiLogId = UUID.randomUUID();
            AiLogSummaryResult item = new AiLogSummaryResult(
                    aiLogId, null, "owner123",
                    AiRequestType.PRODUCT_DESCRIPTION, "FRIENDLY",
                    true, false, null, "바삭한 치킨입니다!", OffsetDateTime.now()
            );
            Page<AiLogSummaryResult> page =
                    new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);
            given(searchAiLogsUseCase.execute(any())).willReturn(page);

            // when & then
            mockMvc.perform(get("/api/v1/ai/stores/{storeId}/logs", storeId)
                            .with(user(ownerDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].aiLogId").value(aiLogId.toString()))
                    .andExpect(jsonPath("$.data.content[0].responseText").value("바삭한 치킨입니다!"));
        }

        @Test
        @DisplayName("MASTER도 200을 반환한다")
        void masterShouldReturn200() throws Exception {
            // given
            given(searchAiLogsUseCase.execute(any()))
                    .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

            // when & then
            mockMvc.perform(get("/api/v1/ai/stores/{storeId}/logs", storeId)
                            .with(user(masterDetails)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("인증되지 않은 요청은 4xx를 반환한다")
        void unauthenticatedShouldReturn4xx() throws Exception {
            mockMvc.perform(get("/api/v1/ai/stores/{storeId}/logs", storeId))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("CUSTOMER 역할은 403을 반환한다")
        void customerShouldReturn403() throws Exception {
            User customer = User.builder()
                    .username("customer1").name("고객").password("pw").role(UserRole.CUSTOMER).build();
            CustomUserDetails customerDetails = new CustomUserDetails(customer);

            mockMvc.perform(get("/api/v1/ai/stores/{storeId}/logs", storeId)
                            .with(user(customerDetails)))
                    .andExpect(status().isForbidden());
        }
    }

    // ──────────────────────────────────────────────────
    // API-AI-103: 상품 기준 AI 로그 목록 조회
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("상품 기준 AI 로그 목록 조회 (API-AI-103)")
    class SearchProductAiLogsTests {

        private UUID productId;

        @BeforeEach
        void setUpProduct() {
            productId = UUID.randomUUID();
        }

        @Test
        @DisplayName("OWNER가 요청하면 200과 빈 Page를 반환한다")
        void ownerShouldReturn200WithEmptyPage() throws Exception {
            // given
            Page<AiLogSummaryResult> emptyPage =
                    new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            given(searchProductAiLogsUseCase.execute(any())).willReturn(emptyPage);

            // when & then
            mockMvc.perform(get("/api/v1/ai/stores/{storeId}/products/{productId}/logs",
                            storeId, productId)
                            .with(user(ownerDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        @DisplayName("OWNER가 데이터가 있는 경우 200과 Page를 반환한다")
        void ownerShouldReturn200WithContent() throws Exception {
            // given
            UUID aiLogId = UUID.randomUUID();
            AiLogSummaryResult item = new AiLogSummaryResult(
                    aiLogId, productId, "owner123",
                    AiRequestType.PRODUCT_DESCRIPTION, "FRIENDLY",
                    true, false, null, "바삭한 치킨입니다!", OffsetDateTime.now()
            );
            Page<AiLogSummaryResult> page =
                    new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);
            given(searchProductAiLogsUseCase.execute(any())).willReturn(page);

            // when & then
            mockMvc.perform(get("/api/v1/ai/stores/{storeId}/products/{productId}/logs",
                            storeId, productId)
                            .with(user(ownerDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].aiLogId").value(aiLogId.toString()))
                    .andExpect(jsonPath("$.data.content[0].productId").value(productId.toString()))
                    .andExpect(jsonPath("$.data.content[0].responseText").value("바삭한 치킨입니다!"));
        }

        @Test
        @DisplayName("MASTER도 200을 반환한다")
        void masterShouldReturn200() throws Exception {
            // given
            given(searchProductAiLogsUseCase.execute(any()))
                    .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

            // when & then
            mockMvc.perform(get("/api/v1/ai/stores/{storeId}/products/{productId}/logs",
                            storeId, productId)
                            .with(user(masterDetails)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("인증되지 않은 요청은 4xx를 반환한다")
        void unauthenticatedShouldReturn4xx() throws Exception {
            mockMvc.perform(get("/api/v1/ai/stores/{storeId}/products/{productId}/logs",
                            storeId, productId))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("CUSTOMER 역할은 403을 반환한다")
        void customerShouldReturn403() throws Exception {
            User customer = User.builder()
                    .username("customer1").name("고객").password("pw").role(UserRole.CUSTOMER).build();
            CustomUserDetails customerDetails = new CustomUserDetails(customer);

            mockMvc.perform(get("/api/v1/ai/stores/{storeId}/products/{productId}/logs",
                            storeId, productId)
                            .with(user(customerDetails)))
                    .andExpect(status().isForbidden());
        }
    }

    // ──────────────────────────────────────────────────
    // API-AI-203: AI 로그 재실행 (재시도)
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("AI 로그 재실행 (API-AI-203)")
    class RetryDescriptionTests {

        private UUID aiLogId;
        private UUID newAiLogId;
        private UUID productId;

        @BeforeEach
        void setUpRetry() {
            aiLogId = UUID.randomUUID();
            newAiLogId = UUID.randomUUID();
            productId = UUID.randomUUID();
        }

        @Test
        @DisplayName("OWNER가 요청하면 201과 재실행 결과를 반환한다")
        void ownerShouldReturn201WithRetryResult() throws Exception {
            // given
            RetryDescriptionResult mockResult = new RetryDescriptionResult(
                    newAiLogId, aiLogId, "재생성된 치킨 설명!", "최종프롬프트"
            );
            given(retryDescriptionUseCase.execute(any())).willReturn(mockResult);

            // when & then
            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/logs/{aiLogId}/retry",
                            storeId, aiLogId)
                            .with(user(ownerDetails))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.data.aiLogId").value(newAiLogId.toString()))
                    .andExpect(jsonPath("$.data.sourceAiLogId").value(aiLogId.toString()))
                    .andExpect(jsonPath("$.data.responseText").value("재생성된 치킨 설명!"));
        }

        @Test
        @DisplayName("overrideInputPrompt를 포함한 요청도 201을 반환한다")
        void shouldReturn201WithOverrideInputPrompt() throws Exception {
            // given
            RetryDescriptionResult mockResult = new RetryDescriptionResult(
                    newAiLogId, aiLogId, "변경된 설명!", "최종프롬프트"
            );
            given(retryDescriptionUseCase.execute(any())).willReturn(mockResult);

            Map<String, Object> requestBody = Map.of(
                    "overrideInputPrompt", "변경된 프롬프트로 재생성해줘"
            );

            // when & then
            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/logs/{aiLogId}/retry",
                            storeId, aiLogId)
                            .with(user(ownerDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.responseText").value("변경된 설명!"));
        }

        @Test
        @DisplayName("MASTER도 201을 반환한다")
        void masterShouldReturn201() throws Exception {
            // given
            given(retryDescriptionUseCase.execute(any())).willReturn(
                    new RetryDescriptionResult(newAiLogId, aiLogId, "치킨 맛있어요!", "프롬프트")
            );

            // when & then
            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/logs/{aiLogId}/retry",
                            storeId, aiLogId)
                            .with(user(masterDetails))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("인증되지 않은 요청은 4xx를 반환한다")
        void unauthenticatedShouldReturn4xx() throws Exception {
            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/logs/{aiLogId}/retry",
                            storeId, aiLogId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("CUSTOMER 역할은 403을 반환한다")
        void customerShouldReturn403() throws Exception {
            User customer = User.builder()
                    .username("customer1").name("고객").password("pw").role(UserRole.CUSTOMER).build();
            CustomUserDetails customerDetails = new CustomUserDetails(customer);

            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/logs/{aiLogId}/retry",
                            storeId, aiLogId)
                            .with(user(customerDetails))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("overrideInputPrompt가 300자를 초과하면 400을 반환한다")
        void shouldReturn400WhenOverrideInputPromptTooLong() throws Exception {
            Map<String, Object> requestBody = Map.of(
                    "overrideInputPrompt", "a".repeat(301)
            );

            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/logs/{aiLogId}/retry",
                            storeId, aiLogId)
                            .with(user(ownerDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ──────────────────────────────────────────────────
    // API-AI-301: AI 음식 이미지 생성
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("AI 음식 이미지 생성 (API-AI-301)")
    class GenerateImageTests {

        @Test
        @DisplayName("OWNER가 유효한 요청을 보내면 201과 이미지 데이터를 반환한다")
        void ownerShouldReturn201WithImageData() throws Exception {
            // given
            UUID aiLogId = UUID.randomUUID();
            GenerateImageResult mockResult = new GenerateImageResult(
                    aiLogId, "base64imagedata==", "image/png"
            );
            given(generateImageUseCase.execute(any())).willReturn(mockResult);

            Map<String, Object> requestBody = Map.of(
                    "productName", "황금 바삭치킨",
                    "prompt", "따뜻한 식당 배경으로 맛있게",
                    "includeText", false
            );

            // when & then
            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/images/preview", storeId)
                            .with(user(ownerDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.data.aiLogId").value(aiLogId.toString()))
                    .andExpect(jsonPath("$.data.imageData").value("base64imagedata=="))
                    .andExpect(jsonPath("$.data.mimeType").value("image/png"));
        }

        @Test
        @DisplayName("MASTER도 201을 반환한다")
        void masterShouldReturn201() throws Exception {
            // given
            given(generateImageUseCase.execute(any())).willReturn(
                    new GenerateImageResult(UUID.randomUUID(), "data==", "image/png")
            );

            Map<String, Object> requestBody = Map.of(
                    "productName", "치킨",
                    "prompt", "맛있게 보이게",
                    "includeText", false
            );

            // when & then
            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/images/preview", storeId)
                            .with(user(masterDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("prompt가 없으면 400을 반환한다")
        void shouldReturn400WhenPromptMissing() throws Exception {
            Map<String, Object> requestBody = Map.of(
                    "productName", "치킨",
                    "includeText", false
                    // prompt 없음
            );

            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/images/preview", storeId)
                            .with(user(ownerDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("prompt가 500자를 초과하면 400을 반환한다")
        void shouldReturn400WhenPromptTooLong() throws Exception {
            Map<String, Object> requestBody = Map.of(
                    "productName", "치킨",
                    "prompt", "a".repeat(501),
                    "includeText", false
            );

            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/images/preview", storeId)
                            .with(user(ownerDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("CUSTOMER 역할은 403을 반환한다")
        void customerShouldReturn403() throws Exception {
            User customer = User.builder()
                    .username("customer1").name("고객").password("pw").role(UserRole.CUSTOMER).build();
            CustomUserDetails customerDetails = new CustomUserDetails(customer);

            Map<String, Object> requestBody = Map.of(
                    "productName", "치킨",
                    "prompt", "맛있게",
                    "includeText", false
            );

            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/images/preview", storeId)
                            .with(user(customerDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("aspectRatio와 style을 포함한 요청도 201을 반환한다")
        void shouldReturn201WithAspectRatioAndStyle() throws Exception {
            // given
            given(generateImageUseCase.execute(any())).willReturn(
                    new GenerateImageResult(UUID.randomUUID(), "data==", "image/png")
            );

            Map<String, Object> requestBody = Map.of(
                    "productName", "황금 바삭치킨",
                    "prompt", "따뜻한 식당 배경으로",
                    "aspectRatio", "LANDSCAPE",
                    "style", "수채화",
                    "includeText", false
            );

            // when & then
            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/images/preview", storeId)
                            .with(user(ownerDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("style이 50자를 초과하면 400을 반환한다")
        void shouldReturn400WhenStyleTooLong() throws Exception {
            Map<String, Object> requestBody = Map.of(
                    "productName", "치킨",
                    "prompt", "맛있게",
                    "style", "a".repeat(51),
                    "includeText", false
            );

            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/images/preview", storeId)
                            .with(user(ownerDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("인증되지 않은 요청은 4xx를 반환한다")
        void unauthenticatedShouldReturn4xx() throws Exception {
            Map<String, Object> requestBody = Map.of(
                    "productName", "치킨",
                    "prompt", "맛있게",
                    "includeText", false
            );

            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/images/preview", storeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().is4xxClientError());
        }
    }

    // ──────────────────────────────────────────────────
    // API-AI-205: AI 설명 원복
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("AI 설명 원복 (API-AI-205)")
    class RollbackDescriptionTests {

        private UUID aiLogId;
        private UUID productId;

        @BeforeEach
        void setUpRollback() {
            aiLogId = UUID.randomUUID();
            productId = UUID.randomUUID();
        }

        @Test
        @DisplayName("OWNER가 요청하면 200과 원복 결과를 반환한다")
        void ownerShouldReturn200WithRollbackResult() throws Exception {
            // given
            RollbackDescriptionResult mockResult = new RollbackDescriptionResult(
                    aiLogId, productId, "원래 설명이었던 치킨", OffsetDateTime.now()
            );
            given(rollbackDescriptionUseCase.execute(any())).willReturn(mockResult);

            // when & then
            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/descriptions/{aiLogId}/rollback",
                            storeId, aiLogId)
                            .with(user(ownerDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.aiLogId").value(aiLogId.toString()))
                    .andExpect(jsonPath("$.data.productId").value(productId.toString()))
                    .andExpect(jsonPath("$.data.restoredDescription").value("원래 설명이었던 치킨"));
        }

        @Test
        @DisplayName("MASTER도 200을 반환한다")
        void masterShouldReturn200() throws Exception {
            // given
            given(rollbackDescriptionUseCase.execute(any())).willReturn(
                    new RollbackDescriptionResult(aiLogId, productId, "이전 설명", OffsetDateTime.now())
            );

            // when & then
            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/descriptions/{aiLogId}/rollback",
                            storeId, aiLogId)
                            .with(user(masterDetails)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("인증되지 않은 요청은 4xx를 반환한다")
        void unauthenticatedShouldReturn4xx() throws Exception {
            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/descriptions/{aiLogId}/rollback",
                            storeId, aiLogId))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("CUSTOMER 역할은 403을 반환한다")
        void customerShouldReturn403() throws Exception {
            User customer = User.builder()
                    .username("customer1").name("고객").password("pw").role(UserRole.CUSTOMER).build();
            CustomUserDetails customerDetails = new CustomUserDetails(customer);

            mockMvc.perform(post("/api/v1/ai/stores/{storeId}/descriptions/{aiLogId}/rollback",
                            storeId, aiLogId)
                            .with(user(customerDetails)))
                    .andExpect(status().isForbidden());
        }
    }

    // ──────────────────────────────────────────────────
    // API-AI-204: AI 호출 통계
    // ──────────────────────────────────────────────────
    @Nested
    @DisplayName("AI 호출 통계 (API-AI-204)")
    class GetAiStatsTests {

        @Test
        @DisplayName("MASTER가 요청하면 200과 통계를 반환한다")
        void masterShouldReturn200WithStats() throws Exception {
            // given
            AiStatsResult mockResult = new AiStatsResult(storeId, 20L, 18L, 2L, 90.0, 320L, null, null);
            given(getAiStatsUseCase.execute(any())).willReturn(mockResult);

            // when & then
            mockMvc.perform(get("/api/v1/ai/stores/{storeId}/stats", storeId)
                            .with(user(masterDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.storeId").value(storeId.toString()))
                    .andExpect(jsonPath("$.data.totalCount").value(20))
                    .andExpect(jsonPath("$.data.successCount").value(18))
                    .andExpect(jsonPath("$.data.failureCount").value(2))
                    .andExpect(jsonPath("$.data.successRate").value(90.0))
                    .andExpect(jsonPath("$.data.avgResponseTimeMs").value(320));
        }

        @Test
        @DisplayName("조회 결과가 없으면 totalCount=0으로 반환한다")
        void shouldReturn200WithZeroStats() throws Exception {
            // given
            AiStatsResult mockResult = new AiStatsResult(storeId, 0L, 0L, 0L, 0.0, null, null, null);
            given(getAiStatsUseCase.execute(any())).willReturn(mockResult);

            // when & then
            mockMvc.perform(get("/api/v1/ai/stores/{storeId}/stats", storeId)
                            .with(user(masterDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalCount").value(0))
                    .andExpect(jsonPath("$.data.avgResponseTimeMs").doesNotExist());
        }

        @Test
        @DisplayName("OWNER 역할은 403을 반환한다")
        void ownerShouldReturn403() throws Exception {
            mockMvc.perform(get("/api/v1/ai/stores/{storeId}/stats", storeId)
                            .with(user(ownerDetails)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증되지 않은 요청은 4xx를 반환한다")
        void unauthenticatedShouldReturn4xx() throws Exception {
            mockMvc.perform(get("/api/v1/ai/stores/{storeId}/stats", storeId))
                    .andExpect(status().is4xxClientError());
        }
    }
}
