package com.dfdt.delivery.domain.ai.infrastructure.persistence;

import com.dfdt.delivery.domain.ai.domain.entity.AiLogEntity;
import com.dfdt.delivery.domain.ai.domain.repository.AiLogRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * AiLogEntity JPA Repository.
 * JpaRepository가 제공하는 save(), findById()가 AiLogRepository 구현을 대신합니다.
 */
public interface JpaAiLogRepository extends JpaRepository<AiLogEntity, UUID>, AiLogRepository {
}