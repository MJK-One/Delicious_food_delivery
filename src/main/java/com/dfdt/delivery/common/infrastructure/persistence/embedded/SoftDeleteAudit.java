package com.dfdt.delivery.common.infrastructure.persistence.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.OffsetDateTime;

@Embeddable
public class SoftDeleteAudit {

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "deleted_by", length = 10)
    private String deletedBy;

    protected SoftDeleteAudit() {}

    public SoftDeleteAudit(OffsetDateTime deletedAt, String deletedBy) {
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public static SoftDeleteAudit active() {
        return new SoftDeleteAudit(null, null);
    }

    public void softDelete(String deletedBy) {
        if (this.deletedAt != null) return;
        this.deletedAt = OffsetDateTime.now();
        this.deletedBy = deletedBy;
    }

    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public OffsetDateTime getDeletedAt() { return deletedAt; }
    public String getDeletedBy() { return deletedBy; }
}