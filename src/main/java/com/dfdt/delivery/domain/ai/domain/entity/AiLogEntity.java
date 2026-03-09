package com.dfdt.delivery.domain.ai.domain.entity;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.domain.ai.domain.entity.enums.AiRequestType;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_ai_log")
public class AiLogEntity {

    @Id
    @Column(name = "ai_log_id", nullable = false, updatable = false)
    private UUID aiLogId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "requested_by", nullable = false, length = 10)
    private String requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 30)
    private AiRequestType requestType;

    @Column(name = "input_prompt", nullable = false, columnDefinition = "TEXT")
    private String inputPrompt;

    @Column(name = "final_prompt", nullable = false, columnDefinition = "TEXT")
    private String finalPrompt;

    @Column(name = "response_text", columnDefinition = "TEXT")
    private String responseText;

    @Column(name = "is_success", nullable = false)
    private Boolean isSuccess;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @Column(name = "is_applied", nullable = false)
    private Boolean isApplied;

    @Column(name = "applied_at")
    private OffsetDateTime appliedAt;

    @Column(name = "applied_by", length = 10)
    private String appliedBy;

    @Column(name = "source_ai_log_id")
    private UUID sourceAiLogId;

    @Column(name = "previous_description", columnDefinition = "TEXT")
    private String previousDescription;

    @Column(name = "rolled_back_at")
    private OffsetDateTime rolledBackAt;

    @Column(name = "rolled_back_by", length = 10)
    private String rolledBackBy;

    @Column(name = "tone", length = 20)
    private String tone;

    @Column(name = "keywords_snapshot", columnDefinition = "TEXT")
    private String keywordsSnapshot;

    @Column(name = "prompt_char_count")
    private Integer promptCharCount;

    @Column(name = "response_char_count")
    private Integer responseCharCount;

    @Embedded
    private CreateAudit createAudit;

    @Embedded
    private SoftDeleteAudit softDeleteAudit;

    protected AiLogEntity() {
        // JPA 기본 생성자
    }

    private AiLogEntity(
            UUID aiLogId,
            UUID storeId,
            UUID productId,
            String requestedBy,
            AiRequestType requestType,
            String inputPrompt,
            String finalPrompt,
            String responseText,
            Boolean isSuccess,
            String errorCode,
            String errorMessage,
            String modelName,
            Integer responseTimeMs,
            Boolean isApplied,
            OffsetDateTime appliedAt,
            String appliedBy,
            UUID sourceAiLogId,
            String previousDescription,
            OffsetDateTime rolledBackAt,
            String rolledBackBy,
            String tone,
            String keywordsSnapshot,
            Integer promptCharCount,
            Integer responseCharCount,
            CreateAudit createAudit,
            SoftDeleteAudit softDeleteAudit
    ) {
        this.aiLogId = aiLogId;
        this.storeId = storeId;
        this.productId = productId;
        this.requestedBy = requestedBy;
        this.requestType = requestType;
        this.inputPrompt = inputPrompt;
        this.finalPrompt = finalPrompt;
        this.responseText = responseText;
        this.isSuccess = isSuccess;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.modelName = modelName;
        this.responseTimeMs = responseTimeMs;
        this.isApplied = isApplied;
        this.appliedAt = appliedAt;
        this.appliedBy = appliedBy;
        this.sourceAiLogId = sourceAiLogId;
        this.previousDescription = previousDescription;
        this.rolledBackAt = rolledBackAt;
        this.rolledBackBy = rolledBackBy;
        this.tone = tone;
        this.keywordsSnapshot = keywordsSnapshot;
        this.promptCharCount = promptCharCount;
        this.responseCharCount = responseCharCount;
        this.createAudit = createAudit;
        this.softDeleteAudit = (softDeleteAudit != null) ? softDeleteAudit : SoftDeleteAudit.active();
    }

    private static void validateCommon(
            UUID storeId,
            String requestedBy,
            AiRequestType requestType,
            String inputPrompt,
            String finalPrompt,
            Integer responseTimeMs
    ) {
        if (storeId == null) {
            throw new IllegalArgumentException("storeId must not be null");
        }
        if (requestedBy == null || requestedBy.isBlank()) {
            throw new IllegalArgumentException("requestedBy must not be blank");
        }
        if (requestType == null) {
            throw new IllegalArgumentException("requestType must not be null");
        }
        if (inputPrompt == null || inputPrompt.isBlank()) {
            throw new IllegalArgumentException("inputPrompt must not be blank");
        }
        if (finalPrompt == null || finalPrompt.isBlank()) {
            throw new IllegalArgumentException("finalPrompt must not be blank");
        }
        if (inputPrompt.length() > 300) {
            throw new IllegalArgumentException("inputPrompt length must be <= 300");
        }
        if (responseTimeMs != null && responseTimeMs < 0) {
            throw new IllegalArgumentException("responseTimeMs must be >= 0");
        }
    }

    private static int calculateLength(String value) {
        return value == null ? 0 : value.length();
    }

    private static AiLogEntity createSuccess(
            UUID storeId,
            UUID productId,
            String requestedBy,
            AiRequestType requestType,
            String inputPrompt,
            String finalPrompt,
            String responseText,
            String modelName,
            Integer responseTimeMs,
            UUID sourceAiLogId,
            String tone,
            String keywordsSnapshot
    ) {
        validateCommon(storeId, requestedBy, requestType, inputPrompt, finalPrompt, responseTimeMs);

        return new AiLogEntity(
                UUID.randomUUID(),
                storeId,
                productId,
                requestedBy,
                requestType,
                inputPrompt,
                finalPrompt,
                responseText,
                true,
                null,
                null,
                modelName,
                responseTimeMs,
                false,
                null,
                null,
                sourceAiLogId,
                null,
                null,
                null,
                tone,
                keywordsSnapshot,
                calculateLength(inputPrompt),
                calculateLength(responseText),
                CreateAudit.now(requestedBy),
                SoftDeleteAudit.active()
        );
    }

    private static AiLogEntity createFailure(
            UUID storeId,
            UUID productId,
            String requestedBy,
            AiRequestType requestType,
            String inputPrompt,
            String finalPrompt,
            String errorCode,
            String errorMessage,
            String modelName,
            Integer responseTimeMs,
            UUID sourceAiLogId,
            String tone,
            String keywordsSnapshot
    ) {
        validateCommon(storeId, requestedBy, requestType, inputPrompt, finalPrompt, responseTimeMs);

        return new AiLogEntity(
                UUID.randomUUID(),
                storeId,
                productId,
                requestedBy,
                requestType,
                inputPrompt,
                finalPrompt,
                null,
                false,
                errorCode,
                errorMessage,
                modelName,
                responseTimeMs,
                false,
                null,
                null,
                sourceAiLogId,
                null,
                null,
                null,
                tone,
                keywordsSnapshot,
                calculateLength(inputPrompt),
                0,
                CreateAudit.now(requestedBy),
                SoftDeleteAudit.active()
        );
    }

    public static AiLogEntity successProductDescription(
            UUID storeId,
            UUID productId,
            String requestedBy,
            String inputPrompt,
            String finalPrompt,
            String responseText,
            String modelName,
            Integer responseTimeMs,
            UUID sourceAiLogId,
            String tone,
            String keywordsSnapshot
    ) {
        return createSuccess(
                storeId,
                productId,
                requestedBy,
                AiRequestType.PRODUCT_DESCRIPTION,
                inputPrompt,
                finalPrompt,
                responseText,
                modelName,
                responseTimeMs,
                sourceAiLogId,
                tone,
                keywordsSnapshot
        );
    }

    public static AiLogEntity failureProductDescription(
            UUID storeId,
            UUID productId,
            String requestedBy,
            String inputPrompt,
            String finalPrompt,
            String errorCode,
            String errorMessage,
            String modelName,
            Integer responseTimeMs,
            UUID sourceAiLogId,
            String tone,
            String keywordsSnapshot
    ) {
        return createFailure(
                storeId,
                productId,
                requestedBy,
                AiRequestType.PRODUCT_DESCRIPTION,
                inputPrompt,
                finalPrompt,
                errorCode,
                errorMessage,
                modelName,
                responseTimeMs,
                sourceAiLogId,
                tone,
                keywordsSnapshot
        );
    }

    public static AiLogEntity successFoodImageGeneration(
            UUID storeId,
            UUID productId,
            String requestedBy,
            String inputPrompt,
            String finalPrompt,
            String responseText,
            String modelName,
            Integer responseTimeMs,
            UUID sourceAiLogId,
            String tone,
            String keywordsSnapshot
    ) {
        return createSuccess(
                storeId,
                productId,
                requestedBy,
                AiRequestType.FOOD_IMAGE_GENERATION,
                inputPrompt,
                finalPrompt,
                responseText,
                modelName,
                responseTimeMs,
                sourceAiLogId,
                tone,
                keywordsSnapshot
        );
    }

    public static AiLogEntity failureFoodImageGeneration(
            UUID storeId,
            UUID productId,
            String requestedBy,
            String inputPrompt,
            String finalPrompt,
            String errorCode,
            String errorMessage,
            String modelName,
            Integer responseTimeMs,
            UUID sourceAiLogId,
            String tone,
            String keywordsSnapshot
    ) {
        return createFailure(
                storeId,
                productId,
                requestedBy,
                AiRequestType.FOOD_IMAGE_GENERATION,
                inputPrompt,
                finalPrompt,
                errorCode,
                errorMessage,
                modelName,
                responseTimeMs,
                sourceAiLogId,
                tone,
                keywordsSnapshot
        );
    }

    public void applyDescription(String previousDescription, String appliedBy) {
        if (appliedBy == null || appliedBy.isBlank()) {
            throw new IllegalArgumentException("appliedBy must not be blank");
        }
        this.previousDescription = previousDescription;
        this.isApplied = true;
        this.appliedAt = OffsetDateTime.now();
        this.appliedBy = appliedBy;
    }

    public void rollback(String rolledBackBy) {
        if (rolledBackBy == null || rolledBackBy.isBlank()) {
            throw new IllegalArgumentException("rolledBackBy must not be blank");
        }
        this.rolledBackAt = OffsetDateTime.now();
        this.rolledBackBy = rolledBackBy;
    }

    public void softDelete(String deletedBy) {
        if (this.softDeleteAudit == null) {
            this.softDeleteAudit = SoftDeleteAudit.active();
        }
        this.softDeleteAudit.softDelete(deletedBy);
    }

    public boolean isDeleted() {
        return this.softDeleteAudit != null && this.softDeleteAudit.isDeleted();
    }

    public UUID getAiLogId() {
        return aiLogId;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public AiRequestType getRequestType() {
        return requestType;
    }

    public String getInputPrompt() {
        return inputPrompt;
    }

    public String getFinalPrompt() {
        return finalPrompt;
    }

    public String getResponseText() {
        return responseText;
    }

    public Boolean getIsSuccess() {
        return isSuccess;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getModelName() {
        return modelName;
    }

    public Integer getResponseTimeMs() {
        return responseTimeMs;
    }

    public Boolean getIsApplied() {
        return isApplied;
    }

    public OffsetDateTime getAppliedAt() {
        return appliedAt;
    }

    public String getAppliedBy() {
        return appliedBy;
    }

    public UUID getSourceAiLogId() {
        return sourceAiLogId;
    }

    public String getPreviousDescription() {
        return previousDescription;
    }

    public OffsetDateTime getRolledBackAt() {
        return rolledBackAt;
    }

    public String getRolledBackBy() {
        return rolledBackBy;
    }

    public String getTone() {
        return tone;
    }

    public String getKeywordsSnapshot() {
        return keywordsSnapshot;
    }

    public Integer getPromptCharCount() {
        return promptCharCount;
    }

    public Integer getResponseCharCount() {
        return responseCharCount;
    }

    public CreateAudit getCreateAudit() {
        return createAudit;
    }

    public SoftDeleteAudit getSoftDeleteAudit() {
        return softDeleteAudit;
    }
}