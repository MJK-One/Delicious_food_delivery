package com.dfdt.delivery.common.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.OffsetDateTime;

@MappedSuperclass
public abstract class SoftDeleteEntity {

    @Column(name = "deleted_at")
    protected OffsetDateTime deletedAt;

    @Column(name = "deleted_by", length = 10)
    protected String deletedBy;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
