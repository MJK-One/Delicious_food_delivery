package com.dfdt.delivery.domain.ai.entity;

import com.dfdt.delivery.common.Entity.SoftDeleteEntity;
import com.dfdt.delivery.domain.ai.enums.AiRequestType;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_ai_log")
@SQLDelete(sql = "UPDATE p_ai_log SET deleted_at = now() WHERE ai_log_id = ?")
@Where(clause = "deleted_at IS NULL")
public class AiLogJpaEntity extends SoftDeleteEntity {

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

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", length = 10)
    private String createdBy;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "deleted_by", length = 10)
    private String deletedBy;

    protected AiLogJpaEntity() {
    }

    public AiLogJpaEntity(
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
            OffsetDateTime createdAt,
            String createdBy,
            OffsetDateTime deletedAt,
            String deletedBy
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
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    // getters
    public UUID getAiLogId() { return aiLogId; }
    public UUID getStoreId() { return storeId; }
    public UUID getProductId() { return productId; }
    public String getRequestedBy() { return requestedBy; }
    public AiRequestType getRequestType() { return requestType; }
    public String getInputPrompt() { return inputPrompt; }
    public String getFinalPrompt() { return finalPrompt; }
    public String getResponseText() { return responseText; }
    public Boolean getIsSuccess() { return isSuccess; }
    public String getErrorCode() { return errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public String getModelName() { return modelName; }
    public Integer getResponseTimeMs() { return responseTimeMs; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }
    public String getDeletedBy() { return deletedBy; }
}
