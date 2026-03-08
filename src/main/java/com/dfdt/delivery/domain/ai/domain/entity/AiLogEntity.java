package com.dfdt.delivery.domain.ai.domain.entity;

import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.domain.ai.domain.entity.enums.AiRequestType;
import jakarta.persistence.*;

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
        this.createAudit = createAudit;
        this.softDeleteAudit = (softDeleteAudit != null) ? softDeleteAudit : SoftDeleteAudit.active();
    }

    private static void validateCommon(
            UUID storeId,
            String requestedBy,
            String inputPrompt,
            String finalPrompt
    ) {
        if (storeId == null) {
            throw new IllegalArgumentException("storeId must not be null");
        }
        if (requestedBy == null || requestedBy.isBlank()) {
            throw new IllegalArgumentException("requestedBy must not be blank");
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
    }

    public static AiLogEntity successProductDescription(
            UUID storeId,
            UUID productId,
            String requestedBy,
            String inputPrompt,
            String finalPrompt,
            String responseText,
            String modelName,
            Integer responseTimeMs
    ) {
        validateCommon(storeId, requestedBy, inputPrompt, finalPrompt);

        return new AiLogEntity(
                UUID.randomUUID(),
                storeId,
                productId,
                requestedBy,
                AiRequestType.PRODUCT_DESCRIPTION,
                inputPrompt,
                finalPrompt,
                responseText,
                true,
                null,
                null,
                modelName,
                responseTimeMs,
                CreateAudit.now(requestedBy),
                SoftDeleteAudit.active()
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
            Integer responseTimeMs
    ) {
        validateCommon(storeId, requestedBy, inputPrompt, finalPrompt);

        return new AiLogEntity(
                UUID.randomUUID(),
                storeId,
                productId,
                requestedBy,
                AiRequestType.PRODUCT_DESCRIPTION,
                inputPrompt,
                finalPrompt,
                null, // 실패 시 responseText 없음
                false,
                errorCode,
                errorMessage,
                modelName,
                responseTimeMs,
                CreateAudit.now(requestedBy),
                SoftDeleteAudit.active()
        );
    }

    public static AiLogEntity successFoodImageGeneration(
            UUID storeId,
            UUID productId,
            String requestedBy,
            String inputPrompt,
            String finalPrompt,
            String responseText,   // 초기엔 URL/요약/JSON 일부를 문자열로 저장 가능
            String modelName,
            Integer responseTimeMs
    ) {
        validateCommon(storeId, requestedBy, inputPrompt, finalPrompt);

        return new AiLogEntity(
                UUID.randomUUID(),
                storeId,
                productId,
                requestedBy,
                AiRequestType.FOOD_IMAGE_GENERATION,
                inputPrompt,
                finalPrompt,
                responseText,
                true,
                null,
                null,
                modelName,
                responseTimeMs,
                CreateAudit.now(requestedBy),
                SoftDeleteAudit.active()
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
            Integer responseTimeMs
    ) {
        validateCommon(storeId, requestedBy, inputPrompt, finalPrompt);

        return new AiLogEntity(
                UUID.randomUUID(),
                storeId,
                productId,
                requestedBy,
                AiRequestType.FOOD_IMAGE_GENERATION,
                inputPrompt,
                finalPrompt,
                null, // 실패 시 responseText 없음
                false,
                errorCode,
                errorMessage,
                modelName,
                responseTimeMs,
                CreateAudit.now(requestedBy),
                SoftDeleteAudit.active()
        );
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

    public CreateAudit getCreateAudit() {
        return createAudit;
    }

    public SoftDeleteAudit getSoftDeleteAudit() {
        return softDeleteAudit;
    }
}