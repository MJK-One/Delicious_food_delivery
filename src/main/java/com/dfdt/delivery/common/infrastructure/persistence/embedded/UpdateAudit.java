package com.dfdt.delivery.common.infrastructure.persistence.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.OffsetDateTime;

@Embeddable
public class UpdateAudit {

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by", length = 10)
    private String updatedBy;

    protected UpdateAudit() {}

    public UpdateAudit(OffsetDateTime updatedAt, String updatedBy) {
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public static UpdateAudit empty() {
        return new UpdateAudit(null, null);
    }

    public void touch(String updatedBy) {
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = updatedBy;
    }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
}
