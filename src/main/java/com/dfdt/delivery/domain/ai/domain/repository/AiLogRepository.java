package com.dfdt.delivery.domain.ai.domain.repository;

import com.dfdt.delivery.domain.ai.domain.entity.AiLogEntity;

import java.util.Optional;
import java.util.UUID;

public interface AiLogRepository {

    AiLogEntity save(AiLogEntity entity);

    Optional<AiLogEntity> findById(UUID aiLogId);
}
