package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.domain.ai.application.dto.ApplyDescriptionCommand;
import com.dfdt.delivery.domain.ai.application.dto.ApplyDescriptionResult;

/**
 * API-AI-002: AI 생성 상품 설명 적용 UseCase.
 * 미리보기로 생성된 AI 설명(AiLog)을 실제 Product.description에 반영합니다.
 */
public interface ApplyDescriptionUseCase {

    ApplyDescriptionResult execute(ApplyDescriptionCommand command);
}