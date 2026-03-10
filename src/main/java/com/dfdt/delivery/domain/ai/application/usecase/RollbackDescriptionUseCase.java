package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.domain.ai.application.dto.RollbackDescriptionCommand;
import com.dfdt.delivery.domain.ai.application.dto.RollbackDescriptionResult;

public interface RollbackDescriptionUseCase {

    RollbackDescriptionResult execute(RollbackDescriptionCommand command);
}
