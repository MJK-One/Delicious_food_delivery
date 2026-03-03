package com.dfdt.delivery.common.infrastructure.persistence.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.OffsetDateTime;

@Embeddable
public class CreateAudit {

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", length = 10, updatable = false)
    private String createdBy;

    protected CreateAudit() {}

    public CreateAudit(OffsetDateTime createdAt, String createdBy) {
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    public static CreateAudit now(String createdBy) {
        return new CreateAudit(OffsetDateTime.now(), createdBy);
    }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
}