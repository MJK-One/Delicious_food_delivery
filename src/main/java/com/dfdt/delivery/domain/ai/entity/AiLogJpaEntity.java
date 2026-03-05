package com.dfdt.delivery.domain.ai.entity;

//import com.dfdt.delivery.common.Entity.SoftDeleteEntity;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.CreateAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.SoftDeleteAudit;
import com.dfdt.delivery.common.infrastructure.persistence.embedded.UpdateAudit;
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
public class AiLogJpaEntity  {

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
            Integer responseTimeMs
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

}