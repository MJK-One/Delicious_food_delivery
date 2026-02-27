package com.dfdt.delivery.common.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.OffsetDateTime;

@MappedSuperclass
public abstract class BaseAuditSoftDeleteEntity extends BaseAuditEntity {

    @Column(name = "deleted_at")
    protected OffsetDateTime deletedAt;

    @Column(name = "deleted_by", length = 10)
    protected String deletedBy;
}
