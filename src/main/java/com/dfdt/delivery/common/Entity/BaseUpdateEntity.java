package com.dfdt.delivery.common.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.OffsetDateTime;

@MappedSuperclass
public abstract class BaseUpdateEntity extends BaseAuditEntity {

    @Column(name = "updated_at")
    protected OffsetDateTime updatedAt;

    @Column(name = "updated_by", length = 10)
    protected String updatedBy;
}